# WheresMyMoney Developer Guidelines

This guide provides essential information for new developers working on the WheresMyMoney project.

## Project Overview

WheresMyMoney is a budgeting application that allows users to track expenses and income across different accounts.

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.3.5
- **Build Tool**: Gradle
- **Database**: PostgreSQL 17
- **ORM**: Spring Data JPA
- **Migration**: Flyway
- **Testing**: JUnit 5, TestContainers, AssertJ
- **Containerization**: Docker, Docker Compose

## Project Structure

The project follows a modular architecture with clean/hexagonal design principles:

```
src/
├── main/java/pl/btsoftware/wheresmymoney/
│   ├── [module]/
│   │   ├── domain/         # Core business logic, entities, value objects
│   │   ├── application/    # Application services, use cases
│   │   ├── infrastructure/ # External interfaces (API, repositories)
│   │   └── [Module]Facade.java # Public interface to the module
│   └── WheresMyMoneyApplication.java
└── test/
    └── java/pl/btsoftware/wheresmymoney/
        ├── [module]/       # Tests mirror the main structure
        └── configuration/  # Test configurations
```

## Development Workflow

### Building the Project

```bash
# Build the application
./gradlew clean build

# Build Docker image
docker build -t wheresmymoney:version .
```

### Running the Application

```bash
# Run with Docker Compose
docker compose up --no-deps --build

# Run locally with Gradle
./gradlew bootRun
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "pl.btsoftware.wheresmymoney.account.domain.AccountTest"
```

### API Testing

The project includes an `api-requests.http` file for testing API endpoints with IntelliJ's HTTP Client. See `README-API.md` for detailed instructions.

## Best Practices

1. **Code Organization**:
   - Follow the established module structure
   - Keep domain logic free from infrastructure concerns
   - Use the module facade as the only entry point to a module

2. **Testing**:
   - Write tests for all new functionality
   - Follow the existing test structure
   - Use TestContainers for integration tests with the database

3. **Database Changes**:
   - Add new migrations in `src/main/resources/db/migration/`
   - Follow the Flyway naming convention: `V{version}__{description}.sql`

4. **Docker**:
   - Use the provided Docker Compose setup for local development
   - The database is initialized with scripts in the `scripts/` directory

## Troubleshooting

- If the application fails to connect to the database, ensure PostgreSQL is running on port 5433
- Check the Docker logs with `docker compose logs`