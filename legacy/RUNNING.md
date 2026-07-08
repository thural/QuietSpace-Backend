# QuietSpace Backend - Running & Documentation Guide

## Prerequisites

- Docker Engine running
- Java 17 (Temurin/OpenJDK)
- Maven (via `./mvnw` wrapper)
- GNU/Linux or macOS terminal

---

## 1. Start MySQL via Docker

```bash
docker run --name quietspace-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=quietspace \
  -p 3306:3306 \
  -d mysql:8.0
```

Verify it's healthy:
```bash
docker exec quietspace-mysql mysql -uroot -proot -e "SELECT 1" quietspace
```

---

## 2. Set Required Environment Variables

```bash
export ADMIN_PASSWORD="admin123"
export ACTIVE_PROFILE="dev"
export MAILDEV_HOST="localhost"
export MAILDEV_PORT="1025"
export JWT_SECRET_KEY="mysecretkeymysecretkeymysecretkey"
export ACTIVATION_URL="http://localhost:3000/activate"
export FRONTEND_HOST="localhost"
export FRONTEND_PORT="3000"
export DB_HOST_NAME="localhost"
export DB_NAME="quietspace"
export DB_USER_USERNAME="root"
export DB_USER_PASSWORD="root"
```

> **Note:** If you restart your terminal/computer, you must re-export these before running.

---

## 3. Run the Application

```bash
cd /home/thural/Github/QuietSpace-Backend/legacy
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

On a fresh checkout, build first:
```bash
./mvnw clean package -DskipTests
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 4. Live Documentation URLs

Open these once the app is fully started (Tomcat on port 8080):

| Documentation | URL |
|---------------|-----|
| **REST endpoints (Swagger UI)** | http://localhost:8080/swagger-ui/index.html |
| **REST endpoints (OpenAPI JSON)** | http://localhost:8080/v3/api-docs |
| **WebSocket endpoints (AsyncAPI UI)** | http://localhost:8080/springwolf/asyncapi-ui.html |
| **WebSocket endpoints (AsyncAPI JSON)** | http://localhost:8080/springwolf/docs |

Authentication is **not** required for the documentation endpoints.

---

## 5. Stop the Application

In the terminal running Maven:
```bash
Ctrl+C
```

Stop MySQL container (keep data):
```bash
docker stop quietspace-mysql
```

Start MySQL again later:
```bash
docker start quietspace-mysql
```

Remove MySQL container and data ( destructive ):
```bash
docker rm -f quietspace-mysql
```
