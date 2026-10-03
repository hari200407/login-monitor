# Login Activity Monitor

A Spring Boot + React security monitoring application that records login attempts, detects suspicious authentication behaviour, applies security controls, and provides an administrative dashboard for monitoring login activity.

## Project Overview

The Login Activity Monitor is designed to monitor authentication activity and identify potentially suspicious behaviour.

The application:

- Records successful and failed login attempts.
- Tracks username, IP address, timestamp, and login status.
- Detects repeated failed login attempts within a 10-minute window.
- Detects successful logins following multiple failed attempts.
- Stores suspicious activity for administrator review.
- Sends email notifications when suspicious activity is detected.
- Applies login rate limiting.
- Supports CAPTCHA after repeated failed login attempts.
- Supports email-based OTP two-factor authentication.
- Supports role-based access control.
- Supports session management with inactivity expiration.
- Provides a React-based security monitoring dashboard.

## Tech Stack

### Backend

- Java 21
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Mail
- MySQL
- Maven
- Lombok

### Frontend

- React
- Vite
- Axios
- React Google reCAPTCHA

## Architecture

```text
login-monitor/
│
├── src/
│   ├── main/
│   │   ├── java/com/innspark/loginmonitor/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   └── innspark/
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── frontend/
│   ├── src/
│   │   ├── services/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   └── Login.jsx
│   ├── package.json
│   └── vite.config.js
│
├── pom.xml
└── README.md
```

## Database

MySQL is used as the application's database.

Create the database:

```sql
CREATE DATABASE login_monitor;
```

The application uses the following main tables.

### `login_attempts`

Stores login attempts.

| Column | Description |
|---|---|
| `id` | Primary key |
| `username` | Username used for login |
| `ip_address` | Client IP address |
| `timestamp` | Time of the attempt |
| `status` | `SUCCESS` or `FAILURE` |

### `suspicious_activity`

Stores detected suspicious behaviour.

| Column | Description |
|---|---|
| `id` | Primary key |
| `ip_address` | IP associated with the activity |
| `username` | Related username, when available |
| `reason` | Reason for detection |
| `timestamp` | Detection time |

Additional tables are used for users, OTP verification, and sessions.

## Configuration

Do not commit real passwords or API secrets to GitHub.

Use environment variables for sensitive configuration.

Example:

```properties
spring.application.name=login-monitor
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3307/login_monitor
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

notification.admin-email=${ADMIN_EMAIL}

spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

captcha.secret-key=${CAPTCHA_SECRET_KEY}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

The project is configured for MySQL on port `3307` in the local development setup.

## Running the Backend

From the project root:

```bash
mvn spring-boot:run
```

The backend starts on:

```text
http://localhost:8080
```

## Running the Frontend

Open a second terminal:

```bash
cd frontend
npm install
npm run dev
```

The React application starts at:

```text
http://localhost:5173
```

## Authentication Flow

The login flow is:

```text
Username + Password
        |
        v
Repeated failures?
        |
        +---- Yes ----> CAPTCHA
        |
        v
Password authentication
        |
        v
OTP sent to registered email
        |
        v
OTP verification
        |
        v
Dashboard
```

## Security Features

### 1. Login Attempt Logging

Every authentication attempt is recorded with:

- Username
- IP address
- Timestamp
- Status

Supported statuses:

```text
SUCCESS
FAILURE
```

### 2. Suspicious Activity Detection

The application checks for repeated failures within a 10-minute window.

Suspicious activity can be detected when:

- Multiple failed logins occur from the same IP address.
- Multiple failed logins occur for the same username.
- A successful login occurs after multiple recent failures.

Detected activity is stored in the `suspicious_activity` table.

### 3. Email Notifications

When suspicious behaviour is detected, the application can send security notification emails to the configured administrator and, where applicable, the affected user's registered email address.

### 4. Rate Limiting

Login requests are rate limited to:

```text
5 requests per minute
```

When the limit is exceeded, the API returns:

```text
HTTP 429 - Too Many Requests
```

### 5. CAPTCHA

After multiple failed login attempts for the same username within the configured detection window, CAPTCHA becomes mandatory.

The CAPTCHA token is verified by the backend before the login request is allowed to continue.

### 6. Two-Factor Authentication

After username/password authentication succeeds, a six-digit OTP is sent to the registered email address.

The user must enter the OTP before the login process is completed.

OTP expiration and reuse protection are implemented.

### 7. Role-Based Access Control

The application supports:

```text
USER
ADMIN
SUPERADMIN
```

Administrative dashboard and suspicious-activity endpoints are restricted to administrative roles.

### 8. Session Management

The application supports sessions with inactivity tracking.

Sessions expire after:

```text
15 minutes of inactivity
```

Users can also log out and invalidate active sessions.

## API Endpoints

### Authentication

#### Login

```http
POST /auth/login
```

Authenticates username and password and initiates OTP verification.

Example request:

```json
{
  "username": "admin",
  "password": "admin123",
  "captchaToken": null
}
```

#### Verify OTP

```http
POST /auth/verify-otp
```

Example:

```json
{
  "username": "admin",
  "otp": "123456"
}
```

### Login Attempts

#### Record Login Attempt

```http
POST /login
```

#### Get Login Attempts

```http
GET /login
```

Supported filters include:

```text
username
ip
status
```

Administrative users can view broader login activity, while regular users are restricted to their own login history.

### Suspicious Activity

```http
GET /suspicious
```

Restricted to:

```text
ADMIN
SUPERADMIN
```

### Sessions

```http
POST /sessions
GET /sessions
POST /sessions/validate
POST /sessions/logout
```

### Dashboard

#### Statistics

```http
GET /dashboard/stats
```

Returns:

- Total attempts
- Successful logins
- Failed logins
- Suspicious activities

#### User Activity

```http
GET /dashboard/users
```

Returns per-user login statistics.

#### Suspicious Timeline

```http
GET /dashboard/suspicious
```

Returns suspicious activity ordered by time.

## Dashboard

The React dashboard provides:

### Statistics

- Total Attempts
- Successful Logins
- Failed Logins
- Suspicious Activities

### User Activity

Displays login activity grouped by username.

### Suspicious Activity Timeline

Displays:

- Time
- Username
- IP address
- Detection reason

## Example Suspicious Detection

Example scenario:

```text
User: admin
IP: 10.99.88.77

Failure
Failure
Failure
Failure
Failure
```

The application can record suspicious activity such as:

```text
5 failed logins for the same user in 10 minutes
```

Another detection scenario is:

```text
Failure
Failure
Failure
Success
```

which can generate:

```text
Successful login after multiple failed attempts
```

## Validation and Security Considerations

The application uses:

- Request validation.
- Password hashing with BCrypt.
- Spring Security authentication.
- Role-based authorization.
- CAPTCHA verification.
- Rate limiting.
- OTP expiration.
- OTP reuse prevention.
- Session expiration.
- Suspicious activity logging.

Production deployments should additionally use HTTPS, secure secret management, stronger rate-limiting infrastructure, and production CAPTCHA credentials.

## Demo Accounts

For local demonstration, the application seeds sample roles including:

```text
USER
ADMIN
SUPERADMIN
```

Example development credentials:

```text
Username: user
Password: user123

Username: admin
Password: admin123

Username: superadmin
Password: superadmin123
```

These credentials are intended only for local demonstration and should be changed in a production environment.

## Build and Test

### Backend

```bash
mvn clean test
```

### Frontend

```bash
cd frontend
npm run build
```

## Required Assessment Class

The assessment-required class is retained at:

```text
src/main/java/com/innspark/loginmonitor/innspark/login.java
```

## Future Improvements

Possible production enhancements include:

- Persistent/distributed rate limiting using Redis.
- JWT or another dedicated token-based authentication flow.
- Persistent audit logging.
- Stronger session/token management.
- Production CAPTCHA configuration.
- HTTPS deployment.
- Advanced dashboard charts and analytics.
- Automated security alerts and reporting.

## Author

Developed as part of the INNSPARK technical assessment.

**Project:** Login Activity Monitor for Suspicious Behaviour
