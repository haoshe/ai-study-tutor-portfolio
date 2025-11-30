# Job 5: Error Handling - Class Relationships

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Request                           │
│                 (e.g., GET /api/flashcards/999)                 │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Security Filter                        │
│                   (JwtAuthenticationFilter)                      │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CONTROLLERS                                 │
│  ┌──────────────────────┐      ┌──────────────────────┐        │
│  │ FlashcardController  │      │   QuizController     │        │
│  │                      │      │                      │        │
│  │ - getFlashcardSet()  │      │ - getQuizSet()       │        │
│  │ - deleteFlashcard()  │      │ - deleteQuizSet()    │        │
│  │ - generateFlashcard()│      │ - generateQuiz()     │        │
│  └──────────┬───────────┘      └──────────┬───────────┘        │
│             │                              │                     │
│             │  throws exceptions           │                     │
│             ▼                              ▼                     │
│  ┌──────────────────────────────────────────────────┐          │
│  │         CUSTOM EXCEPTIONS                         │          │
│  │  ResourceNotFoundException                        │          │
│  │  ForbiddenException                              │          │
│  │  IllegalArgumentException                        │          │
│  └──────────────────────┬───────────────────────────┘          │
└─────────────────────────┼────────────────────────────────────────┘
                          │
                          │ caught by
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│              @RestControllerAdvice                               │
│           GlobalExceptionHandler                                 │
│  ┌───────────────────────────────────────────────────┐          │
│  │ @ExceptionHandler methods:                         │          │
│  │ - handleResourceNotFoundException()    → 404       │          │
│  │ - handleForbiddenException()           → 403       │          │
│  │ - handleIllegalArgumentException()     → 400       │          │
│  │ - handleBadCredentialsException()      → 401       │          │
│  │ - handleRuntimeException()             → 400/500   │          │
│  │ - handleGeneralException()             → 500       │          │
│  └───────────────────┬───────────────────────────────┘          │
└──────────────────────┼──────────────────────────────────────────┘
                       │
                       │ creates
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ErrorResponse DTO                           │
│  {                                                               │
│    "timestamp": "2025-11-30T17:13:45.123",                      │
│    "status": 404,                                               │
│    "error": "Not Found",                                        │
│    "message": "FlashcardSet not found with id: '999'",         │
│    "path": "/api/flashcards/999"                               │
│  }                                                              │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Client Response (JSON)                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## Class Relationships Diagram

```
                    ┌─────────────────────┐
                    │   RuntimeException  │
                    │   (Java Built-in)   │
                    └──────────┬──────────┘
                               │ extends
                ┌──────────────┼──────────────┐
                │              │              │
                ▼              ▼              ▼
   ┌────────────────────┐  ┌──────────────┐  ┌───────────────┐
   │ ResourceNotFound   │  │  Forbidden   │  │IllegalArgument│
   │   Exception        │  │  Exception   │  │  Exception    │
   │                    │  │              │  │ (Java Built-in)│
   └─────────┬──────────┘  └──────┬───────┘  └───────┬───────┘
             │                    │                   │
             │ thrown by          │ thrown by         │ thrown by
             │                    │                   │
             └────────────────────┼───────────────────┘
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │  FlashcardController     │
                    │  QuizController          │
                    │  AuthController          │
                    └────────────┬─────────────┘
                                 │
                                 │ exceptions caught by
                                 ▼
                    ┌──────────────────────────┐
                    │ GlobalExceptionHandler   │
                    │  @RestControllerAdvice   │
                    │                          │
                    │ @ExceptionHandler        │
                    │ - ResourceNotFound       │
                    │ - Forbidden              │
                    │ - IllegalArgument        │
                    │ - BadCredentials         │
                    │ - RuntimeException       │
                    │ - Exception              │
                    └────────────┬─────────────┘
                                 │
                                 │ creates
                                 ▼
                    ┌──────────────────────────┐
                    │    ErrorResponse         │
                    │    (DTO)                 │
                    │                          │
                    │ - timestamp              │
                    │ - status                 │
                    │ - error                  │
                    │ - message                │
                    │ - path                   │
                    └──────────────────────────┘
```

---

## Exception Hierarchy

```
java.lang.Throwable
    │
    └── java.lang.Exception
            │
            └── java.lang.RuntimeException (unchecked)
                    │
                    ├── ResourceNotFoundException [CUSTOM]
                    │   Used for: 404 Not Found errors
                    │   Example: User not found, FlashcardSet not found
                    │
                    ├── ForbiddenException [CUSTOM]
                    │   Used for: 403 Forbidden errors
                    │   Example: Accessing another user's resources
                    │
                    ├── IllegalArgumentException [Java Built-in]
                    │   Used for: 400 Bad Request errors
                    │   Example: Empty study material, invalid difficulty
                    │
                    ├── BadCredentialsException [Spring Security]
                    │   Used for: 401 Unauthorized errors
                    │   Example: Wrong password
                    │
                    └── UsernameNotFoundException [Spring Security]
                        Used for: 401 Unauthorized errors
                        Example: User doesn't exist
```

---

## Flow: Request → Exception → Response

### Example 1: FlashcardSet Not Found (404)

```
1. Client Request
   GET /api/flashcards/999
   Authorization: Bearer <token>

2. FlashcardController.getFlashcardSet(999)
   │
   ├─→ userRepository.findByUsername("testuser")
   │   └─→ User found ✓
   │
   └─→ flashcardSetRepository.findById(999)
       └─→ Optional.empty()
           └─→ .orElseThrow(() -> new ResourceNotFoundException("FlashcardSet", "id", 999))

3. Exception thrown: ResourceNotFoundException
   message = "FlashcardSet not found with id: '999'"

4. GlobalExceptionHandler catches it
   @ExceptionHandler(ResourceNotFoundException.class)
   │
   └─→ Creates ErrorResponse:
       {
         "timestamp": "2025-11-30T17:13:45.123",
         "status": 404,
         "error": "Not Found",
         "message": "FlashcardSet not found with id: '999'",
         "path": "/api/flashcards/999"
       }

5. Response sent to client
   HTTP/1.1 404 Not Found
   Content-Type: application/json
   { ErrorResponse JSON }
```

---

### Example 2: Access Denied (403)

```
1. Client Request
   GET /api/flashcards/1
   Authorization: Bearer <user2_token>

2. FlashcardController.getFlashcardSet(1)
   │
   ├─→ userRepository.findByUsername("user2")
   │   └─→ User found (id=2) ✓
   │
   ├─→ flashcardSetRepository.findById(1)
   │   └─→ FlashcardSet found (owner: user1, id=1) ✓
   │
   └─→ Ownership check:
       if (!flashcardSet.getUser().getId().equals(user.getId()))
       if (!(1 == 2))  // user1 != user2
           └─→ throw new ForbiddenException("You do not have permission...")

3. Exception thrown: ForbiddenException

4. GlobalExceptionHandler catches it
   @ExceptionHandler(ForbiddenException.class)
   │
   └─→ Creates ErrorResponse:
       {
         "status": 403,
         "error": "Forbidden",
         "message": "You do not have permission to access this flashcard set",
         "path": "/api/flashcards/1"
       }

5. Response sent to client
   HTTP/1.1 403 Forbidden
```

---

### Example 3: Validation Error (400)

```
1. Client Request
   POST /api/flashcards/generate
   { "studyMaterial": "", "count": 5 }

2. FlashcardController.generateFlashcards()
   │
   └─→ Validation:
       if (request.getStudyMaterial().trim().isEmpty())
           └─→ throw new IllegalArgumentException("Study material cannot be empty")

3. Exception thrown: IllegalArgumentException

4. GlobalExceptionHandler catches it
   @ExceptionHandler(IllegalArgumentException.class)
   │
   └─→ Creates ErrorResponse:
       {
         "status": 400,
         "error": "Bad Request",
         "message": "Study material cannot be empty",
         "path": "/api/flashcards/generate"
       }

5. Response sent to client
   HTTP/1.1 400 Bad Request
```

---

## Component Responsibilities

### 1. Custom Exception Classes
**Purpose:** Represent specific error conditions  
**Responsibility:** Carry error message and context  
**Location:** `src/main/java/ie/tcd/scss/aichat/exception/`

```java
// ResourceNotFoundException.java
public class ResourceNotFoundException extends RuntimeException {
    // Constructor with formatted message
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}

// ForbiddenException.java
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
```

---

### 2. GlobalExceptionHandler
**Purpose:** Centralized exception handling  
**Responsibility:** 
- Catch all exceptions from controllers
- Convert exceptions to ErrorResponse
- Set appropriate HTTP status codes
- Log errors (optional)

**Annotations:**
- `@RestControllerAdvice` - Global controller advice for REST APIs
- `@ExceptionHandler(ExceptionType.class)` - Handle specific exception type

**Location:** `src/main/java/ie/tcd/scss/aichat/exception/GlobalExceptionHandler.java`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(...) {
        // Create ErrorResponse
        // Return 404
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(...) {
        // Create ErrorResponse
        // Return 403
    }
    
    // More handlers...
}
```

---

### 3. ErrorResponse DTO
**Purpose:** Standardized error response format  
**Responsibility:** 
- Hold error information (status, message, path, timestamp)
- Serialize to JSON
- Provide consistent structure for frontend

**Location:** `src/main/java/ie/tcd/scss/aichat/dto/ErrorResponse.java`

```java
public class ErrorResponse {
    private LocalDateTime timestamp;  // When error occurred
    private int status;                // HTTP status code
    private String error;              // Error type
    private String message;            // Detailed message
    private String path;               // Request path
}
```

---

### 4. Controllers
**Purpose:** Handle HTTP requests  
**Responsibility:** 
- Validate input
- Call services
- **Throw exceptions** (don't handle them)
- Return success responses

**Location:** `src/main/java/ie/tcd/scss/aichat/controller/`

```java
@RestController
@RequestMapping("/api/flashcards")
public class FlashcardController {
    
    @GetMapping("/{id}")
    public ResponseEntity<FlashcardSetResponse> getFlashcardSet(...) {
        // Validate
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException(...));  // ← Throw, don't catch
        
        // Business logic
        FlashcardSet set = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(...));  // ← Throw, don't catch
        
        // Check permissions
        if (!set.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(...);  // ← Throw, don't catch
        }
        
        // Return success
        return ResponseEntity.ok(convertToDto(set));
    }
}
```

---

## Data Flow Sequence

```
┌────────┐     ┌────────────┐     ┌──────────┐     ┌─────────────┐     ┌──────────┐
│ Client │────▶│ Controller │────▶│ Service  │────▶│ Repository  │────▶│ Database │
└────────┘     └─────┬──────┘     └──────────┘     └─────────────┘     └──────────┘
                     │
                     │ throws exception
                     ▼
              ┌─────────────────┐
              │ GlobalException │
              │    Handler      │
              └────────┬────────┘
                       │ creates
                       ▼
              ┌─────────────────┐
              │  ErrorResponse  │
              └────────┬────────┘
                       │
┌────────┐             │
│ Client │◀────────────┘
└────────┘
```

---

## Exception Mapping Table

| Exception Type | HTTP Status | Error Type | Use Case |
|----------------|-------------|------------|----------|
| `ResourceNotFoundException` | 404 | Not Found | User/FlashcardSet/QuizSet not found |
| `ForbiddenException` | 403 | Forbidden | Accessing other user's resources |
| `IllegalArgumentException` | 400 | Bad Request | Empty input, invalid format |
| `BadCredentialsException` | 401 | Unauthorized | Wrong password |
| `UsernameNotFoundException` | 401 | Unauthorized | User doesn't exist |
| `RuntimeException` (with "already exists") | 400 | Bad Request | Duplicate username/email |
| `RuntimeException` (other) | 500 | Internal Server Error | Unexpected errors |
| `Exception` | 500 | Internal Server Error | Catch-all for unknown errors |

---

## Key Design Patterns Used

### 1. **Exception Handling Pattern**
- Controllers throw exceptions
- GlobalExceptionHandler catches them
- Single Responsibility: Controllers don't handle errors

### 2. **DTO Pattern**
- ErrorResponse is a Data Transfer Object
- Decouples internal exception structure from API response
- Allows API evolution without changing exception classes

### 3. **Centralized Configuration**
- `@RestControllerAdvice` applies to all controllers
- One place to modify error handling logic
- Consistent error responses across entire application

### 4. **Fail-Fast Pattern**
- Validate input early
- Throw exceptions immediately
- Don't proceed with invalid data

---

## Benefits of This Architecture

### ✅ **Separation of Concerns**
- Controllers: Business logic
- GlobalExceptionHandler: Error handling
- ErrorResponse: Response format
- Custom Exceptions: Error representation

### ✅ **DRY (Don't Repeat Yourself)**
- Error handling code written once
- Reused across all controllers
- No duplicate error response building

### ✅ **Consistency**
- All errors follow same JSON structure
- Frontend can parse all errors the same way
- Easy to document and test

### ✅ **Maintainability**
- Add new exception types easily
- Modify error format in one place
- Clear responsibility boundaries

### ✅ **Type Safety**
- Controllers return specific types: `ResponseEntity<FlashcardSetResponse>`
- Not generic: `ResponseEntity<?>`
- Compile-time checking

---

## Example: Complete Flow

```java
// 1. Controller throws exception
@GetMapping("/{id}")
public ResponseEntity<FlashcardSetResponse> getFlashcardSet(@PathVariable Long id, ...) {
    FlashcardSet set = flashcardSetRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("FlashcardSet", "id", id));
    // ... rest of method
}

// 2. GlobalExceptionHandler catches it
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
        ResourceNotFoundException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        404,
        "Not Found",
        ex.getMessage(),  // "FlashcardSet not found with id: '999'"
        request.getDescription(false).replace("uri=", "")  // "/api/flashcards/999"
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
}

// 3. ErrorResponse serialized to JSON
{
  "timestamp": "2025-11-30T17:13:45.123",
  "status": 404,
  "error": "Not Found",
  "message": "FlashcardSet not found with id: '999'",
  "path": "/api/flashcards/999"
}

// 4. Client receives HTTP 404 with JSON body
```

---

## Summary

**4 New Classes Created:**
1. `ResourceNotFoundException` - Custom exception for 404 errors
2. `ForbiddenException` - Custom exception for 403 errors
3. `GlobalExceptionHandler` - `@RestControllerAdvice` for centralized handling
4. `ErrorResponse` - DTO for standardized error format

**Relationships:**
- Controllers **throw** custom exceptions
- GlobalExceptionHandler **catches** exceptions
- GlobalExceptionHandler **creates** ErrorResponse
- ErrorResponse **serializes** to JSON for client

**Benefits:**
- Clean controller code
- Consistent error responses
- Easy to maintain and extend
- Type-safe return types
