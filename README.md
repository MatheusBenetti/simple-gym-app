# 🏋️‍♂️ SimpleGymApp

A complete REST API for gym workout sheet management, built with Spring Boot 3, PostgreSQL, and Redis.

## 📋 Features

- ✅ Complete user system (registration, login, profile)
- ✅ JWT authentication with Redis cache
- ✅ Workout creation and management (Back, Chest, Legs, etc.)
- ✅ Exercise addition to workouts (sets, reps)
- ✅ Intelligent caching for better performance
- ✅ Robust security (users only access their own data)
- ✅ RESTful API with validations
- ✅ Docker for easy development
- ✅ Comprehensive test suite (Unit, Integration, Performance)

## 🛠️ Technologies

- **Java 21**
- **Spring Boot 3.2+**
- **Spring Security** (JWT)
- **Spring Data JPA**
- **PostgreSQL 15**
- **Redis 7** (Cache)
- **Docker & Docker Compose**
- **Maven**
- **Lombok**
- **JUnit 5** (Testing)
- **Mockito** (Mocking)
- **Testcontainers** (Integration Tests)

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Docker and Docker Compose
- Maven (or use the wrapper `./mvnw`)

### 1. Clone the repository
```bash
git clone https://github.com/your-username/simplegymapp.git
cd simplegymapp
```

### 2. Start infrastructure (PostgreSQL + Redis)
```bash
docker-compose up postgres redis -d
```

### 3. Run the application
```bash
# Via Maven
./mvnw spring-boot:run

# Or via IDE (IntelliJ IDEA, Eclipse, VS Code)
```

### 4. Application will be running at:
- **API:** http://localhost:8080
- **PostgreSQL:** localhost:5432
- **Redis:** localhost:6379

## 📁 Project Structure

```
src/main/java/com/totex/simplegymapp/
├── business/
│   ├── converter/          # DTO ↔ Model converters
│   ├── dto/               # Data Transfer Objects
│   └── service/           # Business logic
├── controller/            # REST Controllers
├── infrastructure/
│   ├── config/           # Configurations (Redis, etc.)
│   ├── exception/        # Custom exceptions
│   ├── model/           # JPA Entities
│   ├── repository/      # Repositories
│   └── security/        # JWT/Security configuration
└── SimpleGymAppApplication.java

src/test/java/com/totex/simplegymapp/
├── base/                 # Base test classes
├── business/            # Service and converter tests
├── controller/          # Controller integration tests
├── infrastructure/      # Repository and security tests
├── integration/         # End-to-end workflow tests
└── performance/         # Performance and load tests
```

## 🔗 API Endpoints

### 🔐 Authentication
```http
POST /user                    # Register user
POST /user/login             # Login
POST /user/logout            # Logout
POST /user/validate-token    # Validate token
```

### 👤 Users
```http
GET  /user?email=           # Find user by email
PUT  /user                  # Update profile
PUT  /user/password         # Change password
DELETE /user/{email}        # Delete account
```

### 🏋️ Workouts
```http
POST   /workouts                    # Create workout
GET    /workouts/my-workouts        # My workouts
GET    /workouts/{id}               # Get specific workout
PUT    /workouts/{id}               # Update workout
DELETE /workouts/{id}               # Delete workout
```

### 💪 Exercises
```http
POST   /exercises                   # Add exercise
GET    /exercises/workout/{id}      # Exercises of a workout
GET    /exercises/{id}              # Get specific exercise
PUT    /exercises/{id}              # Update exercise
DELETE /exercises/{id}              # Remove exercise
```

## 📝 Usage Examples

### 1. Register user
```bash
curl -X POST http://localhost:8080/user \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@email.com",
    "password": "MyPass@123"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@email.com",
    "password": "MyPass@123"
  }'
```

### 3. Create back workout
```bash
curl -X POST http://localhost:8080/workouts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "workoutName": "Back Workout",
    "startDate": "2025-06-19"
  }'
```

### 4. Add exercise to workout
```bash
curl -X POST http://localhost:8080/exercises \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "exerciseName": "Lat Pulldown",
    "series": 4,
    "repetitions": 12,
    "workoutId": 1
  }'
```

## 💾 Database

### Data Model:
```
User (users)
├── userId (PK)
├── username
├── email (unique)
├── password (hash)
└── workouts[] (relationship)

Workout (workouts)
├── workoutId (PK)
├── workoutName
├── startDate
├── userId (FK)
└── exercises[] (relationship)

Exercise (exercises)
├── exerciseId (PK)
├── exerciseName
├── series
├── repetitions
└── workoutId (FK)
```

## ⚡ Redis Cache

The system uses intelligent caching for:

- **JWT Tokens:** 1-hour cache for login/logout
- **User data:** 30-minute cache
- **Workouts:** 15-minute cache
- **Exercises:** 15-minute cache

## 🔒 Security

- **Hashed passwords** with BCrypt
- **JWT tokens** with expiration
- **Ownership validation** (users only access their data)
- **Robust validations** in DTOs
- **Security headers** configured

## 🧪 Testing

### Test Types Implemented:
- **Unit Tests:** Services, converters, utilities
- **Repository Tests:** Database operations with H2
- **Integration Tests:** Full API workflows
- **Controller Tests:** HTTP endpoints with MockMvc
- **Security Tests:** JWT validation and authentication
- **Performance Tests:** Load testing and caching efficiency

### Run all tests:
```bash
./mvnw test
```

### Run specific test categories:
```bash
# Unit tests only
./mvnw test -Dtest="**/*Test" -DexcludeGroups="integration"

# Integration tests only
./mvnw test -Dtest="**/integration/*Test"

# Repository tests only
./mvnw test -Dtest="**/repository/*Test"

# Controller tests only
./mvnw test -Dtest="**/controller/*Test"

# Performance tests only
./mvnw test -Dtest="**/performance/*Test"

# Service tests only
./mvnw test -Dtest="**/service/*Test"
```

### Test with coverage:
```bash
./mvnw test jacoco:report
```

### Run tests with specific profiles:
```bash
# Tests with H2 database
./mvnw test -Dspring.profiles.active=test

# Tests with real database (requires Docker)
./mvnw test -Dspring.profiles.active=test-db
```

### Test specific classes:
```bash
# Test specific class
./mvnw test -Dtest=UserServiceTest

# Test specific method
./mvnw test -Dtest=UserServiceTest#shouldCreateUserSuccessfully

# Test multiple classes
./mvnw test -Dtest=UserServiceTest,WorkoutServiceTest
```

### Continuous testing (watch mode):
```bash
# Run tests automatically on file changes
./mvnw test -Dspring-boot.run.fork=false -Dspring.devtools.restart.enabled=true
```

### Generate test reports:
```bash
# Surefire reports
./mvnw surefire-report:report

# Coverage report (if JaCoCo is configured)
./mvnw jacoco:report
```

## 🐳 Docker

### Run everything with Docker:
```bash
# Infrastructure + application
docker-compose --profile full up -d

# Infrastructure only (development)
docker-compose up postgres redis -d
```

### Monitoring:
```bash
# View logs
docker-compose logs -f

# Statistics
docker stats

# Connect to PostgreSQL
docker exec -it simplegym-postgres psql -U gymuser -d simplegymdb

# Connect to Redis
docker exec -it simplegym-redis redis-cli -a redispassword
```

## 🔧 Configuration

### Environment variables (production):
```env
# Database
POSTGRES_DB=simplegymdb
POSTGRES_USER=gymuser
POSTGRES_PASSWORD=your_secure_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# JWT
JWT_SECRET=your-super-secure-jwt-key-256-bits
JWT_EXPIRATION=3600000
```

## 🧪 API Testing

### Health Check:
```bash
curl http://localhost:8080/actuator/health
```

### Check Redis cache:
```bash
# Connect to Redis
docker exec -it simplegym-redis redis-cli -a redispassword

# View cached tokens
keys jwt-token:*

# View user data
keys users::*
```

## 🎯 Use Cases

### Example: Creating a complete workout plan

1. **Register** user
2. **Login** and receive token
3. **Create workout** "Workout A - Chest and Triceps"
4. **Add exercises:**
   - Bench press: 4 sets x 10 reps
   - Incline bench press: 3 sets x 12 reps
   - Dips: 3 sets x 15 reps
   - Tricep pushdown: 4 sets x 12 reps

5. **Create another workout** "Workout B - Back and Biceps"
6. **Manage** all workouts in "My Workouts" screen

## 🚧 Future Improvements

- [ ] Web interface (React/Angular)
- [ ] Workout history tracking
- [ ] Progress statistics and charts
- [ ] Workout template system
- [ ] Workout sharing between users
- [ ] Wearable device integration
- [ ] Notification system
- [ ] Mobile API (React Native/Flutter)

## 👨‍💻 Development

### Run tests:
```bash
./mvnw test
```

### Production build:
```bash
./mvnw clean package -DskipTests
```

### Run with production profile:
```bash
java -jar target/simplegymapp-*.jar --spring.profiles.active=prod
```

### Code quality checks:
```bash
# Run checkstyle (if configured)
./mvnw checkstyle:check

# Run SpotBugs (if configured)
./mvnw spotbugs:check

# Run all quality checks
./mvnw verify
```

## 📊 Test Coverage

The project includes comprehensive test coverage across all layers:

- **Controllers:** HTTP endpoints, authentication, validation
- **Services:** Business logic, error handling, caching
- **Repositories:** Database operations, queries, transactions
- **Security:** JWT validation, access control
- **Integration:** End-to-end workflows, user scenarios
- **Performance:** Load testing, cache efficiency

### Coverage Goals:
- **Line Coverage:** > 80%
- **Branch Coverage:** > 70%
- **Method Coverage:** > 85%

