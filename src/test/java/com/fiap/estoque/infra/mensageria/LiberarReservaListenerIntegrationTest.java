package com.fiap.estoque.infra.mensageria;

import com.fiap.estoque.core.gateway.ReservaGateway;
import com.fiap.estoque.resources.TokenTestHelper;
import com.fiap.estoque.resources.testcontainer.AbstractContainer;
import com.fiap.estoque.resources.testcontainer.RabbitTestContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Consumidor do comando "liberar reservas da OS" contra um RabbitMQ de verdade e o MySQL de teste: efeito no
 * saldo, idempotência diante de duplicata, retry de falha temporária e DLQ para o que não tem conserto.
 */
@ActiveProfiles("integration-test")
@Import(TokenTestHelper.class)
@TestPropertySource(properties = {
        "estoque.mensageria.listener.enabled=true",
        "spring.rabbitmq.listener.simple.retry.max-retries=2",
        "spring.rabbitmq.listener.simple.retry.initial-interval=50ms",
        "spring.rabbitmq.listener.simple.retry.max-interval=100ms",
        "management.tracing.sampling.probability=1.0"
})
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LiberarReservaListenerIntegrationTest extends AbstractContainer {

    private static final RabbitTestContainer rabbit = new RabbitTestContainer();

    static {
        rabbit.start();
    }

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.virtual-host", () -> "/");
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    private static final Long ORDEM_ID = 7L;
    private static final String URL = "/reserva/ordem-servico/" + ORDEM_ID;

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private TokenTestHelper tokenTestHelper;
    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private AmqpAdmin amqpAdmin;
    @MockitoSpyBean private ReservaGateway reservaGateway;

    @Value("${estoque.mensageria.exchange}") private String exchange;
    @Value("${estoque.mensageria.fila.liberar-reserva}") private String fila;
    @Value("${estoque.mensageria.routing-key.liberar-reserva}") private String routingKey;

    @LocalServerPort
    private int port;

    private static final String FILA_RESPOSTAS = "teste.respostas-da-os";

    @Value("${ordemservico.mensageria.exchange}") private String exchangeRespostas;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        // Faz o papel da fila ordemservico.respostas, que em producao e declarada pelo fiap-mecanica: sem ela a resposta
        // do estoque volta como nao roteada.
        Queue respostas = new Queue(FILA_RESPOSTAS, false);
        amqpAdmin.declareQueue(respostas);
        amqpAdmin.purgeQueue(FILA_RESPOSTAS, true);
        DirectExchange exchangeDaOs = new DirectExchange(exchangeRespostas, true, false);
        amqpAdmin.declareBinding(BindingBuilder.bind(respostas).to(exchangeDaOs).with("reserva.liberada"));
        amqpAdmin.purgeQueue(fila, true);
        amqpAdmin.purgeQueue(fila + ".dlq", true);
        jdbcTemplate.update("INSERT INTO peca (id, nome, descricao, preco, quantidade_estoque, quantidade_reservada) VALUES (1, 'Filtro', 'desc', 10.00, 10, 0)");
        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenTestHelper.servicoReserva())
                .body("{\"itens\":[{\"tipoItem\":\"PECA\",\"itemId\":1,\"quantidadeTotal\":3}]}")
                .when().put(URL + "/itens").then().statusCode(204);
        assertEquals(3, pecaReservada());
        // a reserva do setup também passa pelo gateway; só interessam as chamadas feitas pelo consumidor
        Mockito.clearInvocations(reservaGateway);
    }

    @AfterEach
    void limpar() {
        amqpAdmin.deleteQueue(FILA_RESPOSTAS);
        amqpAdmin.purgeQueue(fila, true);
        amqpAdmin.purgeQueue(fila + ".dlq", true);
    }

    private int pecaReservada() {
        return jdbcTemplate.queryForObject("SELECT quantidade_reservada FROM peca WHERE id = 1", Integer.class);
    }

    private int pecaEstoque() {
        return jdbcTemplate.queryForObject("SELECT quantidade_estoque FROM peca WHERE id = 1", Integer.class);
    }

    private List<String> statusReservas() {
        return jdbcTemplate.queryForList("SELECT status FROM reserva_estoque WHERE ordem_servico_id = ?", String.class, ORDEM_ID);
    }

    private void publicar(String json) {
        Message mensagem = MessageBuilder.withBody(json.getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();
        rabbitTemplate.send(exchange, routingKey, mensagem);
    }

    private Message receberDaDlq() {
        return rabbitTemplate.receive(fila + ".dlq", Duration.ofSeconds(10).toMillis());
    }

    @Test
    void shouldLiberarAsReservasDaOrdemAoReceberOComando() {
        publicar("{\"ordemServicoId\":7}");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertEquals(0, pecaReservada()));
        assertEquals(List.of("LIBERADA"), statusReservas());
        assertEquals(10, pecaEstoque());
    }

    @Test
    void shouldResponderReservaLiberadaAoOrquestradorDepoisDeLiberar() {
        publicar("{\"ordemServicoId\":7}");

        Message resposta = rabbitTemplate.receive(FILA_RESPOSTAS, Duration.ofSeconds(10).toMillis());
        assertNotNull(resposta, "o estoque deveria responder ReservaLiberada");
        assertEquals("{\"ordemServicoId\":7}", new String(resposta.getBody(), StandardCharsets.UTF_8));
        assertEquals("RESERVA_LIBERADA", resposta.getMessageProperties().getType());
        assertEquals(0, pecaReservada(), "a resposta so sai depois de liberar");
    }

    @Test
    void shouldResponderDeNovoQuandoOComandoChegaEmDuplicata() {
        publicar("{\"ordemServicoId\":7}");
        publicar("{\"ordemServicoId\":7}");

        // A duplicata nao altera o saldo, mas a resposta anterior pode ter se perdido: sai uma por comando.
        assertNotNull(rabbitTemplate.receive(FILA_RESPOSTAS, Duration.ofSeconds(10).toMillis()));
        assertNotNull(rabbitTemplate.receive(FILA_RESPOSTAS, Duration.ofSeconds(10).toMillis()));
        assertEquals(10, pecaEstoque());
    }

    @Test
    void shouldReprocessarQuandoARespostaNaoPodeSerEntregue() {
        amqpAdmin.deleteQueue(FILA_RESPOSTAS); // a OS ainda nao declarou a fila: resposta nao roteada

        publicar("{\"ordemServicoId\":7}");

        Message naDlq = receberDaDlq();
        assertNotNull(naDlq, "sem fila de respostas o comando esgota as tentativas e vai para a DLQ");
        assertEquals(0, pecaReservada(), "a liberacao em si e idempotente e ja foi aplicada");
    }

    @Test
    void shouldSerIdempotenteQuandoOMesmoComandoChegaDuasVezes() {
        publicar("{\"ordemServicoId\":7}");
        publicar("{\"ordemServicoId\":7}");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertEquals(0, pecaReservada()));
        // dá tempo de a segunda mensagem ser consumida e confirmar que nada mudou
        await().pollDelay(Duration.ofMillis(500)).atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertEquals(0, pecaReservada());
            assertEquals(10, pecaEstoque());
            assertEquals(List.of("LIBERADA"), statusReservas());
        });
    }

    @Test
    void shouldNaoAlterarNadaParaOrdemSemReservas() {
        publicar("{\"ordemServicoId\":999}");

        await().pollDelay(Duration.ofMillis(500)).atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertEquals(3, pecaReservada());
            assertEquals(List.of("ATIVA"), statusReservas());
        });
        assertNull(rabbitTemplate.receive(fila + ".dlq", 300));
    }

    @Test
    void shouldTentarDeNovoQuandoAFalhaForTemporaria() {
        Mockito.doThrow(new IllegalStateException("banco indisponivel")).doCallRealMethod()
                .when(reservaGateway).listarAtivasPorOrdemDeServico(ORDEM_ID);

        publicar("{\"ordemServicoId\":7}");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertEquals(0, pecaReservada()));
        assertEquals(List.of("LIBERADA"), statusReservas());
        assertNull(rabbitTemplate.receive(fila + ".dlq", 300));
    }

    @Test
    void shouldEnviarParaADlqQuandoAsTentativasSeEsgotam() {
        Mockito.doThrow(new IllegalStateException("banco indisponivel"))
                .when(reservaGateway).listarAtivasPorOrdemDeServico(ORDEM_ID);

        publicar("{\"ordemServicoId\":7}");

        Message naDlq = receberDaDlq();
        assertNotNull(naDlq, "após esgotar as tentativas a mensagem deveria estar na DLQ");
        assertThat(new String(naDlq.getBody(), StandardCharsets.UTF_8)).contains("\"ordemServicoId\":7");
        Mockito.verify(reservaGateway, Mockito.times(3)).listarAtivasPorOrdemDeServico(ORDEM_ID);
        assertEquals(3, pecaReservada());
        assertEquals(List.of("ATIVA"), statusReservas());
    }

    @Test
    void shouldContinuarOTraceRecebidoNaMensagem(CapturedOutput output) {
        String traceId = "4bf92f3577b34da6a3ce929d0e0e4736";
        Message mensagem = MessageBuilder.withBody("{\"ordemServicoId\":7}".getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("traceparent", "00-" + traceId + "-00f067aa0ba902b7-01")
                .build();

        // Template sem observacao: o do contexto injeta o proprio traceparent e sobrescreveria o da mensagem, que aqui
        // faz o papel do relay da outbox do fiap-mecanica (publicador que nao usa observacao no template).
        new RabbitTemplate(rabbitTemplate.getConnectionFactory()).send(exchange, routingKey, mensagem);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertEquals(0, pecaReservada()));
        // O log "Liberando reservas ..." sai dentro do span do consumidor: carrega o trace id da mensagem, então o
        // consumo aparece no MESMO trace do cancelamento que o originou (e não como um trace separado).
        assertThat(output.getAll()).contains(traceId);
    }

    @Test
    void shouldEnviarParaADlqSemRetryQuandoOComandoNaoTemOrdemDeServico() {
        publicar("{\"outroCampo\":1}");

        assertNotNull(receberDaDlq());
        Mockito.verify(reservaGateway, Mockito.never()).listarAtivasPorOrdemDeServico(Mockito.any());
    }

    @Test
    void shouldEnviarParaADlqQuandoOCorpoNaoEJson() {
        publicar("isto nao e json");

        assertNotNull(receberDaDlq());
        Mockito.verify(reservaGateway, Mockito.never()).listarAtivasPorOrdemDeServico(Mockito.any());
    }
}
