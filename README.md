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

- `/peca/**` e `/insumo/**` exigem `ROLE_ADMINISTRADOR`.
- `/reserva/**` e `/catalogo/**` **não exigem autenticação**. São APIs internas, chamadas só pelo `fiap-mecanica`
  (inclusive na aprovação/recusa de orçamento por link, que não tem usuário autenticado). A proteção é de rede, não de
  aplicação: ver "Premissa de rede" abaixo.
- `/actuator/health/**` e o Swagger são públicos.
- Qualquer outra rota é negada.

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

### Premissa de rede (`/reserva/**` e `/catalogo/**`)

Como o endpoint não tem autenticação, qualquer cliente que o alcance pode reservar, confirmar ou liberar estoque
de qualquer OS (os ids são sequenciais). Por isso ele **não pode ser exposto fora do cluster**:

- no k8s, o Service do estoque deve ser `ClusterIP`, sem rota no API Gateway nem Ingress para `/reserva/**`;
- opcionalmente, uma `NetworkPolicy` liberando só os pods do `fiap-mecanica`;
- no docker-compose local a porta 8081 fica publicada na máquina, o que é aceitável só em desenvolvimento.

Pendente (a implementar depois): autenticação serviço a serviço (ex.: JWT com role de serviço) caso o isolamento
de rede deixe de bastar.

## Configuração

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:mysql://127.0.0.1:3308/estoque` | JDBC URL |
| `DB_USERNAME` | `root` | Usuário do banco |
| `DB_PASSWORD` | `root` | Senha do banco |
| `JWT_SECRET` | — | Base64URL, **o mesmo da API principal** |

## Executar

```bash
cp .env.example .env        # ajuste JWT_SECRET
docker compose up --build   # MySQL em 127.0.0.1:3308, serviço em http://localhost:8081
```

Swagger: `http://localhost:8081/swagger-ui.html`.

Local (sem Docker para o serviço), com o MySQL do compose no ar:

```bash
JWT_SECRET=<mesmo-segredo> ./mvnw spring-boot:run
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
