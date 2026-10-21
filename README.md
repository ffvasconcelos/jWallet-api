# jWallet API

An educational wallet and transfer API using fictitious BRL amounts. This project is intended for Java and Spring Boot practice and does not process real financial transactions.

## Technologies

- Java 21
- Spring Boot 4.1.1
- Maven Wrapper
- Spring Web MVC and Bean Validation

## Initial scope

- Retrieve a wallet and its balance.
- Create a transfer between two wallets.
- Retrieve a transfer.
- List a wallet's transfers with pagination.

PostgreSQL persistence, migrations, idempotency, and concurrency control are covered in later roadmap stages.

## Business rules

1. The amount must be positive and have no more than two decimal places.
2. Source and target wallets must exist and be different.
3. The source wallet must have sufficient balance.
4. An invalid request must not change any balance.
5. Once persistence is introduced, debiting the source, crediting the target, and recording the transfer must happen as one unit of work.

## Roadmap

### Day 1 — Domain and first API

- Implement in-memory wallet and transfer rules.
- Expose the initial HTTP endpoints, DTOs, validation, and centralized error handling.
- Cover valid transfers and the main business-rule failures with tests.

### Day 2 — Persistence and consistency

- Persist wallets and transfers in PostgreSQL with JPA and Flyway migrations.
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

From the project root:

```bash
./mvnw spring-boot:run
```

To run the tests:

```bash
./mvnw test
```
