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

## API de catálogo (interna)

Consulta em lote usada pela ordem de serviço para validar a existência e obter o preço de peças e insumos.

| Método e rota | Descrição |
|---|---|
| `GET /catalogo/pecas?ids=1,2,3` | Retorna `[{"id":1,"preco":89.90}]`. Ids inexistentes são omitidos; quem consulta decide o que fazer com a diferença. |
| `GET /catalogo/insumos?ids=1,2,3` | Idem, para insumos. |

Sem o parâmetro `ids` a resposta é 400.

### Isolamento de rede (defesa em profundidade)

A autenticação acima é a proteção principal, mas as rotas internas continuam **fora do alcance externo**:

- no k8s, o Service do estoque deve ser `ClusterIP`, sem rota no API Gateway nem Ingress para `/reserva/**` e
  `/catalogo/**`;
- opcionalmente, uma `NetworkPolicy` liberando só os pods do `fiap-mecanica`;
- no docker-compose local a porta 8081 fica publicada na máquina, o que é aceitável só em desenvolvimento.

## Configuração

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:mysql://127.0.0.1:3308/estoque` | JDBC URL |
| `DB_USERNAME` | `root` | Usuário do banco |
| `DB_PASSWORD` | `root` | Senha do banco |
| `JWT_SECRET` | — | Base64URL, **o mesmo da API principal** (JWT de usuário) |
| `SERVICE_JWT_SECRET` | — | Base64URL (≥ 32 bytes), **o mesmo do `fiap-mecanica`** e diferente do `JWT_SECRET` (token de serviço) |

## Executar

```bash
cp .env.example .env        # ajuste JWT_SECRET e SERVICE_JWT_SECRET
docker compose up --build   # MySQL em 127.0.0.1:3308, serviço em http://localhost:8081
```

Swagger: `http://localhost:8081/swagger-ui.html`.

Local (sem Docker para o serviço), com o MySQL do compose no ar:

```bash
JWT_SECRET=<mesmo-segredo> SERVICE_JWT_SECRET=<segredo-de-servico> ./mvnw spring-boot:run
```

## Testes

```bash
JAVA_HOME=/home/arthur/.jdks/jdk-25 ./mvnw test                         # tudo (integração exige Docker)
JAVA_HOME=/home/arthur/.jdks/jdk-25 ./mvnw test -Dtest='!*IntegrationTest'  # só unitários e contrato
```

Os testes de integração usam Testcontainers (MySQL) e geram o JWT direto (`TokenTestHelper`).

## Migrations

`src/main/resources/db/migration`: `V1` peça, `V2` insumo, `V3` reserva_estoque. O seed de dados
iniciais entra no passo 3 da extração, junto com a divisão do seed do monólito.
# fiap-estoque
