# QuietSpace Backend

## Project Overview

QuietSpace is a privacy-focused social media application designed for meaningful interactions through a intuitive
interface. Built with Spring Boot, the application provides a comprehensive set of features for modern social
networking, emphasizing clean code, security, and real-time interaction.

## Features

### User Management

- Secure user authentication and authorization
- Email account activation system
- Stateless security with JWT (JSON Web Tokens)
- Refresh token mechanism
- Token blacklist strategy for secure logout

### Social Interactions

- Posts
- Comments
- Replies
- User profiles
- User Reactions
- User Followings
- Real-time notifications

### Real-Time Chat

- Secure WebSocket Chat Functionality
- End-to-end encrypted messaging
- Real-time chat using STOMP protocol
- One-to-one private messaging
- Group chat support
- Message delivery and read receipts
- Secure WebSocket connections
- Persistent message storage
- Integration with existing authentication system

### Media Management

- User profile picture uploads
- Post and Message image attachments
- Image conversion and resizing
- Efficient image storage and retrieval
- Direct database storage of images
- Optimized image representation in DTOs

### Advanced Capabilities

- Pagination, sorting and criteria queries for content
- Robust error handling with global exception management
- Scalable and modular application architecture
- Detailed API documentation

## Technology Stack

### Backend

- Framework: Spring Boot 4.1.0
- Language: Java 25
- Security: Spring Security 7.x, JWT (JJWT 0.13.0)
- API Documentation: Swagger/OpenAPI (springdoc 3.0.3), AsyncAPI (Springwolf 2.5.0)
- Development Tools: Lombok 1.18.46, MapStruct 1.6.3
- Serialization: Jackson 3.x
- Containerization: Docker, Docker Compose
- ORM: JPA/Hibernate 7.4.1
- Database: MySQL
- Migration: Flyway
- Test: JUnit 5, Mockito, Spring Boot Test

### Real-Time WebSocket Communication

- JWT-based authentication for connections
- Secure message routing
- Protection against unauthorized access
- Encrypted message payload
- Connection validation and management
- Error handling with custom Event messages

### Security and Authentication

- Spring Security
- JWT (JSON Web Tokens)
- Secure token management
- Encrypted WebSocket connections

### Development Tools

- Gradle 9.6.1 (Kotlin DSL)
- Lombok
- MapStruct
- Mockito
- JUnit 5

### Deployment

- Docker
- Docker Compose
- Kubernetes

## Detailed Project Structure

```plaintext
quietspace-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── dev/thural/quietspace/
│   │   │       ├── controller/       # REST API endpoints
│   │   │       ├── model/            # Data Transfer Objects
│   │   │       │   ├── request/      # Input DTOs
│   │   │       │   └── response/     # Output DTOs
│   │   │       ├── entity/           # Database entities
│   │   │       ├── repository/       # Data access layers
│   │   │       ├── service/          # Business logic
│   │   │       ├── mapper/           # Object mapping
│   │   │       ├── config/           # Application configurations
│   │   │       ├── security/         # Authentication components
│   │   │       ├── exception/        # Error handling
│   │   │       └── utils/            # Utility classes
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/         # Flyway database scripts
│   └── test/
│       ├── java/
│       │   └── dev/thural/quietspace/
│       │       ├── controller/       # MVC slice & integration tests
│       │       ├── service/          # Service unit tests
│       │       ├── mapper/           # Mapper tests
│       │       └── repository/       # Data layer tests
│       └── resources/
│           └── application.yml       # Test config (H2)
├── build.gradle.kts                  # Gradle build configuration
├── settings.gradle.kts               # Gradle settings
├── gradlew / gradlew.bat             # Gradle wrapper
├── .env                              # Environment variables
└── infrastructure/
    ├── docker/                       # Containerization configs
    └── k8s/                          # Kubernetes deployment
```

## Code Quality Principles

### Development Best Practices

- Modular and clean architecture
- SOLID principles implementation
- Meaningful naming conventions
- Small, focused methods
- Global exception handling
- Strict DRY code approach
- Consistent coding style
- Integration, unit, and mock tests

## Getting Started

### Prerequisites

- Java 25+
- Gradle 9.x (or use the included wrapper)
- MySQL 8+
- Docker (optional, for containerized deployment)

### Quick Setup

1. Clone the repository
   ```bash
   git clone https://github.com/thural/quietspace-backend.git
   cd quietspace-backend
   ```

2. Configure Environment
    - Copy or create a `.env` file at the project root with:
   ```
   ACTIVE_PROFILE=dev
   DB_PORT_NUMBER=3306
   SERVER_PORT_NUMBER=8080
   JWT_SECRET=your_secret_key
   JWT_EXPIRATION=86400000
   JWT_EXPIRATION_REFRESH=604800000
   ```

3. Build and Run
   ```bash
   ./gradlew build
   ./gradlew bootRun
   ```

## API Documentation

### REST API (OpenAPI 3.1 — springdoc)
- UI: `http://localhost:8080/swagger-ui.html`
- JSON: `http://localhost:8080/v3/api-docs`

### WebSocket/STOMP (AsyncAPI v3 — Springwolf)
- UI: `http://localhost:8080/springwolf/asyncapi-ui.html`
- JSON: `http://localhost:8080/springwolf/docs`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push and create a pull request

## License Notice

This project is licensed under a **Proprietary License**. All other rights reserved. See the [LICENSE.md](./LICENSE.md) file for full terms.

