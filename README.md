# Spring Boot User Authentication Boilerplate

A production-ready, highly scalable User Authentication boilerplate built with **Spring Boot 4.1.1**, **PostgreSQL**, and **Redis**. This repository is designed to be a solid foundation for any modern backend application that requires a robust authentication and user management system out-of-the-box.

## 🚀 Features

- **JWT Authentication**: Stateless session management with short-lived Access Tokens (HS256) and long-lived Refresh Tokens.
- **Secure Token Rotation**: Redis Lua scripting ensures **atomic** read-modify-write for Refresh Tokens to securely detect token reuse and prevent race conditions.
- **Decentralized ID Generation**: Custom `@SnowflakeId` generator creates distributed, time-sortable 64-bit Long IDs (Twitter Snowflake algorithm) without database bottlenecks, including robust clock drift handling.
- **JPA Auditing & Soft Delete**: Base entities automatically track `created_by`, `created_at`, `updated_by`, `updated_at`, and `deleted_at`.
- **Rate Limiting**: Built-in Redis-based rate limiting to prevent brute-force attacks (e.g., limit `/login` and `/forgot-password` endpoints).
- **Flyway Migrations**: Clean database version control, pre-seeded with default roles (`ROLE_USER`, `ROLE_ADMIN`, `ROLE_MODERATOR`).
- **Global Exception Handling**: Graceful error handling for Validation, Resource Not Found, Unauthorized, and Duplicate Record errors with consistent JSON structures.
- **Docker Ready**: `docker-compose.yml` included for one-click setup of PostgreSQL and Redis dependencies.

## 🛠️ Tech Stack

- **Framework**: Spring Boot 4.1.1 (Spring Security, Spring Data JPA, Spring Web)
- **Language**: Java 21
- **Database**: PostgreSQL 16
- **Cache / Session / Rate Limit**: Redis 7
- **Migrations**: Flyway
- **Tools**: Maven, Lombok, JJWT (Java JWT)

## 📦 Getting Started

### Prerequisites
- JDK 21+
- Docker & Docker Compose (for local DB/Redis)
- Maven

### 1. Start External Services
Run the following command in the root directory to spin up PostgreSQL and Redis:
```bash
docker-compose up -d
```

### 2. Run the Application
The application will automatically connect to the local Docker services and run Flyway migrations to set up the database schema.
```bash
./mvnw spring-boot:run
```
The server will start on `http://localhost:8080`.

## 📁 Project Structure

This project uses a domain-driven package structure to make it easy to drop in new features:

```
src/main/java/pnh/dev/userauth/
├── auth/           # JWT, Auth Services, Auth Controllers, DTOs
├── common/         # SnowflakeId, BaseEntity, AuditableEntity
├── config/         # SecurityConfig, RedisConfig, JpaAuditingConfig, CorsConfig
├── exception/      # GlobalExceptionHandler, Custom Exceptions
├── security/       # CustomUserDetailsService
├── user/           # UserAccount, UserProfile, Role, Repositories, Controllers
└── UserAuthApplication.java
```

## 🔌 API Endpoints

A complete Postman collection is included in the root directory: `UserAuth_Postman_Collection.json`.

### Authentication (`/api/auth`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/register` | Register a new user | ❌ |
| `POST` | `/login` | Login with username/email & password | ❌ |
| `POST` | `/refresh` | Get new Access Token via Refresh Token | ❌ |
| `POST` | `/logout` | Blacklist current access token | ✅ |
| `POST` | `/logout-all`| Revoke all active sessions across devices | ✅ |
| `POST` | `/forgot-password`| Generate a password reset token | ❌ |
| `POST` | `/reset-password`| Reset password using token | ❌ |

### Users (`/api/users`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET`  | `/me` | Get current user's profile | ✅ |
| `PUT`  | `/me` | Update current user's profile | ✅ |

## 🏗️ How to Extend (For Your Next Project)

1. **Adding New Entities**: Extend `BaseEntity` (if you only need create auditing) or `AuditableEntity` (if you need update/delete auditing). 
   ```java
   @Entity
   public class Product extends AuditableEntity {
       // Your fields
   }
   ```
2. **Changing Database Name**: Update `POSTGRES_DB` in `docker-compose.yml` and `spring.datasource.url` in `src/main/resources/application-dev.properties`.
3. **Updating JWT Secret**: Change `app.jwt.secret` in `application-dev.properties` to a strong, 256-bit+ secure string for production.
4. **CORS Configuration**: By default, `CorsConfig` allows `http://localhost:3000`. Update this class when deploying to production with your frontend domain.

## 📝 License
This boilerplate is open-source and free to use for any commercial or non-commercial projects.
