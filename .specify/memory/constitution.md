<!--
SYNC IMPACT REPORT
==================
Version change: 1.0.0 → 1.1.0
Added sections:
  - Core Principles (5 principles, all new)
  - Tech Stack Constraints (new)
  - Error Handling & Database Conventions (new)
  - Governance (new)
Modified principles: n/a (initial ratification)
Templates requiring updates:
  - .specify/templates/plan-template.md ✅ Constitution Check section already present — gates now bind to these principles
  - .specify/templates/spec-template.md ✅ No changes required; template is technology-agnostic
  - .specify/templates/tasks-template.md ✅ Test tasks are already marked OPTIONAL; aligns with Principle V
Deferred TODOs: none
-->

# Java Spring Boot Backend Constitution

## Core Principles

### I. Controller-Repository Architecture (NON-NEGOTIABLE)

Controllers inject Repositories and Mappers directly — there is no mandatory Service layer
for standard CRUD operations. Business logic (entity lookup, uniqueness checks, state
transitions, persistence) lives in the Controller method. `BaseApiService` exists solely for
cross-cutting utilities (email sending, file deletion, OTP generation) and MUST NOT be used
as a substitute per-entity service. New utility concerns that span multiple features belong
in `BaseApiService`; entity-specific logic belongs in the Controller.

### II. Controller Contract & Response Shape

Every Controller MUST extend `ABasicController`. All API responses MUST be returned as
`ApiMessageDto<T>` using the inherited helpers `makeSuccessResponse` and `makeErrorResponse` —
never return raw objects or custom wrappers. Every write endpoint (POST, PUT, DELETE) MUST
carry `@Transactional`. Every endpoint MUST be protected with
`@PreAuthorize("hasRole('{PREFIX}_{ACTION}')")` using the permission suffixes `_C` (create),
`_U` (update), `_D` (delete), `_V` (view). Controllers MUST be annotated with
`@RestController`, `@RequestMapping("/v1/{resource}")`, `@CrossOrigin`, and `@Slf4j`.

### III. MapStruct-Only Mapping (NEVER manual)

Conversion between Entity, Form, and DTO MUST go through a MapStruct Mapper interface.
Manual setter chains for mapping are forbidden. Mapper configuration MUST use:
`@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)`.
Every mapped field MUST have an explicit `@Mapping` annotation combined with
`@BeanMapping(ignoreByDefault = true)`. The annotation processor order in `pom.xml` MUST be:
Lombok first, then MapStruct, then `lombok-mapstruct-binding`. This order MUST NEVER change —
reversing it causes compilation failures because MapStruct cannot see Lombok-generated
accessors.

### IV. Validation at Form Boundary

All input validation MUST be declared on Form classes via `javax.validation` annotations
(never `jakarta.validation` — this project targets Java 11 / Spring Boot 2.3). No `if`-based
validation logic is permitted inside Controllers or any other layer for data that can be
expressed as constraints. Fields with a fixed set of valid integers (kind, gender, status,
role, etc.) MUST use a custom constraint annotation placed in `validation/` with its
implementation in `validation/impl/`. Valid values MUST be declared as constants in
`BaseConstant` — validators MUST reference those constants, never hardcode integers such as
`Objects.equals(value, 1)`. Every custom constraint MUST expose an `allowNull` parameter to
support optional fields in Update forms.

### V. No Tests

This project has no test suite. `maven.test.skip=true` in `pom.xml` is intentional and MUST
remain. No test class, test directory, or test dependency MUST be created unless the
developer explicitly requests it. All build and CI commands skip tests by default. When
implementing a new feature, the implementation checklist ends at compilation and manual
API verification — not at test coverage.

## Tech Stack Constraints

Java 11 and Spring Boot 2.3.0.RELEASE are the fixed runtime. All `javax.*` namespaces
(persistence, validation) MUST be used — the `jakarta.*` migration does not apply here.
Dependency injection MUST use `@Autowired` field injection; constructor injection MUST NOT
be introduced. Entities MUST extend `Auditable<String>` and carry
`@Getter @Setter @AllArgsConstructor @NoArgsConstructor` from Lombok. Table names MUST use
the `db_` prefix from `DatabaseConstant.PREFIX_TABLE`. IDs are Snowflake-generated via the
inherited `Auditable` mechanism — never define a manual `@Id` field. DTOs and Forms MUST use
`@Getter @Setter @ApiModel` and annotate each field with `@ApiModelProperty`.

## Error Handling & Database Conventions

Error codes follow the pattern `ERROR-{DOMAIN}-{NUMBER}` (e.g., `ERROR-ACCOUNT-0001`) and
MUST be registered as public static final String constants in `ErrorCode.java` before use.
Throw `BadRequestException(message, errorCode)` for invalid input and
`NotFoundException(message, errorCode)` for missing entities — never return error details
through the happy path.

Whenever an Entity class is created or any of its fields, annotations, or relationships
change, the following two-step sequence is MANDATORY before committing:

1. `cd source/com-authentication-api && mvn clean compile` — Liquibase reads compiled
   bytecode, not source files. If compilation fails, fix it before proceeding.
2. `cd source/com-authentication-api && mvn liquibase:diff` — generates a changelog XML
   in `src/main/resources/liquibase/`. Verify every generated table name carries the `db_`
   prefix from `DatabaseConstant.PREFIX_TABLE`, then include the new file in
   `db.changelog-master.xml`.

Never alter the database schema by hand. Never run `mvn liquibase:diff` without a
successful compile first — the diff will be empty or incorrect.

## Governance

This constitution supersedes all other conventions, README sections, or verbal agreements.
Any change to a Core Principle requires updating this file and incrementing the version
before the change is applied to the codebase. Version increments follow semantic versioning:
MAJOR for principle removals or backward-incompatible redefinitions, MINOR for new
principles or materially expanded guidance, PATCH for wording and clarification. Build
command is `cd source/com-authentication-api && mvn clean package`. All generated feature
plans (via `/speckit-plan`) MUST include a Constitution Check gate that validates compliance
with Principles I through V before implementation begins.

**Version**: 1.1.0 | **Ratified**: 2026-07-03 | **Last Amended**: 2026-07-03
