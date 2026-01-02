# Kitly Mail Service

Mailing Service for the Kitly Service - A Spring Boot based REST API for sending emails through various providers.

## Features

- **REST API**: Simple HTTP endpoints for sending and managing emails
- **Provider Abstraction**: Clean interface for swapping email service providers
- **BREVO Integration**: Reference implementation using BREVO (Sendinblue) API
- **Spring Security**: Basic authentication for API endpoints
- **Database Persistence**: Email tracking with JPA/Hibernate and H2 database
- **Flyway Migrations**: Database schema versioning
- **Maven Profiles**: Easy provider configuration through Maven profiles
- **Comprehensive Tests**: Full test coverage with unit and integration tests

## Technology Stack

- Java 17
- Spring Boot 4.0.1
- Spring Security (Basic Auth)
- Spring Data JPA + Hibernate
- Flyway (Database migrations)
- Lombok (Reduce boilerplate)
- H2 Database (Can be replaced with any SQL database)
- JUnit 5 + Mockito (Testing)
- MockWebServer (HTTP client testing)

## Architecture

The service follows a clean architecture pattern:

```
├── controller/       # REST endpoints
├── service/          # Business logic
├── repository/       # Data access layer
├── model/            # Domain entities
├── provider/         # Email provider implementations
│   └── brevo/       # BREVO implementation
└── config/          # Spring configuration
```

### Key Components

- **MailProvider Interface**: Abstract contract for email service providers
- **BrevoMailProvider**: Reference implementation for BREVO API
- **EmailService**: Orchestrates email sending and persistence
- **EmailController**: REST API endpoints
- **Email Entity**: JPA entity for tracking email status

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- BREVO API key (get one at https://www.brevo.com/)

### Build

Build with default BREVO profile:
```bash
mvn clean package
```

Build with specific profile:
```bash
mvn clean package -Pbrevo
```

### Configuration

Configure the application by setting environment variables or updating `application.yml`:

```yaml
# BREVO API Configuration
brevo:
  api:
    key: ${BREVO_API_KEY:your-brevo-api-key}
    url: https://api.brevo.com/v3

# Security Configuration
app:
  security:
    username: ${APP_USERNAME:admin}
    password: ${APP_PASSWORD:admin}
```

### Running the Application

Set environment variables:
```bash
export BREVO_API_KEY=your-actual-api-key
export APP_USERNAME=your-username
export APP_PASSWORD=your-secure-password
```

Run the application:
```bash
java -jar target/kitly-mail-1.0.0-SNAPSHOT.jar
```

Or with Maven:
```bash
mvn spring-boot:run
```

The service will start on port 8080.

### Running with Docker

Build and run using Docker Compose:
```bash
export BREVO_API_KEY=your-actual-api-key
export APP_USERNAME=admin
export APP_PASSWORD=secure-password
docker-compose up -d
```

This will start both PostgreSQL and the application.

To build the Docker image manually:
```bash
mvn clean package
docker build -t kitly-mail:latest .
docker run -p 8080:8080 \
  -e BREVO_API_KEY=your-key \
  -e APP_USERNAME=admin \
  -e APP_PASSWORD=secure \
  -e MAIL_PROVIDER=brevo \
  kitly-mail:latest
```

## API Endpoints

All endpoints require Basic Authentication.

### Send Email

```http
POST /api/emails
Content-Type: application/json
Authorization: Basic <base64-encoded-credentials>

{
  "fromEmail": "sender@example.com",
  "fromName": "Sender Name",
  "toEmail": "recipient@example.com",
  "toName": "Recipient Name",
  "subject": "Test Email",
  "htmlContent": "<h1>Hello World</h1>",
  "textContent": "Hello World"
}
```

**Response:**
```json
{
  "id": 1,
  "fromEmail": "sender@example.com",
  "fromName": "Sender Name",
  "toEmail": "recipient@example.com",
  "toName": "Recipient Name",
  "subject": "Test Email",
  "status": "SENT",
  "externalId": "msg-123",
  "errorMessage": null,
  "createdAt": "2026-01-02T15:00:00",
  "sentAt": "2026-01-02T15:00:01"
}
```

### Get Email by ID

```http
GET /api/emails/{id}
Authorization: Basic <base64-encoded-credentials>
```

### Get All Emails

```http
GET /api/emails
Authorization: Basic <base64-encoded-credentials>
```

### Filter by Status

```http
GET /api/emails?status=SENT
Authorization: Basic <base64-encoded-credentials>
```

Possible status values: `PENDING`, `SENT`, `FAILED`

### Filter by Recipient

```http
GET /api/emails?recipient=user@example.com
Authorization: Basic <base64-encoded-credentials>
```

For more examples including curl commands, see [EXAMPLES.md](EXAMPLES.md).

## Testing

Run all tests:
```bash
mvn test
```

Run with coverage:
```bash
mvn test jacoco:report
```

The test suite includes:
- Unit tests for models, services, and controllers
- Integration tests for the complete email flow
- Mock server tests for the BREVO provider

## Maven Profiles

The service supports multiple email providers through Maven profiles:

- **brevo** (default): BREVO/Sendinblue provider
- **mailgun**: Placeholder for MAILGUN implementation (not implemented)

Activate a specific profile:
```bash
mvn clean package -P<profile-name>
```

## Adding a New Provider

To add a new email provider:

1. Create a new package under `provider/` (e.g., `provider/mailgun/`)
2. Implement the `MailProvider` interface:
```java
@Service
@ConditionalOnProperty(name = "mail.provider", havingValue = "mailgun")
public class MailgunProvider implements MailProvider {
    // Implementation
}
```
3. Add configuration properties to `application.yml`
4. Add a Maven profile in `pom.xml`
5. Add tests for your implementation

## Database

The service uses H2 in-memory database by default. To use a different database:

1. Add the database driver dependency to `pom.xml`
2. Update the datasource configuration in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/kitlymail
    username: user
    password: pass
    driver-class-name: org.postgresql.Driver
```

## Security

The service uses Spring Security with Basic Authentication. Default credentials:
- Username: `admin`
- Password: `admin`

**Important**: Change these credentials in production by setting environment variables:
```bash
export APP_USERNAME=your-username
export APP_PASSWORD=your-secure-password
```

## H2 Console

Access the H2 console at: http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:kitlymail`
- Username: `sa`
- Password: (leave blank)

## License

See [LICENSE](LICENSE) file for details.
