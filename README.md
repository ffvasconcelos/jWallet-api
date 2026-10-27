# jWallet API

An educational wallet and transfer API using fictitious BRL amounts. This project is intended for Java and Spring Boot practice and does not process real financial transactions.

## Technologies

- Java 21
- Spring Boot 4.1.1
- Maven Wrapper
- Spring Web MVC and Bean Validation
- Spring Data JPA / Hibernate
- PostgreSQL 17 (Docker Compose)
- Liquibase 5.0.4 with Hibernate 7 diff generation

## Initial scope

- Create a wallet with an initial fictitious BRL balance.
- Retrieve a wallet and its balance.
- Create a transfer between two wallets.
- Retrieve a transfer.
- List a wallet's transfers with pagination.

PostgreSQL connectivity, Liquibase migrations, JPA repositories, and transactional transfers are configured. Persistence and rollback after a flushed write are verified by integration tests. Idempotency and concurrency control are pending.

## HTTP endpoints

| Method | Path | Successful response |
|---|---|---|
| GET | `/wallets` | `200 OK`, array of wallet DTOs |
| POST | `/wallets` | `201 Created`, wallet JSON and `Location: /wallets/{id}` |
| GET | `/wallets/{id}` | `200 OK`, wallet JSON |
| GET | `/transfers` | `200 OK`, array of transfer DTOs |
| POST | `/transfers` | `201 Created`, transfer JSON and `Location: /transfers/{id}` |
| GET | `/transfers/{id}` | `200 OK`, transfer JSON |

Create a wallet with a server-generated UUID and a fictitious initial balance:

```bash
curl -i http://localhost:8080/wallets \
  -H 'Content-Type: application/json' \
  -d '{"balance":100.00}'
```

The `balance` field is required, must be non-negative, and supports at most
17 integer digits and two decimal places. Invalid balances return `400` with
ProblemDetail and `errors.balance`. Zero is allowed. The POST endpoint always
creates a new UUID, rather than accepting an existing wallet ID for an update.
The existing `GET /wallets/{id}` returns `404` for a missing wallet.

Retrieve a transfer using the UUID returned by `POST /transfers`:

```bash
curl -i http://localhost:8080/transfers/UUID_FROM_POST_RESPONSE
```

The transfer response contains `id`, `sourceWalletId`, `targetWalletId`, `amount`,
`status`, and `createdAt`. A missing transfer returns `404` with ProblemDetail;
a malformed UUID returns `400`. Wallet associations are fetched for transfer
lookup so response mapping works with Open EntityManager in View disabled.
List wallets or transfers:

```bash
curl -i http://localhost:8080/wallets
curl -i http://localhost:8080/transfers
```

Collection responses are JSON arrays of the same DTOs used by individual GETs;
empty collections return `[]` with `200`. Both listings currently return all
records without pagination or a guaranteed order. Transfer listing fetches the
source and target wallets using EntityGraph so mapping works after the repository
session closes. Wallet history pagination is still pending.

## Business rules

1. The amount must be positive and have no more than two decimal places.
2. Source and target wallets must exist and be different.
3. The source wallet must have sufficient balance.
4. An invalid request must not change any balance.
5. Once persistence is introduced, debiting the source, crediting the target, and recording the transfer must happen as one unit of work.

## Roadmap

### Day 2 — Persistence and consistency

- Persist wallets and transfers in PostgreSQL with JPA and Liquibase migrations.
- Make balance updates transactional.
- Add idempotency and concurrency protection.

### Day 3 — Delivery quality

- Add HTTP and PostgreSQL integration tests.
- Provide Docker Compose and a GitHub Actions workflow.
- Complete usage documentation and architectural decisions.

### Day 4 — Optional improvements

- Add operational visibility with Actuator and structured logs.
- Explore a small asynchronous notification extension only if the core is complete.

## Running locally

Start Docker Desktop. On a fresh clone, prepare the local environment file:

```bash
cp .env.example .env
```

Set `DB_PASSWORD` in `.env` to a local password. The file is ignored by Git;
`.env.example` documents the required variables. Keep the file in simple
`KEY=value` format, without `export` or shell expressions.

From the project root, start PostgreSQL and the application:

```bash
docker compose up -d --wait postgres
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

The database name, username, password, and host port come from `.env`.
Docker Compose loads this file automatically; Spring imports it explicitly as
a properties file. In the IDE, use the project root as the working directory.
The default host is `localhost` and the default port is `5432`.
Environment variables can override the file; `DB_URL` can override the complete
application JDBC URL.

PostgreSQL 17 stores its data in a named volume mounted at `/var/lib/postgresql/data`.
The exposed port is bound to loopback. Initialization variables affect only an
empty data volume; changing `.env` does not change existing database credentials.

Check health, connect to the database, or stop it while preserving its data:

```bash
docker compose ps
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
docker compose stop postgres
```

Inside `psql`, use `\conninfo` to inspect the connection, `\dt` to list tables,
and `\q` to exit.

Liquibase applies versioned changelogs before Hibernate validates the schema
(`ddl-auto=validate`). The database contains `wallets`, `transfers`, and Liquibase's
`databasechangelog` and `databasechangeloglock` tracking tables.
Services use JPA repositories. The `demo` profile creates sample wallets only
when their identifiers are absent, preserving existing balances. Transfer
creation timestamps are generated by Hibernate on persistence.

## Generating migrations from entities

Flyway was replaced with Liquibase so generation works through Maven without
an IntelliJ Ultimate subscription. The `db-migrations` Maven profile loads the
local `.env` in the `initialize` phase and exposes the Hibernate extension only
to the build plugin, not to the running application.

1. Start PostgreSQL and ensure existing migrations have been applied.
2. Change the JPA entities.
3. Generate a new candidate changelog using a new output filename:

```bash
./mvnw -Pdb-migrations compile liquibase:diff \
  -Dmigration.output=target/003-describe-your-change.xml
```

`compile` runs `initialize`, loads `.env`, and compiles the latest entities.
The diff compares these entities (desired schema) with the database (current
schema). Generation does not apply changes. No output file is created when
there are no differences. Reusing an existing output file appends changesets;
use a fresh filename for each generation attempt.

4. Review the candidate, including types, nullability, relationships, indexes,
   and any destructive operations. Check constraints are not generated by the
   configured Hibernate diff extension: the initial four checks are maintained
   explicitly in `002-business-checks.sql`. Review and write corresponding SQL
   changesets when those entity constraints change. Renames and data migrations
   also need explicit review.
5. Move the reviewed file into `src/main/resources/db/changelog` and add an
   ordered include in `db.changelog-master.xml`, for example:

```xml
<include file="003-describe-your-change.xml"
         relativeToChangelogFile="true"
         logicalFilePath="db/changelog/003-describe-your-change.xml"/>
```

The stable logical path ensures Maven and Spring identify the same changesets.
The initial generated schema is `001-initial-schema.xml`; do not modify
already-executed changesets. Add new changesets for later schema changes.

6. Preview pending SQL, then start the application or execute the tests:

```bash
./mvnw -Pdb-migrations initialize liquibase:updateSQL
./mvnw test
```

The SQL preview is written to `target/liquibase/migrate.sql` and does not apply
migrations. Application startup and the Spring context test apply pending
migrations using the master changelog, then Hibernate validates table mappings.
The Maven connection uses `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` from
`.env`; `DB_URL` overrides are currently supported only by Spring.

With PostgreSQL running, execute the tests:

```bash
./mvnw test
```

The context test, `TransferPersistenceTest`, and `WalletAndTransferRoutesTest`
require the local PostgreSQL database.
The persistence tests create wallets with random UUIDs and remove only their own
rows. They verify committed balances, the transfer creation timestamp, and rollback
after a simulated failure following an actual SQL flush.

To execute only the current unit tests:

```bash
./mvnw -Dtest=WalletServiceTest,TransferServiceTest test
```
