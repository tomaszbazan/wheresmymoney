# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Build and Run
```bash
# Build the application
gradle clean build

# Run locally (development)
./gradlew bootRun

# Build Docker image
docker build -t wheresmymoney:version .

# Run with Docker Compose (production)
docker compose up --no-deps --build

# Run tests
./gradlew test
```

## Architecture

This is a **Spring Boot 3.3.5** application using **Java 21** that follows **Domain-Driven Design (DDD)** principles with a modular monolith structure.

### Technology Stack
- **Spring Boot** with Spring Data JPA and Hibernate
- **PostgreSQL** database with **Flyway** migrations
- **JavaMoney (Moneta)** for currency handling
- **Testcontainers** for integration testing
- **Lombok** for reducing boilerplate

### Module Structure
The application is organized around business modules. Currently there's one main module:

**Account Module** (`pl.btsoftware.wheresmymoney.account`):
- `AccountModuleFacade` - Public API for the module
- `application/` - Application services (orchestration)  
- `domain/` - Core business logic and domain objects
- `infrastructure/` - External concerns (REST API, persistence, configuration)

### Domain Model
- **Account**: Aggregate root with balance tracking and multi-currency support
- **Expense**: Transaction records linked to accounts
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