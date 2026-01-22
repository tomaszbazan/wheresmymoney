# AI Rules for backend of WheresMyMoney

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


### Database
- PostgreSQL 17 with Docker Compose setup
- Flyway migrations in `src/main/resources/db/migration/`
- JPA entities in `infrastructure/persistance/`

### Testing
- Integration tests use `@IntegrationTest` annotation with Testcontainers
- In-memory repository implementations for unit testing
- Test fixtures in `infrastructure/persistance/` package
- **ALWAYS use AssertJ for assertions** - never use JUnit assertions (assertEquals, assertTrue, etc.)
  - Use `assertThat(actual).isEqualTo(expected)` instead of `assertEquals(expected, actual)`
  - Use `assertThat(value).isNull()` instead of `assertNull(value)`
  - Use `assertThat(value).isNotNull()` instead of `assertNotNull(value)`
  - Use `assertThat(collection).isEmpty()` instead of `assertTrue(collection.isEmpty())`
  - Use `assertThat(collection).hasSize(n)` instead of `assertEquals(n, collection.size())`
  - Use `assertThatThrownBy(() -> code).isInstanceOf(Exception.class)` instead of
    `assertThrows(Exception.class, () -> code)`

## Commands

### Build and Run
```bash
# Build the application
gradle clean build

# Run unit tests (skipping system tests)
gradle clean test -x systemTest

# Run system tests (skipping unit tests)
gradle clean systemTest -x test

# Run checkstyle & spotbugs
gradle checkstyleMain checkstyleTest spotbugsMain spotbugsTest
```

## Development Requirements

### Guidelines for JAVA

- Prefer record above classes for simple data carriers
- Use `var` for local variables when the type is obvious
- Write small, focused methods and classes
- Use functional programming techniques where appropriate
- Follow the project's coding standards and best practices
- After each feature or bug fix, ensure all tests pass (unit and system) and code is cleaned up (use checkstyle &
  spotbugs)

#### SPRING_BOOT

- Use Spring Boot for simplified configuration and rapid development with sensible defaults
- Prefer constructor-based dependency injection over `@Autowired`
- Avoid hardcoding values that may change externally, use configuration parameters instead
- For complex logic, use Spring profiles and configuration parameters to control which beans are injected instead of
  hardcoded conditionals
- If a well-known library simplifies the solution, suggest using it instead of generating a custom implementation
- Use DTOs as immutable `record` types
- Use Bean Validation annotations (e.g., `@Size`, `@Email`, etc.) instead of manual validation logic
- Use `@Valid` on request parameters annotated with `@RequestBody`
- Use custom exceptions for business-related scenarios
- Centralize exception handling with `@ControllerAdvice` and return a consistent error DTO: `{{error_dto}}`
- REST controllers should handle only routing and I/O mapping, not business logic
- Use SLF4J for logging instead of `System.out.println`
- Prefer using lambdas and streams over imperative loops and conditionals where appropriate
- Use `Optional` to avoid `NullPointerException`

#### LOMBOK

- Use Lombok where it clearly simplifies the code
- Use constructor injection with `@RequiredArgsConstructor`
- Prefer Java `record` over Lombok’s `@Value` when applicable
- Avoid using `@Data` in non-DTO classes, instead, use specific annotations like `@Getter`, `@Setter`, and `@ToString`
- Apply Lombok annotations to fields rather than the class if only some fields require them
- Use Lombok’s `@Slf4j` to generate loggers

#### SPRING_DATA_JPA

- Define repositories as interfaces extending `JpaRepository` or `CrudRepository`
- Never expose JPA entities in API responses – always map them to DTOs
- Use `@Transactional` at the service layer for state-changing methods, and keep transactions as short as possible
- Use `@Transactional(readOnly = true)` for read-only operations
- Use `@EntityGraph` or fetch joins to avoid the N+1 select problem
- Use `@Query` for complex queries
- Use projections (DTOs) in multi-join queries with `@Query`
- Use Specifications for dynamic filtering
- Use pagination when working with large datasets
- Use `@Version` for optimistic locking in concurrent updates
- Avoid `CascadeType.REMOVE` on large entity relationships
- Use HikariCP for efficient connection pooling

