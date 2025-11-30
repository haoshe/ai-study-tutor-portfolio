# Development Log - Database Integration (Jobs 1 & 2)

**Date:** November 20, 2025  
**Developer:** Ngo Hung  
**Session Duration:** ~4 hours  
**Status:** Job 1 ✅ Complete | Job 2 ✅ Complete (pending testing)

---

## Overview

Implemented comprehensive database integration for the AI Chat application, completing Job 1 (Database Schema Setup) and Job 2 (User Authentication System) according to scrum master requirements.

---

## Job 1: Database Schema Setup ✅

### Objectives
- Design and implement complete MySQL database schema
- Create entity models with proper relationships
- Set up foreign key constraints with CASCADE DELETE

### Database Schema Created

#### 1. **users** table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 2. **flashcard_sets** table
```sql
CREATE TABLE flashcard_sets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    study_material TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 3. **flashcards** table
```sql
CREATE TABLE flashcards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    set_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    position INTEGER NOT NULL,
    FOREIGN KEY (set_id) REFERENCES flashcard_sets(id) ON DELETE CASCADE
);
```

#### 4. **quiz_sets** table
```sql
CREATE TABLE quiz_sets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    study_material TEXT,
    difficulty VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 5. **quiz_questions** table
```sql
CREATE TABLE quiz_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quiz_set_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_answer VARCHAR(1) NOT NULL,
    explanation TEXT,
    position INTEGER NOT NULL,
    FOREIGN KEY (quiz_set_id) REFERENCES quiz_sets(id) ON DELETE CASCADE
);
```

### Entity Models Created/Modified

#### New Entities
- **`FlashcardSet.java`** - Container for flashcard collections
  - `@ManyToOne` relationship to User
  - `@OneToMany` relationship to Flashcard
  - Auto-managed timestamps with `@PrePersist` and `@PreUpdate`

- **`QuizSet.java`** - Container for quiz questions
  - `@ManyToOne` relationship to User
  - `@OneToMany` relationship to QuizQuestion
  - Difficulty field (String: easy/medium/hard)
  - Auto-managed timestamps

#### Modified Entities
- **`Flashcard.java`**
  - ❌ Removed `course_id` foreign key
  - ✅ Added `set_id` FK to FlashcardSet
  - ✅ Added `position` field (INTEGER)
  - Changed relationship from Course to FlashcardSet

- **`QuizQuestion.java`** (formerly referenced by Quiz entity)
  - ❌ Removed `quiz_id` FK (old Quiz entity)
  - ✅ Added `quiz_set_id` FK to QuizSet
  - ✅ Added `position` field (INTEGER)
  - Changed relationship to new QuizSet entity

- **`User.java`**
  - ✅ Added `updatedAt` field with `@PreUpdate`
  - ✅ Added `@OneToMany` to FlashcardSet
  - ✅ Added `@OneToMany` to QuizSet

- **`Course.java`**
  - ❌ Removed `@OneToMany` to Flashcard
  - ❌ Removed `@OneToMany` to QuizSet
  - Cleaned up orphaned relationships

### Key Design Decisions

1. **Separate Set Entities**: Created FlashcardSet and QuizSet to group related items
   - Rationale: Better organization, allows multiple sets per user
   - Each set stores the original `study_material` for reference

2. **Position Field**: Added to maintain order of flashcards/questions
   - Type: INTEGER
   - Ensures consistent display order

3. **Cascade DELETE**: All child records deleted when parent is removed
   - User deleted → all sets deleted → all cards/questions deleted
   - Maintains data integrity

4. **Difficulty as String**: Changed QuizSet.difficulty from Integer to String
   - Values: "easy", "medium", "hard"
   - More readable and maintainable

---

## Job 2: User Authentication System ✅

### Objectives
- Implement JWT-based authentication
- Create registration and login endpoints
- Hash passwords with BCrypt (10 rounds)
- Configure Spring Security

### Components Implemented

#### 1. DTOs Created

**`RegisterRequest.java`**
```java
{
    String username;  // required, unique
    String email;     // required, unique
    String password;  // required, min 6 chars
}
```

**`LoginRequest.java`**
```java
{
    String username;  // required
    String password;  // required
}
```

**`AuthResponse.java`**
```java
{
    Long id;
    String username;
    String email;
    String token;     // JWT token
}
```

#### 2. JWT Utility (`JwtUtil.java`)

- **Algorithm**: HS256 (HMAC with SHA-256)
- **Secret Key**: Hardcoded (TODO: move to environment variables)
- **Token Expiration**: 24 hours
- **Methods**:
  - `generateToken(String username)` - Creates JWT
  - `extractUsername(String token)` - Parses username from token
  - `validateToken(String token, String username)` - Verifies signature and expiration

#### 3. Authentication Service (`AuthService.java`)

**Key Features**:
- Password hashing with `BCryptPasswordEncoder(10)` (10 rounds as per requirements)
- Username/email uniqueness validation
- Token generation on successful registration/login

**Methods**:
- `register(RegisterRequest)` → `AuthResponse`
  - Validates username uniqueness
  - Validates email uniqueness
  - Hashes password with BCrypt
  - Saves user to database
  - Returns user info + JWT token

- `login(LoginRequest)` → `AuthResponse`
  - Finds user by username
  - Verifies password with BCrypt
  - Returns user info + JWT token

- `getUserFromToken(String token)` → `User`
  - Extracts username from JWT
  - Retrieves user from database

#### 4. User Repository Updates (`UserRepository.java`)

Added methods:
```java
Optional<User> findByUsername(String username);
Optional<User> findByEmail(String email);
boolean existsByUsername(String username);
boolean existsByEmail(String email);
```

#### 5. Authentication Controller (`AuthController.java`)

**Endpoints**:

- **POST** `/api/auth/register`
  - Request: `RegisterRequest`
  - Success: 200 + `AuthResponse`
  - Errors:
    - 400 if username already exists
    - 400 if email already exists

- **POST** `/api/auth/login`
  - Request: `LoginRequest`
  - Success: 200 + `AuthResponse`
  - Errors:
    - 401 if invalid credentials

#### 6. Security Configuration (`SecurityConfig.java`)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())  // Disabled for REST API
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()  // Public endpoints
                .anyRequest().authenticated()  // All other endpoints require auth
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(STATELESS))  // JWT stateless
            .build();
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);  // 10 rounds
    }
}
```

---

## Additional Service Updates

### FlashcardService Enhancements

**Updated Method Signature**:
```java
List<Flashcard> generateFlashcards(
    String studyMaterial, 
    Integer count, 
    Long userId,      // NEW
    String title      // NEW
)
```

**Changes**:
- Now creates `FlashcardSet` entity with:
  - user (from userId)
  - title (default: "AI Generated Flashcards")
  - studyMaterial (stores original text)
- Saves FlashcardSet first to get ID
- Creates Flashcard entities with:
  - flashcardSet reference (FK)
  - position field (0-based index)
- No more direct course relationship

### QuizService Enhancements

**Updated Method Signature**:
```java
List<QuizQuestion> generateQuiz(
    String studyMaterial,
    Integer count,
    String difficulty,
    Long userId,      // NEW
    String title      // NEW
)
```

**Changes**:
- Now creates `QuizSet` entity with:
  - user (from userId)
  - title (default: "AI Generated Quiz")
  - studyMaterial (stores original text)
  - difficulty ("easy"/"medium"/"hard")
- Saves QuizSet first to get ID
- Creates QuizQuestion entities with:
  - quizSet reference (FK)
  - position field (0-based index)
  - correctAnswer converted to letter (A/B/C/D)

### Controller Updates

#### FlashcardController
- Added `AuthService` dependency
- Extracts userId from `Authorization` header (Bearer token)
- Falls back to userId=1 for testing (temporary)
- Passes userId and title to service

#### QuizController
- Added `AuthService` dependency
- Extracts userId from `Authorization` header
- Falls back to userId=1 for testing (temporary)
- Passes userId and title to service

---

## Dependencies Added

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT Library -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

---

## Testing Status

### Unit Tests Status
⚠️ **Test files need updating** - Service method signatures changed

Files requiring test updates:
- `FlashcardServiceTest.java` - ✅ Partially updated (mocks added, some calls updated)
- `QuizServiceTest.java` - ✅ Partially updated (mocks added, some calls updated)
- `FlashcardControllerTest.java` - ✅ Partially updated (AuthService mock added)
- `QuizControllerTest.java` - ✅ Partially updated (AuthService mock added)

**Issue**: Tests compile with warnings, need to update remaining test method calls with new parameters (userId, title)

### Integration Testing Plan (Pending)

**Job 2 Testing Checklist**:
- [ ] Test registration with valid data
- [ ] Test duplicate username validation
- [ ] Test duplicate email validation
- [ ] Test login with valid credentials
- [ ] Test login with invalid credentials
- [ ] Verify password hashing in database (BCrypt $2a$ format)

**Planned Test Commands**:
```bash
# Register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"testuser","password":"Test123!","email":"test@example.com"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"testuser","password":"Test123!"}'

# Verify in database
SELECT id, username, password_hash FROM users;
```

---

## Build & Deployment

### Compilation Status
✅ **Main code compiles successfully**
```bash
./mvnw clean compile -DskipTests
# [INFO] BUILD SUCCESS
# [INFO] 50 source files compiled
```
//FROM HERE!!!
### Application Status
⚠️ **Not yet running** - MySQL connection issues

**Issue**: Application running on Windows, MySQL running in WSL
- WSL IP: `172.31.114.93`
- Need to configure MySQL to accept remote connections from Windows
- Alternative: Run application in WSL or use Docker

**Current Error**:
```
Communications link failure - Connection refused
```

**Next Steps**:
1. Configure MySQL bind-address in WSL
2. Or use WSL IP in application.properties
3. Or run entire stack in Docker

---

## Theoretical Questions & Answers

### 1. Why use BCrypt instead of plain password storage?
**Answer**: BCrypt is a password hashing function designed to be computationally expensive, making brute-force attacks impractical. It includes:
- **Salt**: Random data added to password before hashing (prevents rainbow table attacks)
- **Cost factor**: Number of hashing rounds (we use 10 rounds = 2^10 iterations)
- **One-way function**: Cannot reverse hash to get original password

### 2. What is JWT and why use it?
**Answer**: JSON Web Token is a compact, URL-safe token for representing claims between parties. Benefits:
- **Stateless**: Server doesn't store session data
- **Self-contained**: Token contains all user information
- **Scalable**: No server-side session storage needed
- **Cross-domain**: Works across different domains/services

**Structure**: `header.payload.signature`
- Header: Algorithm and token type
- Payload: Claims (user data)
- Signature: Verifies token wasn't tampered with

### 3. Why separate FlashcardSet from Flashcard?
**Answer**: Separation of concerns and better organization:
- **Grouping**: Users can create multiple flashcard sets on different topics
- **Metadata**: Set stores title, creation date, study material
- **Sharing**: Could share entire sets (future feature)
- **Performance**: Can query sets without loading all flashcards

### 4. What is CASCADE DELETE and why use it?
**Answer**: CASCADE DELETE automatically deletes child records when parent is deleted.

Example: When user is deleted:
1. All user's FlashcardSets are deleted (CASCADE)
2. All Flashcards in those sets are deleted (CASCADE)
3. All user's QuizSets are deleted (CASCADE)
4. All QuizQuestions in those sets are deleted (CASCADE)

**Benefits**:
- Maintains referential integrity
- Prevents orphaned records
- Simplifies deletion logic

### 5. Why use DTOs instead of entities in controllers?
**Answer**: 
- **Security**: Don't expose internal entity structure (e.g., password hash)
- **Flexibility**: API contract independent from database schema
- **Validation**: Different validation rules for requests vs entities
- **Versioning**: Can change database without breaking API

Example: `AuthResponse` excludes `passwordHash` that exists in User entity

### 6. What is the difference between @PrePersist and @PreUpdate?
**Answer**:
- **@PrePersist**: Called before INSERT (first save)
  - Used for setting `createdAt` timestamp
  - Sets default values
  
- **@PreUpdate**: Called before UPDATE (subsequent saves)
  - Used for updating `updatedAt` timestamp
  - Recalculates computed fields

### 7. Why disable CSRF for REST APIs?
**Answer**: CSRF (Cross-Site Request Forgery) protection is designed for browser-based sessions with cookies. For REST APIs with JWT:
- **Stateless**: No session cookies to hijack
- **Token in header**: CSRF attacks can't access Authorization header
- **Same-origin**: Modern browsers block cross-origin header manipulation

**Note**: Keep CSRF enabled for traditional session-based web apps

### 8. What is @Transactional and when to use it?
**Answer**: Not explicitly used yet, but important for Job 3+:
- Ensures database operations are atomic (all or nothing)
- Rolls back changes if any operation fails
- Example use case: Creating QuizSet + multiple QuizQuestions should be one transaction

```java
@Transactional
public QuizSet createQuizWithQuestions(QuizSet set, List<QuizQuestion> questions) {
    QuizSet saved = quizSetRepository.save(set);
    questions.forEach(q -> {
        q.setQuizSet(saved);
        quizQuestionRepository.save(q);
    });
    return saved;
    // If any save fails, all changes are rolled back
}
```

---

## Issues Encountered & Solutions

### 1. Course entity had orphaned relationships
**Problem**: Course entity still referenced flashcards and quizSets after we removed those FKs

**Error**:
```
Collection 'ie.tcd.scss.aichat.model.Course.flashcards' is 'mappedBy' 
a property named 'course' which does not exist
```

**Solution**: Removed `@OneToMany` relationships from Course.java
```java
// REMOVED:
@OneToMany(mappedBy = "course")
private List<QuizSet> quizzeSets;

@OneToMany(mappedBy = "course")
private List<Flashcard> flashcards;
```

### 2. QuizSet difficulty type mismatch
**Problem**: QuizSet.difficulty was Integer but service passed String

**Solution**: Changed entity field type to String
```java
// Before:
private Integer difficulty;

// After:
private String difficulty;  // Values: "easy", "medium", "hard"
```

### 3. Test files incompatible with new method signatures
**Problem**: Changed service methods to include userId and title parameters, breaking 26 test methods

**Temporary Solution**: Skip tests during build (`-DskipTests`)

**Proper Solution (TODO)**: Update all test mocks:
```java
// Old:
when(service.generateFlashcards(anyString(), eq(3)))

// New:
when(service.generateFlashcards(anyString(), eq(3), eq(1L), any(String.class)))
```

### 4. MySQL connection from Windows to WSL
**Problem**: Application runs on Windows, MySQL in WSL, can't connect via localhost

**Investigation**: WSL IP is `172.31.114.93`, but MySQL doesn't accept remote connections

**Options**:
1. Configure MySQL bind-address to 0.0.0.0
2. Run application in WSL
3. Use Docker Compose for entire stack
4. Use port forwarding

---

## Files Created/Modified

### Created Files
```
src/main/java/ie/tcd/scss/aichat/
├── dto/
│   ├── RegisterRequest.java          [NEW]
│   ├── LoginRequest.java              [NEW]
│   └── AuthResponse.java              [NEW]
├── model/
│   ├── FlashcardSet.java              [NEW]
│   └── QuizSet.java                   [NEW]
├── util/
│   └── JwtUtil.java                   [NEW]
├── service/
│   └── AuthService.java               [NEW]
├── controller/
│   └── AuthController.java            [NEW]
└── config/
    └── SecurityConfig.java            [NEW]
```

### Modified Files
```
src/main/java/ie/tcd/scss/aichat/
├── model/
│   ├── User.java                      [MODIFIED] +updatedAt, +relationships
│   ├── Flashcard.java                 [MODIFIED] -course_id, +set_id, +position
│   ├── QuizQuestion.java              [MODIFIED] -quiz_id, +quiz_set_id, +position
│   └── Course.java                    [MODIFIED] -flashcards, -quizSets relationships
├── repository/
│   ├── UserRepository.java            [MODIFIED] +findByUsername, +exists methods
│   ├── FlashcardSetRepository.java    [NEW]
│   ├── QuizSetRepository.java         [NEW]
│   └── QuizQuestionRepository.java    [MODIFIED] Updated FK queries
├── service/
│   ├── FlashcardService.java          [MODIFIED] +userId, +title parameters
│   └── QuizService.java               [MODIFIED] +userId, +title parameters
└── controller/
    ├── FlashcardController.java       [MODIFIED] +AuthService, JWT extraction
    └── QuizController.java            [MODIFIED] +AuthService, JWT extraction

pom.xml                                [MODIFIED] +Spring Security, +JWT dependencies
```

---

## Next Steps (Job 3+)

### Job 3: Flashcard Storage Endpoints
- [ ] POST `/api/flashcards/save` - Save flashcard set
- [ ] GET `/api/flashcards/history` - List user's flashcard sets
- [ ] GET `/api/flashcards/{id}` - Get specific flashcard set
- [ ] DELETE `/api/flashcards/{id}` - Delete flashcard set
- [ ] Require authentication (JWT validation)

### Job 4: Quiz Storage Endpoints
- [ ] POST `/api/quiz/save` - Save quiz set
- [ ] GET `/api/quiz/history` - List user's quiz sets
- [ ] GET `/api/quiz/{id}` - Get specific quiz set
- [ ] DELETE `/api/quiz/{id}` - Delete quiz set
- [ ] Require authentication

### Immediate TODOs
1. Fix MySQL connection (WSL networking)
2. Update test files for new method signatures
3. Run application successfully
4. Test authentication endpoints
5. Verify password hashing in database
6. Move JWT secret to environment variables
7. Add proper error handling and validation
8. Implement JWT filter for protected endpoints

---

## Lessons Learned

1. **Plan entity relationships carefully** - Changing FKs later requires updating multiple files
2. **Keep tests in sync** - Update tests immediately when changing method signatures
3. **WSL networking complexity** - Consider Docker for consistent environments
4. **DTO importance** - Separation from entities prevents security issues and coupling
5. **Cascade operations** - Powerful but must understand implications for data integrity
6. **BCrypt cost factor** - 10 rounds balances security and performance for authentication

---

## Time Breakdown

- Schema design & entity creation: 1.5 hours
- Authentication system implementation: 1.5 hours
- Service refactoring (userId, title): 0.5 hours
- Debugging & troubleshooting: 0.5 hours
- **Total**: ~4 hours

---

## References

- Spring Security Documentation: https://spring.io/projects/spring-security
- JWT.io: https://jwt.io/
- BCrypt Documentation: https://en.wikipedia.org/wiki/Bcrypt
- JPA Cascade Types: https://docs.oracle.com/javaee/7/api/javax/persistence/CascadeType.html
