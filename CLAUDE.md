# authentication-api (MovieHub)

Spring Boot 2.3 / Java 11 authentication & user-management service.

- Maven module: `source/com-authentication-api` (groupId `com.authentication.api`, artifactId `authentication-api`)
- Package root: `com.authentication.api`
- Key packages: `controller/`, `service/` (+ `service/impl/`), `mapper/` (MapStruct), `model/` (+ `model/criteria/` for Spring Data Specifications), `form/` (request bodies, `javax.validation`), `dto/`, `exception/`, `jwt/`, `config/` (OAuth2 authorization/resource server security).
- Persistence: JPA/Hibernate + Liquibase (`src/main/resources/liquibase/`).
- Async/eventing: RabbitMQ (`service/rabbit/`), Redis (`service/redis/`).

## Controller conventions

- Every controller extends `ABasicController`, which exposes `makeSuccessResponse(...)`, `makeErrorResponse(...)`, `makeResponseListDto(...)`, and current-session helpers (`getCurrentUser()`, `getSessionFromToken()`, `isSuperAdmin()`, `isAdmin()`, `isEmployee()`, `isUser()`) backed by an injected `UserServiceImpl.getAddInfoFromToken()` (returns `BaseJwt`).
- Most write/read endpoints return `ApiMessageDto<T>` (`result`/`code`/`data`/`message`); some auth endpoints (login, social callbacks, anonymous token) return `OAuth2AccessToken` directly — don't assume one shape for every endpoint.
- Errors are thrown as `NotFoundException` / `BadRequestException` / `UnauthorizationException`, each carrying a `code` from `dto/ErrorCode.java`.
- Request bodies are `@Valid @RequestBody <Form>` classes under `form/**` using `javax.validation` (`@NotBlank`/`@NotEmpty` plus custom constraints like `@EmailConstraint`, `@PasswordConstraint`).

## Testing

- **Unit tests only, Controller layer only.** Use **JUnit 5 + Mockito** (provided transitively by `spring-boot-starter-test`, already in `pom.xml` alongside `spring-security-test`). Never `@SpringBootTest`/`@WebMvcTest` — call controller methods directly as plain Java objects (`@ExtendWith(MockitoExtension.class)`, `@Mock` + `@InjectMocks`), mocking repositories/mappers/services at the controller boundary.
- Test files live under `source/com-authentication-api/src/test/java/com/authentication/api/controller/`, named `<ClassUnderTest>Test`.
- The pom sets `<maven.test.skip>true</maven.test.skip>` by default — run tests with the skip flags overridden. The `./mvnw` wrapper in this repo is broken (missing `.mvn/wrapper/*`) — use system `mvn`:
  ```sh
  cd source/com-authentication-api
  mvn -Dmaven.test.skip=false -DskipTests=false -Dtest=<TheTest> test
  ```
- Mockito runs in strict-stubbing mode: don't put a shared `when(...)` in `@BeforeEach` unless every test in the class actually hits it — many controller methods throw (e.g. not-found) before reaching an `isSuperAdmin()`/`isAdmin()` check, which leaves that stub unused and fails with `UnnecessaryStubbingException`. Stub per-test instead.
- Do not write tests for `service/`, `mapper/`, `repository/`, or `validation/` classes directly — cover them indirectly through controller tests.
- The `test-automator` agent (`.claude/agents/test-automator.md`) is set up to generate this kind of test on request.
