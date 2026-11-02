package com.fiap.estoque.infra.mensageria;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class CredenciaisDoBrokerObrigatoriasUnitTest {

    // PropertyPlaceholderAutoConfiguration registra o resolvedor estrito de placeholders, como na aplicação real
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
            .withUserConfiguration(CredenciaisDoBrokerObrigatorias.class);

    @Test
    void shouldFalharNaPartidaQuandoOUsuarioNaoFoiDefinido() {
        runner.withPropertyValues(
                        "spring.rabbitmq.username=${USUARIO_DO_BROKER_AUSENTE_NO_TESTE}",
                        "spring.rabbitmq.password=senha")
                .run(contexto -> {
                    assertNotNull(contexto.getStartupFailure());
                    assertTrue(causas(contexto.getStartupFailure()).contains("USUARIO_DO_BROKER_AUSENTE_NO_TESTE"));
                });
    }

    @Test
    void shouldFalharNaPartidaQuandoASenhaNaoFoiDefinida() {
        runner.withPropertyValues(
                        "spring.rabbitmq.username=mecanica",
                        "spring.rabbitmq.password=${SENHA_DO_BROKER_AUSENTE_NO_TESTE}")
                .run(contexto -> {
                    assertNotNull(contexto.getStartupFailure());
                    assertTrue(causas(contexto.getStartupFailure()).contains("SENHA_DO_BROKER_AUSENTE_NO_TESTE"));
                });
    }

    @Test
    void shouldSubirQuandoAsCredenciaisForemDefinidas() {
        runner.withPropertyValues("spring.rabbitmq.username=mecanica", "spring.rabbitmq.password=senha")
                .run(contexto -> assertNull(contexto.getStartupFailure()));
    }

    private static String causas(Throwable erro) {
        StringBuilder texto = new StringBuilder();
        for (Throwable t = erro; t != null; t = t.getCause()) {
            texto.append(t.getMessage()).append(' ');
        }
        return texto.toString();
    }
}
