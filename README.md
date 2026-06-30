# Authentication API

Authentication and authorization service (OAuth2) for the MovieHub system.

## Tech stack
- Java 11, Spring Boot 2.3
- MySQL, Liquibase
- RabbitMQ
- Swagger
- Docker

## Configuration

**Database**
```properties
spring.datasource.url=jdbc:mysql://<db-host>:<db-port>/<db-name>?useUnicode=yes&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
spring.datasource.username=<username>
spring.datasource.password=<password>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

**RabbitMQ**
```properties
spring.rabbitmq.host=<host>
spring.rabbitmq.port=<port>
spring.rabbitmq.username=<username>
spring.rabbitmq.password=<password>

app.rabbitmq.exchange.topic.userEvent=User_Event
app.rabbitmq.queue.new.user=new_user
```

**Migration**
Schema and data migrations are defined in `src/main/resources/liquibase/db.changelog-master.xml`.

## Build

```bash
mvn clean package
```

Build Docker image:
```bash
docker build . --tag authentication-api:v1.0
```

## Run

From jar file:
```bash
java -jar <app.jar> -Dspring.profiles.active=<profile>
```

## Contact

| Name | Email | Role |
| --- | --- | --- |
| | | Senior Software Engineer |
