## Commands

### Build and Run

```bash
# Build the application
gradle clean build

# Run unit tests (skipping system tests)
gradle clean test -x systemTest

# Run system tests (skipping unit tests)
gradle clean systemTest -x test
```

## Architecture

This is a **Spring Boot 3.3.5** application using **Java 21** that follows **Domain-Driven Design (DDD)** principles
with a modular monolith structure.

### Technology Stack

- **Spring Boot** with Spring Data JPA and Hibernate
- **PostgreSQL** database with **Flyway** migrations
- **JavaMoney (Moneta)** for currency handling
- **Testcontainers** for integration testing
- **Lombok** for reducing boilerplate

### Module Structure

The application is organized around business modules.

**Account Module** (`pl.btsoftware.wheresmymoney.account`):

- `AccountModuleFacade` - Public API for the module
- `application/` - Application services (orchestration)
- `domain/` - Core business logic and domain objects
- `infrastructure/` - External concerns (REST API, persistence, configuration)

**Transaction Module** (`pl.btsoftware.wheresmymoney.transaction`):

- `TransactionModuleFacade` - Public API for the module
- `application/` - Application services (orchestration)
- `domain/` - Core business logic and domain objects
- `infrastructure/` - External concerns (REST API, persistence, configuration)

### Domain Model

- **Account**: Aggregate root with balance tracking and multi-currency support
- **Transaction**: Transaction records linked to accounts
- **Money**: Value object supporting PLN, EUR, USD, GBP with proper arithmetic
- Rich domain validation with custom business exceptions in `domain/error/`

### Database

- PostgreSQL 17 with Docker Compose setup
- Flyway migrations in `src/main/resources/db/migration/`
- JPA entities in `infrastructure/persistance/`

### Testing

- Integration tests use `@IntegrationTest` annotation with Testcontainers
- In-memory repository implementations for unit testing
- Test fixtures in `infrastructure/persistance/` package

## Development Requirements

- Prefer record above classes for simple data carriers
- Use `var` for local variables when the type is obvious
- Avoid Lombok for domain objects; use it in infrastructure layer if needed
- Write small, focused methods and classes
- Use functional programming techniques where appropriate
- Follow the project's coding standards and best practices
- After each feature or bug fix, ensure all tests pass (unit and system) and code is cleaned up