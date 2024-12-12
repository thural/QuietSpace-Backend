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

- Framework: Spring Boot 3.3.4
- Language: Java 17
- Security: Spring Security, JWT
- API Documentation: Swagger/OpenAPI
- Development Tools: Lombok
- Containerization: Docker, Kubernetes
- ORM: JPA/Hibernate
- Database: MySQL
- Migration: Flyway
- Test: Junit5, Mockito

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

- Maven
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
QuietSpace-Backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/quietspace/
│   │   │       ├── controller/     # REST API endpoints
│   │   │       │   ├── UserController.java
│   │   │       │   ├── PostController.java
│   │   │       │   └── CommentController.java
│   │   │       ├── model/          # Data Transfer Objects
│   │   │       │   ├── request/    # Input DTOs
│   │   │       │   └── response/   # Output DTOs
│   │   │       ├── entity/         # Database entities
│   │   │       │   ├── User.java
│   │   │       │   ├── Post.java
│   │   │       │   └── Comment.java
│   │   │       ├── repository/     # Data access layers
│   │   │       ├── service/        # Business logic
│   │   │       ├── config/         # Application configurations
│   │   │       │   ├── WebSocketConfig.java
│   │   │       │   └── SecurityConfig.java
│   │   │       ├── security/       # Authentication components
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   └── JwtUtil.java
│   │   │       └── exception/      # Error handling
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/       # Flyway database scripts
│   └── test/                       # Comprehensive test suites
│       ├── unit/
│       ├── integration/
│       └── mock/
└── infrastructure/
    ├── docker/                     # Containerization configs
    └── k8s/                        # Kubernetes deployment
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

- Java 17+
- Maven
- MySQL
- Docker (optional)
- Kubernetes (optional)

### Quick Setup

1. Clone the repository
   ```bash
   git clone https://github.com/thural/QuietSpace-Backend.git
   cd QuietSpace-Backend
   ```

2. Configure Environment
    - Create a `.env` file with database and JWT configurations
   ```
   SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/quietspace
   SPRING_DATASOURCE_USERNAME=your_username
   SPRING_DATASOURCE_PASSWORD=your_password
   JWT_SECRET=your_secret_key
   ```

3. Build and Run
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## API Documentation

Swagger/OpenAPI documentation available at:
`http://localhost:8080/swagger-ui.html`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push and create a pull request

# License Notice

This repository is licensed under the GNU Affero General Public License v3.0 (AGPLv3). This license requires that any
modifications or distributions of the software, including commercial or proprietary use, must be made available under
the same terms. Failure to comply with these terms may result in legal action.
