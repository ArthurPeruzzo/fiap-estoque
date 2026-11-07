# fiap-estoque

Microsserviço de **estoque** (peças, insumos e reservas por ordem de serviço) da Mecânica FIAP.
Extraído do módulo `estoque` do monólito [`fiap-mecanica`](https://github.com/ArthurPeruzzo/fiap-mecanica).

> **Status:** passo 2 de 5 da extração. Além do CRUD de peças e insumos (`/peca`, `/insumo`), o serviço
> expõe a API de reservas usada pela ordem de serviço (`/reserva/**`), que o monólito já consome via
> REST. A API de catálogo (consulta de preço por ids) ainda não foi exposta: o monólito segue lendo o
> catálogo do módulo embutido.

## Stack

Java 25 · Spring Boot 4.0.5 · Spring Web MVC · Spring Security · Spring Data JPA · MySQL + Flyway ·
Lombok · Auth0 `java-jwt` · SpringDoc OpenAPI · Maven wrapper (`./mvnw`).

## Arquitetura

Clean Architecture, igual ao monólito: `core/` (domínio, casos de uso, portas) sem Spring/JPA, e
`infra/` (controllers, gateways de banco, entidades JPA). Pacote raiz `com.fiap.estoque`.

- `core/domain`: `ItemEstoque` (abstrata), `Peca`, `Insumo`, `Reserva`, `ReservaDeEstoque` (domain service).
- `core/gateway`: `PecaGateway`, `InsumoGateway`, `ItemEstoqueGateway`, `ReservaGateway`.
- `core/usecase`: CRUD de peça/insumo e `ReservarItemEstoque`, `ConfirmarReservasOrdemDeServico`, `LiberarReservasOrdemDeServico`.
- `infra/controller`: pares `XxxCleanController` + `XxxHttpController`.
- `shared`: exceções, paginação, OpenAPI e segurança stateless.

## Autenticação

O serviço **não tem tabela de usuários**. Valida o JWT emitido pela API principal (mesmo
`JWT_SECRET` e issuer `mecanica-fiap`) e usa a claim `roles`:

- `/peca/**` e `/insumo/**` exigem `ROLE_ADMINISTRADOR` (JWT de usuário).
- `/reserva/**` e `/catalogo/**` são APIs internas e exigem um **token de serviço** emitido pelo `fiap-mecanica`
  (ver "Autenticação serviço a serviço"). O JWT de usuário não vale nelas, e o token de serviço não vale nas
  demais rotas.
- `/actuator/health/**` e o Swagger são públicos.
- Qualquer outra rota é negada.

### Autenticação serviço a serviço

O `fiap-mecanica` gera um JWT novo a cada chamada ao estoque (sem estado e sem chamada a servidor de
autenticação). Ele é assinado com um segredo próprio, `SERVICE_JWT_SECRET`, **diferente** do `JWT_SECRET` dos
usuários, para que um não possa forjar o outro.

| Claim | Valor | Validação aqui |
|---|---|---|
| `iss` | `mecanica-fiap-servicos` | obrigatória e igual |
| `aud` | `fiap-estoque` | obrigatória e igual (um token de outro destino é recusado) |
| `sub` | `svc:fiap-mecanica` | identifica o chamador (log/auditoria) |
| `scope` | `reserva:escrita` ou `catalogo:leitura`, separados por espaço | `/reserva/**` exige `reserva:escrita`; `/catalogo/**` exige `catalogo:leitura` |
| `iat`, `exp`, `jti` | agora, agora + 90 s, UUID | `exp` com tolerância de relógio de 10 s |

Respostas: sem token ou token inválido/expirado/de outro destino → **401**; token válido sem o escopo da
rota → **403**.

Limites conhecidos: o token trafega sem TLS dentro do cluster (a janela de reuso de um token capturado é de
90 s, e `confirmar`/`liberar`/`reservar` são idempotentes), e o HS256 é simétrico (quem valida também poderia
assinar). O próximo passo, quando houver mais serviços, é assinatura assimétrica ou mTLS no mesh.

## API de reservas (interna)

| Método e rota | Descrição |
|---|---|
| `PUT /reserva/ordem-servico/{id}/itens` | Define, em lote (peças e insumos), a quantidade **total** reservada por item. Idempotente e atômico: se um item não tem saldo (422) ou não existe (404), nenhum é reservado. |
| `POST /reserva/ordem-servico/{id}/confirmar` | Converte as reservas ativas em baixa definitiva. |
| `POST /reserva/ordem-servico/{id}/liberar` | Devolve ao saldo tudo que estava reservado (usado também como compensação). |

Corpo do `PUT`: `{"itens":[{"tipoItem":"PECA","itemId":5,"quantidadeTotal":3}]}`. Cada operação roda numa única
transação (`@Transactional` no controller).

### Comandos assíncronos e respostas da saga (RabbitMQ)

O estoque **consome** dois comandos do `fiap-mecanica` (orquestrador da saga de pagamento, ver o
[ADR-0009](../fiap-mecanica/docs/adr/0009-saga-de-pagamento-orquestrada.md) do monólito) e **responde** a cada um no
exchange da OS:

| Comando (exchange `estoque.comandos`, direct) | Chave | Fila (DLQ `<fila>.dlq`, DLX `estoque.comandos.dlx`) | Efeito | Resposta (exchange `ordemservico.eventos`) |
|---|---|---|---|---|
| Liberar reservas (OS cancelada) | `reserva.liberar` | `estoque.liberar-reserva` | mesmo caso de uso do `POST .../liberar` | `reserva.liberada` |
| Confirmar baixa (pagamento aprovado) | `reserva.confirmar-baixa` | `estoque.confirmar-baixa` | mesmo caso de uso do `POST .../confirmar` | `baixa.confirmada` |

Corpo de comandos e respostas: `{"ordemServicoId": 7}`. Os listeners (`LiberarReservaListener` e
`ConfirmarBaixaListener`) executam o caso de uso e só então publicam a resposta, esperando a confirmação do broker. A
entrega é *pelo menos uma vez* e as duas ações só agem em reservas `ATIVA`, então duplicata ou OS sem reservas não altera o
saldo; **a resposta é reenviada mesmo assim**, porque a anterior pode ter se perdido. Não há outbox aqui: se a resposta
falhar (broker fora, ou a OS ainda não declarou a fila `ordemservico.respostas`), o comando é reprocessado. Falha ao
processar tem 3 retries (1 s, 2 s, 4 s) e depois a mensagem vai para a DLQ; corpo malformado vai direto para a DLQ, sem
retry. Mensagens na DLQ pedem investigação manual (painel do RabbitMQ em `http://localhost:15672`). O trace do comando
continua na resposta (`traceparent`). O usuário deste serviço no broker enxerga os recursos `estoque.*` e publica em
`ordemservico.eventos`.

## API de catálogo (interna)

Consulta em lote usada pela ordem de serviço para validar a existência e obter o preço de peças e insumos.

| Método e rota | Descrição |
|---|---|
| `GET /catalogo/pecas?ids=1,2,3` | Retorna `[{"id":1,"preco":89.90}]`. Ids inexistentes são omitidos; quem consulta decide o que fazer com a diferença. |
| `GET /catalogo/insumos?ids=1,2,3` | Idem, para insumos. |

Sem o parâmetro `ids` a resposta é 400.

### Exposição e isolamento de rede

- O Service é `LoadBalancer` **só** para que o API Gateway alcance `/peca/**` e `/insumo/**` (administrador). O
  `fiap-mecanica` não usa o ELB: chama `http://fiap-estoque.fiap-estoque.svc.cluster.local`.
- Como o ELB é público, `/reserva/**` e `/catalogo/**` também ficam alcançáveis diretamente; o que as protege é o token de
  serviço (sem `aud`/`scope` corretos: 401/403). A "entrada única" é o API Gateway por convenção. Impô-la exigiria NLB
  interno + VPC Link (evolução registrada no [ADR-0001](docs/adr/0001-infraestrutura-propria-do-servico.md)).
- Uma `NetworkPolicy` não foi aplicada: restringir a entrada aos pods do `fiap-mecanica` também bloquearia o tráfego
  do ELB, e o VPC CNI do cluster não tem a política de rede habilitada.
- No docker-compose local a porta 8081 fica publicada na máquina, o que é aceitável só em desenvolvimento.

## Configuração

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:mysql://127.0.0.1:3308/estoque` | JDBC URL |
| `DB_USERNAME` | `root` | Usuário do banco |
| `DB_PASSWORD` | `root` | Senha do banco |
| `JWT_SECRET` | — | Base64URL, **o mesmo da API principal** (JWT de usuário) |
| `SERVICE_JWT_SECRET` | — | Base64URL (≥ 32 bytes), **o mesmo do `fiap-mecanica`** e diferente do `JWT_SECRET` (token de serviço) |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | — | Usuário deste serviço no broker (obrigatório); o mesmo par de `RABBITMQ_ESTOQUE_*` no `.env` do `fiap-mecanica`, que sobe o RabbitMQ |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` / `RABBITMQ_VHOST` | `localhost` / `5672` / `fiap` | Localização do broker |

## Infraestrutura e deploy

Este repositório é dono de tudo o que é específico do estoque (decisões no [ADR-0001](docs/adr/0001-infraestrutura-propria-do-servico.md)):

| Onde | O quê |
|---|---|
| `infra/terraform/app-infra` | Repositório ECR `fiap-estoque` e instância RDS MySQL 8.4 **própria** (`fiap-estoque-db`, banco `estoque`). State `tfstate/estoque.tfstate`. |
| `k8s/` | Namespace `fiap-estoque`, Deployment, Service `LoadBalancer`, HPA (1–3 réplicas, CPU 70%). ConfigMap e Secret são gerados pelo CD. |
| `k8s/rabbitmq/` | RabbitMQ no cluster (namespace `mensageria`): StatefulSet + disco EBS gp3, Services `ClusterIP` e Job que cria o vhost `fiap` e os usuários de menor privilégio. |
| `.github/workflows` | `build-test` (testes + cobertura mínima de 80%), `ci` (pull request) e `cd` (merge na `main`). |

**Compartilhado, fora deste repositório:** VPC, subnets e o cluster EKS (`fiap-mecanica-infra-k8s`). Numa conta zerada o CD dele
roda primeiro; este módulo lê o state dele (`tfstate/terraform.tfstate`).

### Ordem de deploy numa conta zerada

O CD do estoque depende **só do cluster** (lê o state do `fiap-mecanica-infra-k8s`); não depende do banco nem do código do
`fiap-mecanica`. Por isso ele roda **antes** do monólito, e o monólito sobe numa passada só, já encontrando o Service do
estoque (para as rotas `/peca` e `/insumo` do gateway):

```
1º  fiap-mecanica-infra-k8s   (bucket, VPC, cluster)
2º  fiap-estoque              (RDS próprio, RabbitMQ, estoque)   ┐ independentes entre si:
    fiap-mecanica-infra-db    (RDS do monólito)                  │ podem rodar em paralelo
    fiap-mecanica-lambda                                         ┘
3º  fiap-mecanica             (cria o gateway já com as rotas do estoque)
```

### Pipelines

- **CI (pull request):** build, testes e `jacoco:check` (**falha abaixo de 80%** das linhas), build da imagem Docker e
  `terraform fmt/validate/plan`.
- **CD (`main`):** `build-test` → `app-infra-apply` (ECR + RDS) → `docker-build-push` (tag = versão do `pom.xml`; não
  sobrescreve tag já publicada, então **suba a versão a cada merge**) → `rabbitmq-deploy` → `k8s-deploy`.
- A cobertura aparece no resumo de cada execução e o relatório HTML é publicado como artefato (`jacoco-report`).

### GitHub Secrets do repositório

| Secret | Uso |
|---|---|
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` | Credenciais da Academy Lab (expiram; `scripts/refresh-aws-secrets.sh` as republica). |
| `TF_VAR_DB_USERNAME`, `TF_VAR_DB_PASSWORD` | Usuário/senha master do RDS **do estoque** (credencial própria, diferente da do `fiap-mecanica`). |
| `JWT_SECRET` | O mesmo do `fiap-mecanica` (JWT de usuário). |
| `SERVICE_JWT_SECRET` | O mesmo do `fiap-mecanica`, diferente do `JWT_SECRET` (token de serviço). |
| `NEW_RELIC_LICENSE_KEY` | A mesma do `fiap-mecanica` (traces e métricas OTLP). |
| `RABBITMQ_ADMIN_PASSWORD` | Administrador do broker. |
| `RABBITMQ_ESTOQUE_PASSWORD` | Senha do usuário `estoque` no broker (este serviço). |
| `RABBITMQ_MECANICA_PASSWORD` | Senha do usuário `mecanica` no broker; o Job a cria e o `fiap-mecanica` precisa do mesmo valor. |
| `RABBITMQ_COBRANCA_PASSWORD` | Senha do usuário `cobranca` no broker (opcional: sem ela o usuário não é criado); o `fiap-cobranca` precisa do mesmo valor. |

Use valores **alfanuméricos** nas senhas do RabbitMQ (viram JSON no Job de inicialização). A proteção da `main` (PR obrigatório e
checagens) é configurada no GitHub.

### Pontos de atenção

- **Disco do broker (EBS CSI) e o hop limit do IMDS:** o driver roda em pods e obtém as credenciais da `LabRole` pelo serviço
  de metadados (IMDS) da instância. Os nós do cluster nascem com `HttpPutResponseHopLimit = 1` e IMDSv2 obrigatório; um pod
  fica um salto além da instância, então o controller não alcança o IMDS (`no EC2 IMDS role found ... context deadline
  exceeded`), entra em `CrashLoopBackOff` e o add-on fica preso em `CREATING`. O CD ajusta o hop limit para **2** antes de
  instalar o add-on (passo idempotente, reaplicado a cada deploy); com isso o controller sobe e o add-on fica `ACTIVE`. Vale
  enquanto os nós existirem; a correção definitiva é um launch template com hop limit 2 no node group do
  `fiap-mecanica-infra-k8s`. Plano B, se o disco não funcionar: RabbitMQ numa EC2 com disco EBS. Sem disco persistente, a
  queda do broker perderia mensagens já marcadas como enviadas pela outbox do `fiap-mecanica`.
- **Sem SonarQube** (nem outra análise estática) ainda.
- **Integração com o `fiap-mecanica`:** feita no CD dele (`ESTOQUE_URL` interno, `RABBITMQ_*` e as rotas `/peca` e `/insumo` do
  API Gateway). O monólito deve ser o **último** a subir, para encontrar o Service do estoque na primeira passada.

## Observabilidade

Mesma configuração do `fiap-mecanica` (Fase 3): traces e métricas exportados por OTLP para a New Relic, com o serviço
aparecendo como `estoque`. A chave vem de `NEW_RELIC_LICENSE_KEY` (Secret no cluster; nunca no código) e os logs ECS
carregam `trace.id`/`span.id`, correlacionando logs e traces.

- **Propagação:** o serviço continua o trace recebido no cabeçalho W3C `traceparent` (teste
  `ContinuaTraceRecebidoIntegrationTest`). O `fiap-mecanica` envia esse cabeçalho nas chamadas HTTP de catálogo e reserva.
- **Mensageria:** o trace atravessa a outbox e o RabbitMQ. O relay do `fiap-mecanica` coloca o `traceparent` da requisição de
  cancelamento no cabeçalho da mensagem e o listener (`spring.rabbitmq.listener.simple.observation-enabled`) abre o span do
  consumo como filho dele: cancelar a OS e liberar a reserva no estoque aparecem no **mesmo trace** (teste
  `LiberarReservaListenerIntegrationTest#shouldContinuarOTraceRecebidoNaMensagem`).
- **Local:** sem `NEW_RELIC_LICENSE_KEY` a exportação falha com autenticação recusada (apenas avisos nos logs); defina a
  chave ou ignore os avisos. Nos testes a exportação fica desligada.

## Executar

```bash
cp .env.example .env        # ajuste JWT_SECRET, SERVICE_JWT_SECRET e RABBITMQ_*
docker compose up --build   # MySQL em 127.0.0.1:3308, serviço em http://localhost:8081
```

Swagger: `http://localhost:8081/swagger-ui.html`.

Local (sem Docker para o serviço), com o MySQL do compose no ar:

```bash
JWT_SECRET=<mesmo-segredo> SERVICE_JWT_SECRET=<segredo-de-servico> ./mvnw spring-boot:run
```

## Testes

O build exige **80% de cobertura de linhas** (`./mvnw verify` aplica o `jacoco:check`; hoje ~97%). Relatório em `target/site/jacoco/index.html`.

```bash
JAVA_HOME=/home/arthur/.jdks/jdk-25 ./mvnw test                         # tudo (integração exige Docker)
JAVA_HOME=/home/arthur/.jdks/jdk-25 ./mvnw test -Dtest='!*IntegrationTest'  # só unitários e contrato
```

Os testes de integração usam Testcontainers (MySQL) e geram o JWT direto (`TokenTestHelper`).

## Migrations

`src/main/resources/db/migration`: `V1` peça, `V2` insumo, `V3` reserva_estoque. O seed de dados
iniciais entra no passo 3 da extração, junto com a divisão do seed do monólito.
# fiap-estoque
