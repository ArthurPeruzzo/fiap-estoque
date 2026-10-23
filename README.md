# fiap-estoque

Microsserviço de **estoque** (peças, insumos e reservas por ordem de serviço) da Mecânica FIAP.
Extraído do módulo `estoque` do monólito [`fiap-mecanica`](https://github.com/ArthurPeruzzo/fiap-mecanica).

> **Status:** passo 1 de 5 da extração. Este serviço já expõe o CRUD de peças e insumos
> (`/peca`, `/insumo`) e traz o domínio de reservas, mas a API interna usada pela ordem de
> serviço (catálogo e reservas) ainda não foi exposta e a API principal ainda usa o módulo
> embutido.

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
- `/actuator/health/**` e o Swagger são públicos.
- Qualquer outra rota é negada.

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
