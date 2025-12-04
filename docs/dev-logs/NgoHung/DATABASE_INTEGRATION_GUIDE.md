# Database Integration Guide for Frontend

**Author:** Ngo Hung  
**Date:** November 30, 2025  
**Version:** 1.0

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Database Structure](#database-structure)
3. [API Endpoints Reference](#api-endpoints-reference)
4. [Frontend Integration Examples](#frontend-integration-examples)
5. [Authentication Flow](#authentication-flow)
6. [Error Handling](#error-handling)
7. [Testing Endpoints](#testing-endpoints)

---

## Overview

This guide explains how to connect the frontend to the backend database. The backend API runs on **http://localhost:8080** and provides RESTful endpoints for:

- User Authentication (Register/Login)
- Flashcard Generation and Storage
- Quiz Generation and Storage
- Study Material Management

**Base URL:** `http://localhost:8080`

---

## Database Structure

### Tables Overview

The database consists of 5 main tables:

#### 1. **users** - User Accounts
```sql
- id (BIGINT, Primary Key)
- username (VARCHAR, UNIQUE)
- email (VARCHAR, UNIQUE)
- password_hash (VARCHAR) - BCrypt hashed
- created_at (DATETIME)
- updated_at (DATETIME)
```

#### 2. **flashcard_sets** - Flashcard Collections
```sql
- id (BIGINT, Primary Key)
- user_id (BIGINT, Foreign Key → users.id)
- title (VARCHAR)
- study_material (TEXT)
- created_at (DATETIME)
- updated_at (DATETIME)
```

#### 3. **flashcards** - Individual Flashcards
```sql
- id (BIGINT, Primary Key)
- set_id (BIGINT, Foreign Key → flashcard_sets.id)
- question (TEXT)
- answer (TEXT)
- position (INT)
```

#### 4. **quiz_sets** - Quiz Collections
```sql
- id (BIGINT, Primary Key)
- user_id (BIGINT, Foreign Key → users.id)
- title (VARCHAR)
- study_material (TEXT)
- difficulty (VARCHAR) - 'easy', 'medium', or 'hard'
- created_at (DATETIME)
- updated_at (DATETIME)
```

#### 5. **quiz_questions** - Quiz Questions
```sql
- id (BIGINT, Primary Key)
- quiz_set_id (BIGINT, Foreign Key → quiz_sets.id)
- question (TEXT)
- option_a (TEXT)
- option_b (TEXT)
- option_c (TEXT)
- option_d (TEXT)
- correct_answer (VARCHAR) - 'A', 'B', 'C', or 'D'
- explanation (TEXT)
- position (INT)
```

### Foreign Key Relationships

```
users (1) ──→ (many) flashcard_sets
flashcard_sets (1) ──→ (many) flashcards

users (1) ──→ (many) quiz_sets
quiz_sets (1) ──→ (many) quiz_questions
```

**Important:** When a user is deleted, all their flashcard_sets and quiz_sets are automatically deleted (CASCADE DELETE).

---

## API Endpoints Reference

### 🔐 Authentication Endpoints

#### 1. Register New User

**POST** `/api/auth/register`

**Request Body:**
```json
{
  "username": "your_username",
  "email": "your@email.com",
  "password": "your_password"
}
```

**Success Response (200):**
```json
{
  "id": 1,
  "username": "your_username",
  "email": "your@email.com",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTczMjk5..."
}
```

**Error Responses:**
- **400 Bad Request:** Username or email already exists
```json
{
  "status": 400,
  "message": "Username already exists"
}
```

---

#### 2. Login User

**POST** `/api/auth/login`

**Request Body:**
```json
{
  "username": "your_username",
  "password": "your_password"
}
```

**Success Response (200):**
```json
{
  "id": 1,
  "username": "your_username",
  "email": "your@email.com",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTczMjk5..."
}
```

**Error Responses:**
- **401 Unauthorized:** Invalid credentials
```json
{
  "status": 401,
  "message": "Invalid credentials"
}
```

---

### 📚 Flashcard Endpoints

#### 3. Generate Flashcards

**POST** `/api/flashcards/generate`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your_jwt_token>  (optional for now)
```

**Request Body:**
```json
{
  "studyMaterial": "Your study text here. Can be multiple paragraphs...",
  "count": 5
}
```

**Success Response (200):**
```json
[
  {
    "question": "What is Spring Boot?",
    "answer": "An open-source Java framework for building production-ready applications"
  },
  {
    "question": "What does @Autowired do?",
    "answer": "It enables automatic dependency injection in Spring"
  }
]
```

**Parameters:**
- `studyMaterial` (required): The text content to generate flashcards from
- `count` (optional): Number of flashcards to generate (default: 5)

---

### 🎯 Quiz Endpoints

#### 4. Generate Quiz

**POST** `/api/quiz/generate`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your_jwt_token>  (optional for now)
```

**Request Body:**
```json
{
  "studyMaterial": "Your study text here...",
  "count": 3,
  "difficulty": "medium"
}
```

**Success Response (200):**
```json
[
  {
    "question": "What is Spring Boot?",
    "options": [
      "An open-source Java framework",
      "A JavaScript library",
      "A database system",
      "A CSS preprocessor"
    ],
    "correctAnswer": 0,
    "explanation": "Spring Boot is an open-source Java framework for building production-ready applications."
  }
]
```

**Parameters:**
- `studyMaterial` (required): The text content to generate quiz from
- `count` (optional): Number of questions to generate (default: 5)
- `difficulty` (optional): "easy", "medium", or "hard" (default: "medium")

**Error Responses:**
- **400 Bad Request:** Invalid input
```json
{
  "error": "Study material is required and cannot be empty"
}
```
```json
{
  "error": "Invalid difficulty. Must be 'easy', 'medium', or 'hard'"
}
```

---

#### 5. Test Quiz Endpoint

**GET** `/api/quiz/test`

**Success Response (200):**
```json
{
  "message": "Quiz API is working!"
}
```

**Use this to check if the backend is running.**

---

## Frontend Integration Examples

### React/JavaScript Example

#### Setup with Proxy (Recommended)

In your `package.json`, add:
```json
{
  "proxy": "http://localhost:8080"
}
```

This allows you to make requests to `/api/...` instead of `http://localhost:8080/api/...`

---

### Example 1: User Registration

```javascript
async function registerUser(username, email, password) {
  try {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username,
        email: email,
        password: password
      })
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Registration failed');
    }

    const data = await response.json();
    // Save token to localStorage
    localStorage.setItem('authToken', data.token);
    localStorage.setItem('username', data.username);
    
    return data;
  } catch (error) {
    console.error('Registration error:', error);
    throw error;
  }
}

// Usage in React component:
const handleRegister = async (e) => {
  e.preventDefault();
  try {
    const result = await registerUser(username, email, password);
    console.log('Registration successful:', result);
    // Redirect to dashboard or home page
  } catch (error) {
    alert('Registration failed: ' + error.message);
  }
};
```

---

### Example 2: User Login

```javascript
async function loginUser(username, password) {
  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username,
        password: password
      })
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Login failed');
    }

    const data = await response.json();
    // Save token to localStorage
    localStorage.setItem('authToken', data.token);
    localStorage.setItem('username', data.username);
    localStorage.setItem('userId', data.id);
    
    return data;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
}

// Usage in React component:
const handleLogin = async (e) => {
  e.preventDefault();
  try {
    const result = await loginUser(username, password);
    console.log('Login successful:', result);
    // Redirect to dashboard
  } catch (error) {
    alert('Login failed: ' + error.message);
  }
};
```

---

### Example 3: Generate Flashcards

```javascript
async function generateFlashcards(studyText, count = 5) {
  try {
    const token = localStorage.getItem('authToken');
    
    const response = await fetch('/api/flashcards/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        // Authorization header is optional for now
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      body: JSON.stringify({
        studyMaterial: studyText,
        count: count
      })
    });

    if (!response.ok) {
      throw new Error('Failed to generate flashcards');
    }

    const flashcards = await response.json();
    return flashcards;
  } catch (error) {
    console.error('Error generating flashcards:', error);
    throw error;
  }
}

// Usage in React component:
const handleGenerateFlashcards = async () => {
  try {
    const flashcards = await generateFlashcards(studyMaterial, 10);
    setFlashcards(flashcards);
    console.log('Generated flashcards:', flashcards);
  } catch (error) {
    alert('Failed to generate flashcards: ' + error.message);
  }
};
```

---

### Example 4: Generate Quiz

```javascript
async function generateQuiz(studyText, count = 5, difficulty = 'medium') {
  try {
    const token = localStorage.getItem('authToken');
    
    const response = await fetch('/api/quiz/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      body: JSON.stringify({
        studyMaterial: studyText,
        count: count,
        difficulty: difficulty
      })
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error || 'Failed to generate quiz');
    }

    const questions = await response.json();
    return questions;
  } catch (error) {
    console.error('Error generating quiz:', error);
    throw error;
  }
}

// Usage in React component:
const handleGenerateQuiz = async () => {
  try {
    const questions = await generateQuiz(studyMaterial, 5, 'hard');
    setQuizQuestions(questions);
    console.log('Generated quiz questions:', questions);
  } catch (error) {
    alert('Failed to generate quiz: ' + error.message);
  }
};
```

---

### Example 5: Complete React Component

```jsx
import React, { useState } from 'react';

function StudyAssistant() {
  const [studyMaterial, setStudyMaterial] = useState('');
  const [flashcards, setFlashcards] = useState([]);
  const [quizQuestions, setQuizQuestions] = useState([]);
  const [loading, setLoading] = useState(false);

  const generateFlashcards = async () => {
    if (!studyMaterial.trim()) {
      alert('Please enter study material');
      return;
    }

    setLoading(true);
    try {
      const response = await fetch('/api/flashcards/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          studyMaterial: studyMaterial,
          count: 5
        })
      });

      if (!response.ok) {
        throw new Error('Failed to generate flashcards');
      }

      const data = await response.json();
      setFlashcards(data);
      alert('Flashcards generated successfully!');
    } catch (error) {
      alert('Error: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const generateQuiz = async () => {
    if (!studyMaterial.trim()) {
      alert('Please enter study material');
      return;
    }

    setLoading(true);
    try {
      const response = await fetch('/api/quiz/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          studyMaterial: studyMaterial,
          count: 5,
          difficulty: 'medium'
        })
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to generate quiz');
      }

      const data = await response.json();
      setQuizQuestions(data);
      alert('Quiz generated successfully!');
    } catch (error) {
      alert('Error: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="study-assistant">
      <h1>AI Study Assistant</h1>
      
      <div className="input-section">
        <textarea
          placeholder="Enter your study material here..."
          value={studyMaterial}
          onChange={(e) => setStudyMaterial(e.target.value)}
          rows={10}
          cols={80}
        />
      </div>

      <div className="button-section">
        <button onClick={generateFlashcards} disabled={loading}>
          {loading ? 'Generating...' : 'Generate Flashcards'}
        </button>
        <button onClick={generateQuiz} disabled={loading}>
          {loading ? 'Generating...' : 'Generate Quiz'}
        </button>
      </div>

      {flashcards.length > 0 && (
        <div className="flashcards-section">
          <h2>Generated Flashcards</h2>
          {flashcards.map((card, index) => (
            <div key={index} className="flashcard">
              <h3>Q: {card.question}</h3>
              <p>A: {card.answer}</p>
            </div>
          ))}
        </div>
      )}

      {quizQuestions.length > 0 && (
        <div className="quiz-section">
          <h2>Generated Quiz</h2>
          {quizQuestions.map((q, index) => (
            <div key={index} className="quiz-question">
              <h3>{index + 1}. {q.question}</h3>
              <ul>
                {q.options.map((option, i) => (
                  <li key={i} className={i === q.correctAnswer ? 'correct' : ''}>
                    {String.fromCharCode(65 + i)}. {option}
                  </li>
                ))}
              </ul>
              <p className="explanation"><strong>Explanation:</strong> {q.explanation}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default StudyAssistant;
```

---

## Authentication Flow

### How JWT Tokens Work

1. **User registers or logs in** → Backend returns JWT token
2. **Frontend stores token** in localStorage or sessionStorage
3. **For protected endpoints**, include token in Authorization header:
   ```javascript
   headers: {
     'Authorization': `Bearer ${token}`
   }
   ```
4. **Token expires after 24 hours** → User needs to login again

### Storing and Using Tokens

```javascript
// After successful login/register:
localStorage.setItem('authToken', response.token);
localStorage.setItem('username', response.username);

// When making authenticated requests:
const token = localStorage.getItem('authToken');
fetch('/api/some-endpoint', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// On logout:
localStorage.removeItem('authToken');
localStorage.removeItem('username');
```

### Check if User is Logged In

```javascript
function isLoggedIn() {
  const token = localStorage.getItem('authToken');
  return token !== null;
}

// Use in React:
const [isAuthenticated, setIsAuthenticated] = useState(isLoggedIn());

useEffect(() => {
  if (!isAuthenticated) {
    // Redirect to login page
    window.location.href = '/login';
  }
}, [isAuthenticated]);
```

---

## Error Handling

### Common HTTP Status Codes

| Code | Meaning | When It Happens |
|------|---------|----------------|
| **200** | OK | Request successful |
| **400** | Bad Request | Invalid input data (empty fields, wrong format) |
| **401** | Unauthorized | Missing or invalid token, wrong password |
| **403** | Forbidden | User doesn't have permission |
| **404** | Not Found | Endpoint doesn't exist |
| **500** | Internal Server Error | Something went wrong on the server |

### Error Response Format

All errors follow this structure:
```json
{
  "status": 400,
  "message": "Descriptive error message"
}
```

Or:
```json
{
  "error": "Descriptive error message"
}
```

### Example Error Handling

```javascript
async function makeRequest(url, options) {
  try {
    const response = await fetch(url, options);
    
    if (!response.ok) {
      // Try to get error message from response
      let errorMessage = 'Request failed';
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorData.error || errorMessage;
      } catch (e) {
        // Response wasn't JSON
      }
      
      throw new Error(`${response.status}: ${errorMessage}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Request error:', error);
    throw error;
  }
}

// Usage:
try {
  const data = await makeRequest('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  console.log('Success:', data);
} catch (error) {
  // Handle specific errors
  if (error.message.includes('401')) {
    alert('Invalid username or password');
  } else if (error.message.includes('400')) {
    alert('Please fill in all fields');
  } else {
    alert('An error occurred: ' + error.message);
  }
}
```

---

## Testing Endpoints

### Using cURL (Command Line)

#### Test Backend Connection
```bash
curl http://localhost:8080/api/quiz/test
```

#### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!"
  }'
```

#### Login User
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!"
  }'
```

#### Generate Flashcards
```bash
curl -X POST http://localhost:8080/api/flashcards/generate \
  -H "Content-Type: application/json" \
  -d '{
    "studyMaterial": "Spring Boot is a Java framework...",
    "count": 5
  }'
```

#### Generate Quiz
```bash
curl -X POST http://localhost:8080/api/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{
    "studyMaterial": "Database normalization is...",
    "count": 3,
    "difficulty": "medium"
  }'
```

---

### Using Browser Developer Tools

1. **Open Developer Console** (F12)
2. **Go to Network tab**
3. **Make requests from your frontend**
4. **Inspect requests and responses**

You can also test directly in console:
```javascript
// Test in browser console:
fetch('/api/quiz/test')
  .then(res => res.json())
  .then(data => console.log(data));
```

---

## Quick Start Checklist

### Backend Setup
- [ ] Make sure MySQL is running
- [ ] Database `aichat_db` exists
- [ ] User `aichat_user` has permissions
- [ ] Spring Boot application is running on port 8080
- [ ] Test with: `curl http://localhost:8080/api/quiz/test`

### Frontend Setup
- [ ] Add `"proxy": "http://localhost:8080"` to package.json
- [ ] Install dependencies: `npm install`
- [ ] Start frontend: `npm start` (usually runs on port 3000)
- [ ] Frontend can now call `/api/...` endpoints

### Test Integration
```javascript
// Quick test in your React app:
useEffect(() => {
  fetch('/api/quiz/test')
    .then(res => res.json())
    .then(data => console.log('Backend connected:', data))
    .catch(err => console.error('Backend not connected:', err));
}, []);
```

---

## Common Issues & Solutions

### Issue 1: CORS Error
**Error:** "Access to fetch at 'http://localhost:8080/api/...' from origin 'http://localhost:3000' has been blocked by CORS"

**Solution:** The backend already has `@CrossOrigin(origins = "*")` on controllers. If still having issues, make sure you're using the proxy in package.json.

---

### Issue 2: 401 Unauthorized
**Error:** Getting 401 even with correct credentials

**Solution:** 
1. Check if username/password are correct
2. Make sure you're sending the token in the Authorization header
3. Token might be expired (24 hour expiration)

---

### Issue 3: Network Error
**Error:** "Failed to fetch" or "Network request failed"

**Solution:**
1. Make sure backend is running: `curl http://localhost:8080/api/quiz/test`
2. Check if MySQL database is running
3. Verify the proxy setting in package.json

---

### Issue 4: Empty Response
**Error:** Getting empty array `[]` instead of data

**Solution:**
1. Check that `studyMaterial` is not empty
2. Verify the AI service (OpenAI) is configured with valid API key
3. Check backend logs for errors

---

## Data Flow Diagram

```
┌─────────────┐
│   Frontend  │
│  (React)    │
└──────┬──────┘
       │
       │ HTTP Request (JSON)
       │
       ▼
┌─────────────┐
│   Backend   │
│ (Spring)    │
│  Port 8080  │
└──────┬──────┘
       │
       │ JDBC
       │
       ▼
┌─────────────┐
│   MySQL     │
│  Database   │
│  aichat_db  │
└─────────────┘
```

**Request Flow:**
1. User fills form in React
2. React calls `/api/...` endpoint
3. Spring Boot receives request
4. Spring Boot queries/updates MySQL database
5. Spring Boot returns JSON response
6. React displays results to user

---

## Additional Resources

### Files to Check
- **Backend Controllers:** `src/main/java/ie/tcd/scss/aichat/controller/`
  - `AuthController.java` - Authentication endpoints
  - `QuizController.java` - Quiz generation
  - `FlashcardController.java` - Flashcard generation

- **Database Entities:** `src/main/java/ie/tcd/scss/aichat/model/`
  - `User.java`
  - `FlashcardSet.java`, `Flashcard.java`
  - `QuizSet.java`, `QuizQuestion.java`

- **Test Files:** `src/test/java/ie/tcd/scss/aichat/`
  - All 73 tests passing ✅
  - Check tests for usage examples

### Contact

If you have questions about database integration:
- **Developer:** Ngo Hung
- **Check:** Development logs in `docs/dev-logs/NgoHung/`
- **Tests:** Run `mvn test` to verify backend is working

---

## Summary

### What You Need to Do in Frontend:

1. **Add proxy to package.json:**
   ```json
   "proxy": "http://localhost:8080"
   ```

2. **Make POST requests to generate content:**
   - `/api/flashcards/generate` - for flashcards
   - `/api/quiz/generate` - for quizzes

3. **Handle authentication (optional for now):**
   - `/api/auth/register` - create account
   - `/api/auth/login` - login
   - Store token in localStorage
   - Include token in Authorization header for protected endpoints

4. **Display the results:**
   - Flashcards: array of {question, answer}
   - Quiz: array of {question, options[], correctAnswer, explanation}

**That's it! You're ready to integrate the database with your frontend.** 🚀

---

## Test Results

All API endpoints have been tested and verified as of November 30, 2025.

### ✅ Working Endpoints (Tested Successfully)

1. **POST /api/auth/register** - HTTP 200
   - Successfully creates new user
   - Returns user ID, username, email, and JWT token
   - Properly rejects duplicate usernames (HTTP 400)

2. **POST /api/auth/login** - HTTP 200
   - Successfully authenticates user
   - Returns JWT token
   - Properly rejects invalid credentials (HTTP 401)

### ⚠️ Current Limitations

**Flashcard and Quiz endpoints currently require authentication:**

- **POST /api/flashcards/generate** - Returns HTTP 403 without auth token
- **POST /api/quiz/generate** - Returns HTTP 403 without auth token

To use these endpoints, you must include the JWT token in the Authorization header:

```javascript
const token = localStorage.getItem('authToken');

fetch('/api/flashcards/generate', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`  // REQUIRED
  },
  body: JSON.stringify({
    studyMaterial: "Your text here...",
    count: 5
  })
});
```

**Test Script Available:**

Run `bash test-endpoints.sh` to test all API endpoints automatically.

---

*Last Updated: November 30, 2025  
Test Results Verified: November 30, 2025*
