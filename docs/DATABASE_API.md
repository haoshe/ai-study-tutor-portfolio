# Database API Documentation

This document describes all REST API endpoints for the AI Chat Study Assistant application.

## Base URL
```
http://localhost:8080/api
```

## Authentication

All endpoints except `/auth/register` and `/auth/login` require JWT authentication.

### Authentication Header
```
Authorization: Bearer <your_jwt_token>
```

---

## Endpoint Summary

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register new user | ❌ |
| POST | `/auth/login` | User login | ❌ |
| POST | `/flashcards/generate` | Generate flashcards | ✅ |
| GET | `/flashcards/history` | Get user's flashcard sets | ✅ |
| GET | `/flashcards/{id}` | Get flashcard set by ID | ✅ |
| DELETE | `/flashcards/{id}` | Delete flashcard set | ✅ |
| POST | `/quiz/generate` | Generate quiz questions | ✅ |
| GET | `/quiz/history` | Get user's quiz sets | ✅ |
| GET | `/quiz/{id}` | Get quiz set by ID | ✅ |
| DELETE | `/quiz/{id}` | Delete quiz set | ✅ |

---

## Authentication Endpoints

### 1. Register User
**POST** `/auth/register`

Create a new user account.

**Required Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Request Schema:**
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| username | string | Yes | Min: 3 chars, Unique | Username for login |
| password | string | Yes | Min: 6 chars | User password (will be encrypted) |

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTcwMTM2MDAwMCwiZXhwIjoxNzAxNDQ2NDAwfQ.signature",
  "username": "testuser"
}
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| token | string | JWT token valid for 24 hours |
| username | string | Registered username |

**Error Responses:**
- `400 Bad Request`: Username already exists or invalid input
  ```json
  {
    "status": 400,
    "message": "Username already exists"
  }
  ```
- `500 Internal Server Error`: Registration failed
  ```json
  {
    "status": 500,
    "message": "Registration failed: Database error"
  }
  ```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

---

### 2. Login
**POST** `/auth/login`

Authenticate user and receive JWT token.

**Required Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Request Schema:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| username | string | Yes | Registered username |
| password | string | Yes | User password |

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTcwMTM2MDAwMCwiZXhwIjoxNzAxNDQ2NDAwfQ.signature",
  "username": "testuser"
}
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| token | string | JWT token valid for 24 hours |
| username | string | Authenticated username |

**Error Responses:**
- `401 Unauthorized`: Invalid credentials
  ```json
  {
    "status": 401,
    "message": "Invalid username or password"
  }
  ```
- `500 Internal Server Error`: Login failed
  ```json
  {
    "status": 500,
    "message": "Login failed: Internal error"
  }
  ```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

---

## Flashcard Endpoints

### 3. Generate Flashcards
**POST** `/flashcards/generate`

Generate flashcards from study material using AI.

**Required Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "studyMaterial": "Newton's laws of motion describe the relationship between forces and motion. The first law states that an object at rest stays at rest unless acted upon by an external force. The second law states that F=ma. The third law states that for every action there is an equal and opposite reaction.",
  "count": 5
}
```

**Request Schema:**
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| studyMaterial | string | Yes | Not empty | Study content to generate flashcards from |
| count | integer | Yes | > 0 | Number of flashcards to generate |

**Success Response (200 OK):**
```json
[
  {
    "question": "What is Newton's First Law?",
    "answer": "An object at rest stays at rest, and an object in motion stays in motion unless acted upon by an external force."
  },
  {
    "question": "What does Newton's Second Law state?",
    "answer": "F = ma (Force equals mass times acceleration)"
  },
  {
    "question": "What is Newton's Third Law?",
    "answer": "For every action, there is an equal and opposite reaction."
  }
]
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| question | string | Flashcard question |
| answer | string | Flashcard answer |

**Error Responses:**
- `400 Bad Request`: Study material is empty or invalid
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Study material cannot be empty",
    "path": "/api/flashcards/generate"
  }
  ```
- `401 Unauthorized`: Missing or invalid JWT token
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "path": "/api/flashcards/generate"
  }
  ```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/flashcards/generate \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "studyMaterial": "Newton'\''s laws of motion describe the relationship between forces and motion...",
    "count": 5
  }'
```

---

### 4. Get Flashcard History
**GET** `/flashcards/history`

Retrieve all flashcard sets created by the authenticated user, ordered by creation date (newest first).

**Required Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "userId": 1,
    "username": "testuser",
    "title": "AI Generated Flashcards",
    "studyMaterial": "Newton's laws of motion describe the relationship between forces and motion...",
    "createdAt": "2025-11-30T10:30:00",
    "updatedAt": "2025-11-30T10:30:00",
    "flashcards": [
      {
        "id": 1,
        "question": "What is Newton's First Law?",
        "answer": "An object at rest stays at rest...",
        "position": 0
      },
      {
        "id": 2,
        "question": "What does Newton's Second Law state?",
        "answer": "F = ma (Force equals mass times acceleration)",
        "position": 1
      }
    ]
  },
  {
    "id": 2,
    "userId": 1,
    "username": "testuser",
    "title": "AI Generated Flashcards",
    "studyMaterial": "Photosynthesis is the process...",
    "createdAt": "2025-11-29T15:20:00",
    "updatedAt": "2025-11-29T15:20:00",
    "flashcards": [...]
  }
]
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| id | long | Flashcard set ID |
| userId | long | Owner's user ID |
| username | string | Owner's username |
| title | string | Set title |
| studyMaterial | string | Original study material used for generation |
| createdAt | datetime | Creation timestamp (ISO-8601) |
| updatedAt | datetime | Last update timestamp (ISO-8601) |
| flashcards | array | List of flashcard objects in this set |
| flashcards[].id | long | Flashcard ID |
| flashcards[].question | string | Question text |
| flashcards[].answer | string | Answer text |
| flashcards[].position | integer | Order position in set (0-based) |

**Error Responses:**
- `401 Unauthorized`: Missing or invalid JWT token
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "path": "/api/flashcards/history"
  }
  ```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/flashcards/history \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 5. Get Flashcard Set by ID
**GET** `/flashcards/{id}`

Retrieve a specific flashcard set by its ID. User must be the owner.

**Required Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**URL Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | long | Yes | Flashcard set ID |

**Success Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "username": "testuser",
  "title": "AI Generated Flashcards",
  "studyMaterial": "Newton's laws of motion describe the relationship between forces and motion...",
  "createdAt": "2025-11-30T10:30:00",
  "updatedAt": "2025-11-30T10:30:00",
  "flashcards": [
    {
      "id": 1,
      "question": "What is Newton's First Law?",
      "answer": "An object at rest stays at rest unless acted upon by an external force.",
      "position": 0
    },
    {
      "id": 2,
      "question": "What does Newton's Second Law state?",
      "answer": "F = ma (Force equals mass times acceleration)",
      "position": 1
    }
  ]
}
```

**Response Schema:**
Same as flashcard history item (see endpoint #4)

**Error Responses:**
- `401 Unauthorized`: Missing or invalid JWT token
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "path": "/api/flashcards/1"
  }
  ```
- `403 Forbidden`: User does not own this flashcard set
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 403,
    "error": "Forbidden",
    "message": "You do not have permission to access this flashcard set",
    "path": "/api/flashcards/1"
  }
  ```
- `404 Not Found`: Flashcard set not found
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "FlashcardSet not found with id: 1",
    "path": "/api/flashcards/1"
  }
  ```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/flashcards/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 6. Delete Flashcard Set
**DELETE** `/flashcards/{id}`

Delete a flashcard set by its ID. User must be the owner. This also deletes all flashcards in the set (CASCADE).

**Required Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**URL Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | long | Yes | Flashcard set ID to delete |

**Success Response (204 No Content):**
```
(Empty body)
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid JWT token
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "path": "/api/flashcards/1"
  }
  ```
- `403 Forbidden`: User does not own this flashcard set
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 403,
    "error": "Forbidden",
    "message": "You do not have permission to delete this flashcard set",
    "path": "/api/flashcards/1"
  }
  ```
- `404 Not Found`: Flashcard set not found
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "FlashcardSet not found with id: 1",
    "path": "/api/flashcards/1"
  }
  ```

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/flashcards/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## Quiz Endpoints

### 7. Generate Quiz
**POST** `/quiz/generate`

Generate multiple choice quiz questions from study material using AI.

**Required Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "studyMaterial": "Photosynthesis is the process by which plants convert light energy into chemical energy stored in glucose. It occurs in the chloroplasts and requires sunlight, water, and carbon dioxide. The process produces oxygen as a byproduct.",
  "count": 5,
  "difficulty": "medium"
}
```

**Request Schema:**
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| studyMaterial | string | Yes | Not empty | Study content to generate quiz from |
| count | integer | Yes | > 0 | Number of questions to generate |
| difficulty | string | No | "easy", "medium", or "hard" | Quiz difficulty level (default: medium) |

**Success Response (200 OK):**
```json
[
  {
    "question": "What is the primary purpose of photosynthesis?",
    "optionA": "To produce oxygen",
    "optionB": "To convert light energy into chemical energy",
    "optionC": "To absorb carbon dioxide",
    "optionD": "To create water",
    "correctAnswer": "B",
    "explanation": "Photosynthesis primarily converts light energy from the sun into chemical energy stored in glucose molecules. While oxygen is produced as a byproduct, the main purpose is energy conversion."
  },
  {
    "question": "Where does photosynthesis occur in plant cells?",
    "optionA": "Mitochondria",
    "optionB": "Nucleus",
    "optionC": "Chloroplasts",
    "optionD": "Cell membrane",
    "correctAnswer": "C",
    "explanation": "Photosynthesis takes place in the chloroplasts, which contain chlorophyll and other pigments necessary for capturing light energy."
  }
]
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| question | string | Question text |
| optionA | string | First answer option |
| optionB | string | Second answer option |
| optionC | string | Third answer option |
| optionD | string | Fourth answer option |
| correctAnswer | string | Correct option letter ("A", "B", "C", or "D") |
| explanation | string | Detailed explanation of the correct answer |

**Error Responses:**
- `400 Bad Request`: Invalid difficulty or empty study material
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid difficulty. Must be 'easy', 'medium', or 'hard'",
    "path": "/api/quiz/generate"
  }
  ```
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Study material is required and cannot be empty",
    "path": "/api/quiz/generate"
  }
  ```
- `401 Unauthorized`: Missing or invalid JWT token
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "path": "/api/quiz/generate"
  }
  ```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/quiz/generate \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "studyMaterial": "Photosynthesis is the process by which plants convert light energy...",
    "count": 5,
    "difficulty": "medium"
  }'
```

---

### 8. Get Quiz History
**GET** `/quiz/history`

Retrieve all quiz sets created by the authenticated user, ordered by creation date (newest first).

**Required Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "userId": 1,
    "username": "testuser",
    "title": "AI Generated Quiz",
    "studyMaterial": "Photosynthesis is the process by which plants convert light energy...",
    "difficulty": "medium",
    "createdAt": "2025-11-30T10:30:00",
    "updatedAt": "2025-11-30T10:30:00",
    "questions": [
      {
        "id": 1,
        "question": "What is the primary purpose of photosynthesis?",
        "optionA": "To produce oxygen",
        "optionB": "To convert light energy into chemical energy",
        "optionC": "To absorb carbon dioxide",
        "optionD": "To create water",
        "correctAnswer": "B",
        "explanation": "Photosynthesis primarily converts light energy from the sun...",
        "position": 0
      },
      {
        "id": 2,
        "question": "Where does photosynthesis occur in plant cells?",
        "optionA": "Mitochondria",
        "optionB": "Nucleus",
        "optionC": "Chloroplasts",
        "optionD": "Cell membrane",
        "correctAnswer": "C",
        "explanation": "Photosynthesis takes place in the chloroplasts...",
        "position": 1
      }
    ]
  }
]
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| id | long | Quiz set ID |
| userId | long | Owner's user ID |
| username | string | Owner's username |
| title | string | Set title |
| studyMaterial | string | Original study material used for generation |
| difficulty | string | Quiz difficulty level ("easy", "medium", or "hard") |
| createdAt | datetime | Creation timestamp (ISO-8601) |
| updatedAt | datetime | Last update timestamp (ISO-8601) |
| questions | array | List of question objects in this set |
| questions[].id | long | Question ID |
| questions[].question | string | Question text |
| questions[].optionA | string | First answer option |
| questions[].optionB | string | Second answer option |
| questions[].optionC | string | Third answer option |
| questions[].optionD | string | Fourth answer option |
| questions[].correctAnswer | string | Correct option letter ("A", "B", "C", or "D") |
| questions[].explanation | string | Explanation for the correct answer |
| questions[].position | integer | Order position in set (0-based) |

**Error Responses:**
- `401 Unauthorized`: Missing or invalid JWT token
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "path": "/api/quiz/history"
  }
  ```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/quiz/history \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 9. Get Quiz Set by ID
**GET** `/quiz/{id}`

Retrieve a specific quiz set by its ID. User must be the owner.

**Required Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**URL Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | long | Yes | Quiz set ID |

**Success Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "username": "testuser",
  "title": "AI Generated Quiz",
  "studyMaterial": "Photosynthesis is the process by which plants convert light energy...",
  "difficulty": "medium",
  "createdAt": "2025-11-30T10:30:00",
  "updatedAt": "2025-11-30T10:30:00",
  "questions": [
    {
      "id": 1,
      "question": "What is the primary purpose of photosynthesis?",
      "optionA": "To produce oxygen",
      "optionB": "To convert light energy into chemical energy",
      "optionC": "To absorb carbon dioxide",
      "optionD": "To create water",
      "correctAnswer": "B",
      "explanation": "Photosynthesis primarily converts light energy from the sun into chemical energy stored in glucose molecules.",
      "position": 0
    }
  ]
}
```

**Response Schema:**
Same as quiz history item (see endpoint #8)

**Error Responses:**
- `401 Unauthorized`: Missing or invalid JWT token
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "path": "/api/quiz/1"
  }
  ```
- `403 Forbidden`: User does not own this quiz set
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 403,
    "error": "Forbidden",
    "message": "You do not have permission to access this quiz set",
    "path": "/api/quiz/1"
  }
  ```
- `404 Not Found`: Quiz set not found
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "QuizSet not found with id: 1",
    "path": "/api/quiz/1"
  }
  ```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/quiz/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 10. Delete Quiz Set
**DELETE** `/quiz/{id}`

Delete a quiz set by its ID. User must be the owner. This also deletes all questions in the set (CASCADE).

**Required Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**URL Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | long | Yes | Quiz set ID to delete |

**Success Response (204 No Content):**
```
(Empty body)
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid JWT token
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "path": "/api/quiz/1"
  }
  ```
- `403 Forbidden`: User does not own this quiz set
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 403,
    "error": "Forbidden",
    "message": "You do not have permission to delete this quiz set",
    "path": "/api/quiz/1"
  }
  ```
- `404 Not Found`: Quiz set not found
  ```json
  {
    "timestamp": "2025-11-30T10:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "QuizSet not found with id: 1",
    "path": "/api/quiz/1"
  }
  ```

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/quiz/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## Common Error Response Format

All error responses follow this standardized format (defined in `ErrorResponse.java`):

```json
{
  "timestamp": "2025-11-30T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message explaining what went wrong",
  "path": "/api/endpoint/path"
}
```

**Error Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| timestamp | datetime | When the error occurred (ISO-8601) |
| status | integer | HTTP status code |
| error | string | HTTP status text |
| message | string | Detailed error description |
| path | string | API endpoint where error occurred |

### HTTP Status Codes

| Code | Meaning | When It Occurs |
|------|---------|----------------|
| 200 | OK | Request successful, data returned |
| 204 | No Content | Delete successful, no content to return |
| 400 | Bad Request | Invalid input, validation failed, missing required fields |
| 401 | Unauthorized | Missing JWT token or token is invalid/expired |
| 403 | Forbidden | Valid authentication but user lacks permission for resource |
| 404 | Not Found | Requested resource (flashcard set, quiz set) doesn't exist |
| 500 | Internal Server Error | Unexpected server-side error |

---

## Complete Workflow Example

### Step 1: Register a New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "secure123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwMTM2MDAwMCwiZXhwIjoxNzAxNDQ2NDAwfQ.abc123...",
  "username": "john_doe"
}
```

**Save the token:**
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwMTM2MDAwMCwiZXhwIjoxNzAxNDQ2NDAwfQ.abc123..."
```

---

### Step 2: Generate Flashcards
```bash
curl -X POST http://localhost:8080/api/flashcards/generate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "studyMaterial": "The water cycle includes evaporation, condensation, precipitation, and collection. Water evaporates from oceans and lakes, forms clouds through condensation, falls as rain or snow (precipitation), and returns to water bodies (collection).",
    "count": 4
  }'
```

**Response:**
```json
[
  {
    "question": "What are the four stages of the water cycle?",
    "answer": "Evaporation, condensation, precipitation, and collection"
  },
  {
    "question": "What is evaporation in the water cycle?",
    "answer": "Water turning into vapor from oceans and lakes"
  },
  {
    "question": "What happens during condensation?",
    "answer": "Water vapor forms clouds"
  },
  {
    "question": "What is precipitation?",
    "answer": "Water falling as rain or snow"
  }
]
```

---

### Step 3: View Flashcard History
```bash
curl -X GET http://localhost:8080/api/flashcards/history \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "username": "john_doe",
    "title": "AI Generated Flashcards",
    "studyMaterial": "The water cycle includes evaporation...",
    "createdAt": "2025-11-30T10:30:00",
    "updatedAt": "2025-11-30T10:30:00",
    "flashcards": [
      {
        "id": 1,
        "question": "What are the four stages of the water cycle?",
        "answer": "Evaporation, condensation, precipitation, and collection",
        "position": 0
      },
      {
        "id": 2,
        "question": "What is evaporation in the water cycle?",
        "answer": "Water turning into vapor from oceans and lakes",
        "position": 1
      }
    ]
  }
]
```

---

### Step 4: Generate Quiz
```bash
curl -X POST http://localhost:8080/api/quiz/generate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "studyMaterial": "The water cycle includes evaporation, condensation, precipitation, and collection. Water evaporates from oceans and lakes, forms clouds through condensation, falls as rain or snow (precipitation), and returns to water bodies (collection).",
    "count": 3,
    "difficulty": "easy"
  }'
```

**Response:**
```json
[
  {
    "question": "What is the first stage of the water cycle?",
    "optionA": "Evaporation",
    "optionB": "Condensation",
    "optionC": "Precipitation",
    "optionD": "Collection",
    "correctAnswer": "A",
    "explanation": "Evaporation is when water turns into vapor from bodies of water like oceans and lakes."
  },
  {
    "question": "What forms during condensation?",
    "optionA": "Rain",
    "optionB": "Clouds",
    "optionC": "Snow",
    "optionD": "Ice",
    "correctAnswer": "B",
    "explanation": "During condensation, water vapor cools and forms clouds in the atmosphere."
  }
]
```

---

### Step 5: Get Specific Flashcard Set
```bash
curl -X GET http://localhost:8080/api/flashcards/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

### Step 6: Delete a Quiz Set
```bash
curl -X DELETE http://localhost:8080/api/quiz/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Response:** `204 No Content` (empty body)

---

## Testing with Swagger UI

Interactive API testing is available through Swagger UI:

### Access Swagger UI
Navigate to: **http://localhost:8080/swagger-ui.html**

### Using Swagger UI

1. **Authorize:**
   - Click the **"Authorize"** button (🔓 icon) at the top right
   - Enter: `Bearer <your_token>` (include the word "Bearer" and a space)
   - Click **"Authorize"** then **"Close"**

2. **Test Endpoints:**
   - Expand any endpoint (e.g., `POST /api/flashcards/generate`)
   - Click **"Try it out"**
   - Fill in the request body with example data
   - Click **"Execute"**
   - View the response below

3. **View Schemas:**
   - Scroll to the bottom to see all request/response schemas
   - Click on model names to expand detailed field descriptions

### Swagger Features
- ✅ All 10 endpoints organized by tags (Authentication, Flashcards, Quizzes)
- ✅ Request/response examples
- ✅ Field descriptions and constraints
- ✅ "Try it out" functionality for immediate testing
- ✅ JWT authorization built-in

---

## Important Notes

### Authentication
- JWT tokens expire after **24 hours**
- Token format: `Bearer <token>` (must include "Bearer " prefix)
- Tokens are returned from `/auth/register` and `/auth/login`
- Store tokens securely (e.g., localStorage in frontend)

### Data Constraints
- `studyMaterial`: Plain text, max recommended **5000 characters**
- `username`: Min 3 characters, must be unique
- `password`: Min 6 characters, will be encrypted with BCrypt
- `count`: Must be positive integer (recommended: 3-10 for flashcards, 5-15 for quizzes)
- `difficulty`: Must be exactly "easy", "medium", or "hard" (case-sensitive)

### Cascade Deletion
- Deleting a **FlashcardSet** automatically deletes all its **Flashcard** items
- Deleting a **QuizSet** automatically deletes all its **QuizQuestion** items
- Deleting a **User** deletes all their flashcard sets and quiz sets

### Ownership
- Users can only view/modify/delete **their own** resources
- Attempting to access another user's resources returns **403 Forbidden**
- User ID is extracted from JWT token automatically

### Timestamps
- All timestamps use **ISO-8601 format**: `2025-11-30T10:30:00`
- Timezone: **UTC**
- `createdAt`: Set automatically on creation
- `updatedAt`: Updated automatically on modification

### Database
- Database: **MySQL 8.0**
- Connection: `localhost:3306/aichat_db`
- Schema auto-updated via Hibernate (`spring.jpa.hibernate.ddl-auto=update`)
- All tables use **InnoDB** engine with foreign key constraints

---

## Troubleshooting

### 401 Unauthorized
**Problem:** "Full authentication is required to access this resource"

**Solutions:**
- Ensure you've included the `Authorization` header
- Check token format: `Bearer <token>` (space after "Bearer")
- Token might be expired (24h limit) - login again
- Verify token wasn't truncated when copying

### 403 Forbidden
**Problem:** "You do not have permission to access/delete this resource"

**Cause:** You're trying to access another user's flashcard/quiz set

**Solution:** Only access resources you created with your account

### 404 Not Found
**Problem:** "FlashcardSet/QuizSet not found with id: X"

**Solutions:**
- Verify the ID exists by checking `/history` first
- Resource might have been deleted
- Ensure you're using the correct endpoint (flashcards vs quiz)

### 400 Bad Request
**Problem:** Validation errors

**Common causes:**
- Empty `studyMaterial`
- Invalid `difficulty` value (must be "easy", "medium", or "hard")
- Missing required fields in request body
- Username already exists (registration)

---

## API Version
- **Version:** 1.0.0
- **Last Updated:** November 30, 2025
- **Spring Boot Version:** 3.5.7
- **Java Version:** 21
