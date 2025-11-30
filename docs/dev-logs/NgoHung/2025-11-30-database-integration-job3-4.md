# Database Integration - Jobs 3 & 4 Completion Report

**Date:** November 30, 2025  
**Developer:** Ngo Hung  
**Status:** Job 3 ✅ COMPLETE | Job 4 ✅ COMPLETE

---

## Overview

Implemented storage endpoints for both Flashcards (Job 3) and Quizzes (Job 4), providing full CRUD functionality with authentication and authorization. Both jobs follow identical patterns for consistency and code reusability.

---

## Job 3: Flashcard Storage Endpoints ✅

### Requirements
- POST `/api/flashcards/save` - Save flashcard set (auto-saves via /generate)
- GET `/api/flashcards/history` - List user's flashcard sets
- GET `/api/flashcards/{id}` - Get specific flashcard set
- DELETE `/api/flashcards/{id}` - Delete flashcard set
- Authentication required (JWT)
- Authorization: Users can only access their own flashcards

### Implementation

#### 1. GET /api/flashcards/history
**Purpose:** List all flashcard sets owned by authenticated user  
**Authentication:** Required (JWT token)  
**Authorization:** User can only see their own flashcard sets  

**Code:**
```java
@GetMapping("/history")
public ResponseEntity<List<FlashcardSetResponse>> getHistory(Authentication authentication) {
    String username = authentication.getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    List<FlashcardSet> sets = flashcardSetRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    List<FlashcardSetResponse> response = sets.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
    
    return ResponseEntity.ok(response);
}
```

**Example Request:**
```bash
curl http://localhost:8080/api/flashcards/history \
  -H "Authorization: Bearer eyJhbGc..."
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "username": "testuser",
    "title": "AI Generated Flashcards",
    "studyMaterial": "Test content...",
    "createdAt": "2025-11-30T...",
    "updatedAt": "2025-11-30T...",
    "flashcards": [
      {
        "id": 1,
        "question": "What is...?",
        "answer": "It is...",
        "position": 0
      }
    ]
  }
]
```

---

#### 2. GET /api/flashcards/{id}
**Purpose:** Get specific flashcard set by ID  
**Authentication:** Required (JWT token)  
**Authorization:** User can only access their own flashcard sets (403 if not owner)  

**Code:**
```java
@GetMapping("/{id}")
public ResponseEntity<?> getFlashcardSet(@PathVariable Long id, Authentication authentication) {
    String username = authentication.getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    FlashcardSet set = flashcardSetRepository.findById(id)
        .orElse(null);
    
    if (set == null) {
        return ResponseEntity.notFound().build();
    }
    
    // Check ownership
    if (!set.getUser().getId().equals(user.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    return ResponseEntity.ok(convertToDto(set));
}
```

**Example Request:**
```bash
curl http://localhost:8080/api/flashcards/1 \
  -H "Authorization: Bearer eyJhbGc..."
```

**Expected Responses:**
- **200 OK**: Flashcard set data (if owned by user)
- **403 Forbidden**: If trying to access another user's flashcard set
- **404 Not Found**: If flashcard set doesn't exist

---

#### 3. DELETE /api/flashcards/{id}
**Purpose:** Delete flashcard set by ID  
**Authentication:** Required (JWT token)  
**Authorization:** User can only delete their own flashcard sets (403 if not owner)  

**Code:**
```java
@DeleteMapping("/{id}")
public ResponseEntity<?> deleteFlashcardSet(@PathVariable Long id, Authentication authentication) {
    String username = authentication.getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    FlashcardSet set = flashcardSetRepository.findById(id)
        .orElse(null);
    
    if (set == null) {
        return ResponseEntity.notFound().build();
    }
    
    // Check ownership
    if (!set.getUser().getId().equals(user.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    flashcardSetRepository.delete(set);
    return ResponseEntity.noContent().build();
}
```

**Example Request:**
```bash
curl -X DELETE http://localhost:8080/api/flashcards/1 \
  -H "Authorization: Bearer eyJhbGc..."
```

**Expected Responses:**
- **204 No Content**: Successfully deleted (CASCADE deletes all flashcards in set)
- **403 Forbidden**: If trying to delete another user's flashcard set
- **404 Not Found**: If flashcard set doesn't exist

---

#### 4. POST /api/flashcards/generate (Auto-saves)
**Note:** The existing `/generate` endpoint already saves flashcard sets to the database, so a separate `/save` endpoint is not needed.

**Code:**
```java
@PostMapping("/generate")
public ResponseEntity<?> generateFlashcards(@RequestBody FlashcardRequest request, Authentication authentication) {
    // ... validation ...
    
    String username = authentication.getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    // Service automatically creates FlashcardSet and saves to database
    List<Flashcard> flashcards = flashcardService.generateFlashcards(
        request.getStudyMaterial(),
        request.getCount(),
        user.getId(),
        "AI Generated Flashcards"
    );
    
    return ResponseEntity.ok(flashcards);
}
```

---

### Repository Method Added

**FlashcardSetRepository.java:**
```java
public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Long> {
    List<FlashcardSet> findByUserIdOrderByCreatedAtDesc(Long userId);
}
```

---

### DTO Created

**FlashcardSetResponse.java:**
```java
public class FlashcardSetResponse {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String studyMaterial;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FlashcardResponse> flashcards;
    
    // Getters, setters, constructors
}

public class FlashcardResponse {
    private Long id;
    private String question;
    private String answer;
    private Integer position;
    
    // Getters, setters, constructors
}
```

---

## Job 4: Quiz Storage Endpoints ✅

### Requirements
- POST `/api/quiz/save` - Save quiz set (auto-saves via /generate)
- GET `/api/quiz/history` - List user's quiz sets
- GET `/api/quiz/{id}` - Get specific quiz set
- DELETE `/api/quiz/{id}` - Delete quiz set
- Authentication required (JWT)
- Authorization: Users can only access their own quizzes

### Implementation

#### 1. GET /api/quiz/history
**Purpose:** List all quiz sets owned by authenticated user  
**Authentication:** Required (JWT token)  
**Authorization:** User can only see their own quiz sets  

**Code:**
```java
@GetMapping("/history")
public ResponseEntity<List<QuizSetResponse>> getHistory(Authentication authentication) {
    String username = authentication.getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    List<QuizSet> sets = quizSetRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    List<QuizSetResponse> response = sets.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
    
    return ResponseEntity.ok(response);
}
```

**Example Request:**
```bash
curl http://localhost:8080/api/quiz/history \
  -H "Authorization: Bearer eyJhbGc..."
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "username": "testuser",
    "title": "AI Generated Quiz",
    "studyMaterial": "Test content...",
    "difficulty": "medium",
    "createdAt": "2025-11-30T...",
    "updatedAt": "2025-11-30T...",
    "questions": [
      {
        "id": 1,
        "question": "What is...?",
        "optionA": "Answer A",
        "optionB": "Answer B",
        "optionC": "Answer C",
        "optionD": "Answer D",
        "correctAnswer": "A",
        "explanation": "Because...",
        "position": 0
      }
    ]
  }
]
```

---

#### 2. GET /api/quiz/{id}
**Purpose:** Get specific quiz set by ID  
**Authentication:** Required (JWT token)  
**Authorization:** User can only access their own quiz sets (403 if not owner)  

**Code:**
```java
@GetMapping("/{id}")
public ResponseEntity<?> getQuizSet(@PathVariable Long id, Authentication authentication) {
    String username = authentication.getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    QuizSet set = quizSetRepository.findById(id)
        .orElse(null);
    
    if (set == null) {
        return ResponseEntity.notFound().build();
    }
    
    // Check ownership
    if (!set.getUser().getId().equals(user.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    return ResponseEntity.ok(convertToDto(set));
}
```

**Example Request:**
```bash
curl http://localhost:8080/api/quiz/1 \
  -H "Authorization: Bearer eyJhbGc..."
```

**Expected Responses:**
- **200 OK**: Quiz set data (if owned by user)
- **403 Forbidden**: If trying to access another user's quiz set
- **404 Not Found**: If quiz set doesn't exist

---

#### 3. DELETE /api/quiz/{id}
**Purpose:** Delete quiz set by ID  
**Authentication:** Required (JWT token)  
**Authorization:** User can only delete their own quiz sets (403 if not owner)  

**Code:**
```java
@DeleteMapping("/{id}")
public ResponseEntity<?> deleteQuizSet(@PathVariable Long id, Authentication authentication) {
    String username = authentication.getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    QuizSet set = quizSetRepository.findById(id)
        .orElse(null);
    
    if (set == null) {
        return ResponseEntity.notFound().build();
    }
    
    // Check ownership
    if (!set.getUser().getId().equals(user.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    quizSetRepository.delete(set);
    return ResponseEntity.noContent().build();
}
```

**Example Request:**
```bash
curl -X DELETE http://localhost:8080/api/quiz/1 \
  -H "Authorization: Bearer eyJhbGc..."
```

**Expected Responses:**
- **204 No Content**: Successfully deleted (CASCADE deletes all quiz questions)
- **403 Forbidden**: If trying to delete another user's quiz set
- **404 Not Found**: If quiz set doesn't exist

---

#### 4. POST /api/quiz/generate (Auto-saves)
**Note:** The existing `/generate` endpoint already saves quiz sets to the database, so a separate `/save` endpoint is not needed.

**Code:**
```java
@PostMapping("/generate")
public ResponseEntity<?> generateQuiz(@RequestBody QuizRequest request, Authentication authentication) {
    // ... validation ...
    
    String username = authentication.getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    // Service automatically creates QuizSet and saves to database
    List<QuizQuestion> questions = quizService.generateQuiz(
        request.getStudyMaterial(),
        request.getCount(),
        request.getDifficulty(),
        user.getId(),
        "AI Generated Quiz"
    );
    
    return ResponseEntity.ok(questions);
}
```

---

### Repository Method Added

**QuizSetRepository.java:**
```java
public interface QuizSetRepository extends JpaRepository<QuizSet, Long> {
    List<QuizSet> findByUserIdOrderByCreatedAtDesc(Long userId);
}
```

---

### DTO Created

**QuizSetResponse.java:**
```java
public class QuizSetResponse {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String studyMaterial;
    private String difficulty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuizQuestionResponse> questions;
    
    // Getters, setters, constructors
}

public class QuizQuestionResponse {
    private Long id;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private String explanation;
    private Integer position;
    
    // Getters, setters, constructors
}
```

---

## Common Features (Both Jobs)

### 1. Authentication Enforcement
- All endpoints require valid JWT token in `Authorization: Bearer <token>` header
- Spring Security automatically validates token before controller method executes
- Returns **401 Unauthorized** if no token or invalid token

### 2. Authorization (Ownership Check)
- Each GET/{id} and DELETE/{id} endpoint verifies user owns the resource
- Prevents users from accessing/modifying other users' data
- Returns **403 Forbidden** if user tries to access another user's resource

### 3. Data Isolation
- Repository methods filter by `userId` to ensure data isolation
- `findByUserIdOrderByCreatedAtDesc(Long userId)` only returns current user's sets
- No cross-user data leakage possible

### 4. CASCADE DELETE
- Database foreign key constraints configured with `ON DELETE CASCADE`
- Deleting a FlashcardSet automatically deletes all associated Flashcards
- Deleting a QuizSet automatically deletes all associated QuizQuestions
- Maintains referential integrity

### 5. DTO Conversion
- Controller methods use DTOs instead of entities for responses
- Prevents exposing sensitive entity relationships
- Includes user information (id, username) in responses
- Consistent JSON structure for frontend consumption

---

## Database Schema (Recap)

### Flashcard Tables

**flashcard_sets:**
```sql
+----------------+--------------+------+-----+---------+----------------+
| Field          | Type         | Null | Key | Default | Extra          |
+----------------+--------------+------+-----+---------+----------------+
| id             | bigint       | NO   | PRI | NULL    | auto_increment |
| created_at     | datetime(6)  | NO   |     | NULL    |                |
| study_material | text         | YES  |     | NULL    |                |
| title          | varchar(255) | NO   |     | NULL    |                |
| updated_at     | datetime(6)  | NO   |     | NULL    |                |
| user_id        | bigint       | NO   | MUL | NULL    | FK to users    |
+----------------+--------------+------+-----+---------+----------------+
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
```

**flashcards:**
```sql
+----------+---------+------+-----+---------+----------------+
| Field    | Type    | Null | Key | Default | Extra          |
+----------+---------+------+-----+---------+----------------+
| id       | bigint  | NO   | PRI | NULL    | auto_increment |
| answer   | text    | NO   |     | NULL    |                |
| position | int     | NO   |     | NULL    |                |
| question | text    | NO   |     | NULL    |                |
| set_id   | bigint  | NO   | MUL | NULL    | FK to sets     |
+----------+---------+------+-----+---------+----------------+
FOREIGN KEY (set_id) REFERENCES flashcard_sets(id) ON DELETE CASCADE
```

### Quiz Tables

**quiz_sets:**
```sql
+----------------+--------------+------+-----+---------+----------------+
| Field          | Type         | Null | Key | Default | Extra          |
+----------------+--------------+------+-----+---------+----------------+
| id             | bigint       | NO   | PRI | NULL    | auto_increment |
| created_at     | datetime(6)  | NO   |     | NULL    |                |
| difficulty     | varchar(255) | NO   |     | NULL    |                |
| study_material | text         | YES  |     | NULL    |                |
| title          | varchar(255) | NO   |     | NULL    |                |
| updated_at     | datetime(6)  | NO   |     | NULL    |                |
| user_id        | bigint       | NO   | MUL | NULL    | FK to users    |
+----------------+--------------+------+-----+---------+----------------+
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
```

**quiz_questions:**
```sql
+----------------+--------------+------+-----+---------+----------------+
| Field          | Type         | Null | Key | Default | Extra          |
+----------------+--------------+------+-----+---------+----------------+
| id             | bigint       | NO   | PRI | NULL    | auto_increment |
| correct_answer | varchar(255) | NO   |     | NULL    |                |
| explanation    | text         | YES  |     | NULL    |                |
| option_a       | text         | NO   |     | NULL    |                |
| option_b       | text         | NO   |     | NULL    |                |
| option_c       | text         | NO   |     | NULL    |                |
| option_d       | text         | NO   |     | NULL    |                |
| position       | int          | NO   |     | NULL    |                |
| question       | text         | NO   |     | NULL    |                |
| quiz_set_id    | bigint       | NO   | MUL | NULL    | FK to quiz_sets|
+----------------+--------------+------+-----+---------+----------------+
FOREIGN KEY (quiz_set_id) REFERENCES quiz_sets(id) ON DELETE CASCADE
```

---

## Testing Commands

### Complete Testing Flow

#### 1. Register and Login
```bash
# Register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

# Login and get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Save token to variable (PowerShell)
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST -ContentType "application/json" `
  -Body '{"username":"testuser","password":"password123"}'
$token = $response.token
```

---

#### 2. Test Flashcard Endpoints

**Generate Flashcards (auto-saves):**
```bash
curl -X POST http://localhost:8080/api/flashcards/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d '{
    "studyMaterial": "Python is a high-level programming language",
    "count": 5
  }'
```

**Get Flashcard History:**
```bash
curl http://localhost:8080/api/flashcards/history \
  -H "Authorization: Bearer $token"
```

**Get Specific Flashcard Set:**
```bash
curl http://localhost:8080/api/flashcards/1 \
  -H "Authorization: Bearer $token"
```

**Delete Flashcard Set:**
```bash
curl -X DELETE http://localhost:8080/api/flashcards/1 \
  -H "Authorization: Bearer $token"
```

---

#### 3. Test Quiz Endpoints

**Generate Quiz (auto-saves):**
```bash
curl -X POST http://localhost:8080/api/quiz/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d '{
    "studyMaterial": "Machine learning is a subset of AI",
    "count": 5,
    "difficulty": "medium"
  }'
```

**Get Quiz History:**
```bash
curl http://localhost:8080/api/quiz/history \
  -H "Authorization: Bearer $token"
```

**Get Specific Quiz Set:**
```bash
curl http://localhost:8080/api/quiz/1 \
  -H "Authorization: Bearer $token"
```

**Delete Quiz Set:**
```bash
curl -X DELETE http://localhost:8080/api/quiz/1 \
  -H "Authorization: Bearer $token"
```

---

#### 4. Test Authorization (403 Forbidden)

```bash
# Login as second user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user2","email":"user2@example.com","password":"password123"}'

$response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST -ContentType "application/json" `
  -Body '{"username":"user2","password":"password123"}'
$token2 = $response2.token

# Try to access first user's flashcard set (should return 403)
curl http://localhost:8080/api/flashcards/1 \
  -H "Authorization: Bearer $token2"

# Try to delete first user's quiz set (should return 403)
curl -X DELETE http://localhost:8080/api/quiz/1 \
  -H "Authorization: Bearer $token2"
```

---

## Success Criteria Met

| Requirement | Job 3 | Job 4 | Evidence |
|-------------|-------|-------|----------|
| History endpoint | ✅ `/api/flashcards/history` | ✅ `/api/quiz/history` | Implemented in controllers |
| Get by ID endpoint | ✅ `/api/flashcards/{id}` | ✅ `/api/quiz/{id}` | With ownership check |
| Delete endpoint | ✅ `/api/flashcards/{id}` | ✅ `/api/quiz/{id}` | With ownership check |
| Save endpoint | ✅ Auto-save via /generate | ✅ Auto-save via /generate | No separate endpoint needed |
| Authentication required | ✅ Spring Security | ✅ Spring Security | JWT validation |
| Authorization (ownership) | ✅ User ID check | ✅ User ID check | 403 for other users |
| 401 without token | ✅ Automatic | ✅ Automatic | Spring Security default |
| 403 for other user's data | ✅ Explicit check | ✅ Explicit check | Tested |
| Data persists in MySQL | ✅ JPA repositories | ✅ JPA repositories | Verified in DB |
| CASCADE DELETE works | ✅ FK constraints | ✅ FK constraints | Tested |

---

## Code Structure Comparison

Both Job 3 and Job 4 use identical patterns:

### FlashcardController ↔ QuizController
```java
// Identical structure:
- GET /history → findByUserIdOrderByCreatedAtDesc()
- GET /{id} → findById() + ownership check
- DELETE /{id} → findById() + ownership check + delete()
- convertToDto() → Maps entity to response DTO
```

### FlashcardSetRepository ↔ QuizSetRepository
```java
// Same query method:
List<FlashcardSet> findByUserIdOrderByCreatedAtDesc(Long userId);
List<QuizSet> findByUserIdOrderByCreatedAtDesc(Long userId);
```

### DTOs
```java
// Parallel structure:
FlashcardSetResponse ↔ QuizSetResponse
FlashcardResponse ↔ QuizQuestionResponse
```

---

## Files Created/Modified

### New Files Created

**DTOs:**
```
src/main/java/ie/tcd/scss/aichat/dto/
├── FlashcardSetResponse.java     [NEW - Job 3]
├── FlashcardResponse.java        [NEW - Job 3]
├── QuizSetResponse.java          [NEW - Job 4]
└── QuizQuestionResponse.java     [NEW - Job 4]
```

**Repositories:**
```
src/main/java/ie/tcd/scss/aichat/repository/
├── FlashcardSetRepository.java   [MODIFIED - added findByUserIdOrderByCreatedAtDesc]
└── QuizSetRepository.java        [MODIFIED - added findByUserIdOrderByCreatedAtDesc]
```

### Modified Files

**Controllers:**
```
src/main/java/ie/tcd/scss/aichat/controller/
├── FlashcardController.java      [MODIFIED - added 3 endpoints]
│   ├── + GET /history
│   ├── + GET /{id}
│   └── + DELETE /{id}
└── QuizController.java           [MODIFIED - added 3 endpoints]
    ├── + GET /history
    ├── + GET /{id}
    └── + DELETE /{id}
```

---

## Next Steps

### ✅ Completed
- Job 1: Database Schema Setup
- Job 2: User Authentication System
- Job 3: Flashcard Storage Endpoints
- Job 4: Quiz Storage Endpoints

### ⏭️ Remaining Jobs
- **Job 5**: Error Handling (Global exception handler)
- **Job 6**: API Documentation (Swagger/OpenAPI)
- **Job 7**: Automated Testing (Integration tests for new endpoints)
- **Job 8**: Manual Testing (Postman collection)
- **Job 9**: Performance Optimization

---

## Lessons Learned

1. **Code Reusability**: Job 4 took ~1 hour because we copied and adapted Job 3's structure
2. **Consistent Patterns**: Using identical patterns for Flashcards and Quizzes reduces bugs
3. **DTO Importance**: Response DTOs prevent exposing entity internals and provide clean API
4. **Ownership Checks**: Critical to verify user owns resource before GET/DELETE operations
5. **CASCADE DELETE**: Database constraints handle cleanup automatically, no manual deletion needed
6. **Spring Security Integration**: Authentication parameter automatically populated from JWT

---

## Time Breakdown

- **Job 3**: ~2 hours
  - Repository methods: 15 minutes
  - DTOs creation: 30 minutes
  - Controller endpoints: 45 minutes
  - Testing and debugging: 30 minutes

- **Job 4**: ~1 hour
  - Copy and adapt from Job 3: 30 minutes
  - Quiz-specific modifications: 15 minutes
  - Testing: 15 minutes

**Total**: ~3 hours for both jobs

---

## References

- Spring Security Authentication: https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html
- JPA Cascade Types: https://docs.oracle.com/javaee/7/api/javax/persistence/CascadeType.html
- REST API Best Practices: https://restfulapi.net/
- HTTP Status Codes: https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
