Frontend Development - Authentication System
Date: November 23, 2025
Developer: Fiachra
Branch: feature/login

Objective
Implement a complete JWT-based authentication system for the React frontend that integrates with the existing Spring Boot backend authentication (Jobs 1 & 2 from database implementation). Enable user registration, login, session persistence, and secure API access to the AI Study Assistant.

Development Process
Initial Request
Prompt: "I want to implement a login page for our application. If it's possible I want in a separate file from StudyAssistant.jsx and StudyAssistant.css as I think they are long and complicated enough already. Can you guide me on how to structure this and what components I need?"
Context: Had recently completed database integration with JWT authentication on backend. Backend supports /api/auth/register and /api/auth/login endpoints with BCrypt password hashing. Frontend needed to be updated to work with this authentication system.

Step 1: Branch Merging and Conflict Resolution
Before implementing authentication, needed to merge capabilities from two feature branches:

feature/addPDFfield - Added PDF upload functionality
database - Added database integration with user authentication

Merge Process
bash# Checked out main development branch
git checkout develop

# Merged PDF functionality
git merge feature/addPDFfield
# No conflicts - clean merge

# Merged database authentication
git merge database
# CONFLICT: application.properties had different database configs
Conflict Resolution
File: src/main/resources/application.properties
Conflict:
properties<<<<<<< HEAD
# PDF configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
=======
# Database configuration  
spring.datasource.url=jdbc:mysql://localhost:3306/aichat_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
>>>>>>> database
Resolution: Kept both configurations
properties# PDF configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/aichat_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
Verified backend still compiled and ran after merge:
bash./mvnw clean compile
./mvnw spring-boot:run
# ✅ No errors

Step 2: Authentication Component Structure
Prompt: "How should I structure the Auth component? What state do I need and how should it communicate with the backend?"
Guidance Received: Create a separate Auth.js component with:

Dual-mode form (login/registration toggle)
State management for form data and errors
Validation before API calls
localStorage for token storage
Callback pattern to notify parent on success

Created src/components/Auth.js with:

Form state using useState:

javascript  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    username: '', email: '', password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

Validation logic:

Required: username, password
Registration also requires: email
Password minimum: 6 characters


API calls to backend endpoints
Token storage pattern

Key Decision: Used callback pattern (onLoginSuccess) to update parent component state rather than using Context API - simpler for this use case.

Step 3: Styling the Authentication UI
Prompt: "What's a good way to style the login page? I want it to match our existing design but stand out as a landing page."
Guidance Received: Use gradient background with card layout, similar to modern auth pages. Purple gradient would match the study assistant theme.
Created src/components/Auth.css with:

Purple gradient background (#667eea to #764ba2)
Centered card with shadow
Form styling with focus states
Button animations (hover, active states)
Error display with shake animation
Responsive breakpoints

Issue Encountered: Error messages appeared too suddenly
Solution: Added CSS shake animation:
css@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10px); }
  75% { transform: translateX(10px); }
}

Step 4: App-Level State Management
Prompt: "How do I manage authentication state at the app level? Should I use Context or just lift state up?"
Guidance Received: For this application size, lifting state to App.js is sufficient. Use localStorage to persist sessions across refreshes.
Modified src/App.js:

Added authentication state
Implemented useEffect for session restoration
Created handler functions for login/logout
Conditional rendering based on auth state

Key implementation:
javascriptuseEffect(() => {
  const token = localStorage.getItem('token');
  const storedUser = localStorage.getItem('user');
  
  if (token && storedUser) {
    try {
      const userData = JSON.parse(storedUser);
      setUser(userData);
    } catch (error) {
      // Clear corrupted data
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  }
  setLoading(false);
}, []);
Key Learning: Always parse JSON in try-catch when reading from localStorage - prevents crashes from corrupted data.

Step 5: Header and Layout Design
Prompt: "I need a header that shows when users are logged in. How should I structure this?"
Created src/App.css for app-wide styles:

Sticky header that stays at top
User greeting with username
Logout button with hover effects
Responsive layout for mobile

Issue Encountered: Header wasn't sticky on scroll
Solution: Added proper CSS:
css.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
}

Step 6: Integrating Auth with StudyAssistant
Prompt: "How do I pass authentication info to StudyAssistant? And how should API calls include the token?"
Guidance Received: Pass userId as prop, create helper function for auth headers, update both generate functions.
Modified src/components/StudyAssistant.jsx:

Added userId prop
Created getAuthHeaders() helper:

javascript  const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    };
  };

Updated API calls in generateFlashcards() and generateQuiz()
Added 401 handling for token expiration

Challenge: Deciding where to handle token expiration
Solution: Check response status and redirect to login:
javascriptif (response.status === 401) {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  window.location.reload();
  return;
}

Step 7: Folder Structure Fixes
Issue Discovered: My project structure was different from initial guidance:
src/
├── App.js          (here, not in components)
├── App.css         (here, not in components)
└── components/
    ├── Auth.js
    ├── Auth.css
    ├── StudyAssistant.jsx
    └── StudyAssistant.css
Prompt: "My files are organized differently - how do I fix the imports?"
Solution: Updated import paths in src/App.js:
javascript// Fixed imports
import Auth from './components/Auth';
import StudyAssistant from './components/StudyAssistant';
No other files needed changes since CSS files were already in the same folder as their components.

Step 8: Database Documentation and Team Communication
Prompt: "What's the database name and how can I view/manage users? Also need to explain this to my team."
Database Details:

Name: aichat_db
Users table stores: username, email, password_hash
Passwords stored as BCrypt hashes ($2a$10$...)

Viewing Users:
bashmysql -u root -p
USE aichat_db;
SELECT * FROM users;
Deleting Users:
sqlDELETE FROM users WHERE username = 'testuser';
# ⚠️ CASCADE DELETE removes all user's flashcards/quizzes
Created documentation including:

MySQL connection instructions
View/delete SQL commands
Setup guide for Windows/Mac/Linux
WhatsApp message templates for team
Database relationship explanation


Step 9: Security Analysis
Prompt: "Does this implementation actually provide proper authentication, authorization, and security?"
Performed comprehensive security analysis:
✅ Implemented (Frontend Ready)

Authentication system with JWT tokens
Password hashing (BCrypt, backend)
Session persistence
CSRF protection (disabled for REST - correct)
SQL injection protection (JPA)
XSS protection (React escaping)

⚠️ Backend Gaps Identified
From dev log analysis, discovered backend has:

No JWT validation filter
userId extracted from request body (should be from token)
No ownership checks on data
Hardcoded JWT secret

Security Score: Frontend 9/10, Backend 5/10
Documented fixes needed:

Implement JwtAuthenticationFilter.java
Extract userId from Authentication object
Add ownership checks on endpoints
Move JWT secret to environment variable

Key Finding: Frontend is production-ready, but backend needs critical security fixes before deployment.

Step 10: Git Commit Documentation
Prompt: "Need a detailed commit message covering all changes for GitLab"
Created three commit message formats:

Detailed documentation-style
Conventional commits format
GitLab MR description

Documented:

3 files created (Auth.js, Auth.css, App.css)
2 files modified (App.js, StudyAssistant.jsx)
~450 lines added
Complete feature list
API integration details
Security analysis
Testing notes


Technical Stack

Frontend Framework: React 18.2.0
State Management: React Hooks (useState, useEffect)
API Communication: Fetch API
Storage: localStorage
Styling: Plain CSS
Authentication: JWT Bearer tokens
Backend: Spring Boot with BCrypt (10 rounds)


Project Structure
src/
├── App.js                          [MODIFIED]
├── App.css                         [CREATED]
└── components/
    ├── Auth.js                     [CREATED]
    ├── Auth.css                    [CREATED]
    ├── StudyAssistant.jsx          [MODIFIED]
    └── StudyAssistant.css          [UNCHANGED]

Key Features

User Registration - Username, email, password with validation
User Login - Credential verification with error handling
Session Persistence - Auto-login via localStorage
JWT Management - Secure token storage and transmission
Auto Logout - On token expiration (401)
Responsive UI - Mobile-friendly design
Loading States - Visual feedback during API calls
Error Handling - Clear validation messages
Modern Design - Purple gradient with animations
Security - Input validation, XSS protection


API Integration
javascript// Registration
POST /api/auth/register
Body: { username, email, password }
Response: { id, username, email, token }

// Login
POST /api/auth/login
Body: { username, password }
Response: { id, username, email, token }

// Generate Flashcards (authenticated)
POST /api/flashcards/generate
Headers: { Authorization: "Bearer <token>" }
Body: { studyMaterial, count, userId, title }

// Generate Quiz (authenticated)
POST /api/quiz/generate
Headers: { Authorization: "Bearer <token>" }
Body: { studyMaterial, count, difficulty, userId, title }

Authentication Flow
1. User enters credentials → Auth component
2. Frontend validates input
3. POST to /api/auth/login or /api/auth/register
4. Backend returns { id, username, email, token }
5. Store token + user in localStorage
6. App.js updates state → render StudyAssistant
7. All API calls include Authorization header
8. On 401 → clear storage → reload (show login)
9. On logout → clear storage → show Auth
10. On page load → check localStorage → auto-login

Challenges and Solutions
1. Branch Merge Conflicts
Problem: application.properties had conflicting configurations from PDF and database branches
Solution: Manually merged both configurations, keeping PDF upload settings and database connection settings together
2. Component Communication
Problem: Needed to pass auth state from Auth component to App to StudyAssistant
Solution: Used callback pattern for Auth→App and props for App→StudyAssistant. Simpler than Context for this scale.
3. Session Persistence
Problem: Users logged out on page refresh
Solution: Check localStorage on mount in App.js useEffect, parse stored user data
4. Token Expiration
Problem: Expired tokens cause API failures without user feedback
Solution: Check for 401 status, clear storage, reload page to show login
5. Import Path Confusion
Problem: Guide assumed different folder structure
Solution: Updated imports to match actual structure (./components/Auth)
6. Error State Management
Problem: Errors persisted between login/signup mode switches
Solution: Clear error state in toggleMode() function
7. Backend Security Gaps
Problem: Frontend ready but backend has validation issues
Solution: Documented critical fixes needed, frontend works with current backend but noted production blockers

Testing Performed
Manual Testing

✅ Registration with valid data
✅ Registration validation (short password, missing fields)
✅ Login with correct credentials
✅ Login with wrong password
✅ Session persistence after refresh
✅ Logout clears session
✅ Generate flashcards while logged in
✅ Generate quiz while logged in
✅ Authorization header in requests (DevTools verified)
✅ 401 redirects to login
✅ Toggle between login/signup
✅ Enter key submits form

Security Checks

✅ Passwords not in plain text
✅ JWT in Authorization header
✅ No sensitive data in errors
✅ React XSS protection working
✅ No console errors


Issues Encountered & Solutions
Issue 1: Merge conflict in application.properties
Error: Both branches modified database configuration section
Resolution: Kept both PDF upload config and database config, tested backend still runs
Issue 2: Header not staying sticky
Cause: Missing position: sticky and z-index
Fix: Added proper CSS positioning to .app-header
Issue 3: Error messages too jarring
Improvement: Added shake animation and softer colors
Issue 4: localStorage data corruption crashes app
Fix: Wrapped JSON.parse() in try-catch, clear on error

Security Features
Frontend Security ✅

JWT tokens via Authorization header
Input validation (required fields, length, format)
XSS protection (React escaping)
Session management (auto-logout on expiration)
Error messages don't leak info
No sensitive data in localStorage (only token + basic user info)

Backend Security 

BCrypt password hashing (10 rounds)
JWT token generation (24-hour expiry)
CSRF disabled for REST (correct)
SQL injection protection (JPA)

Known Gaps ⚠️
Backend needs:

JWT validation filter
User data ownership checks
Environment variable for JWT secret
Extract userId from token, not request


Documentation Created

Auth Component Guide - Structure and implementation
Security Analysis - Feature-by-feature assessment
Database Guide - MySQL commands and team communication
Folder Structure Fix - Import path corrections
Commit Messages - Three formats for GitLab


Running the System
Backend
bash# Start MySQL
sudo service mysql start

# Run Spring Boot
cd backend
./mvnw spring-boot:run
Frontend
bashcd frontend
npm install
npm start
First Use

Navigate to http://localhost:3000
Click "Sign Up"
Enter username, email, password (≥6 chars)
Auto-logged in → see StudyAssistant
Username shown in header
Generate flashcards/quiz with authentication


Next Steps (Future Work)
Priority 1: Backend Security 

 Implement JwtAuthenticationFilter
 Extract userId from Authentication token
 Add ownership checks on endpoints
 Move JWT secret to env variable
 Implement history endpoints (Jobs 3 & 4)

Priority 2: Enhanced Auth

 Password strength meter
 Rate limiting on login


Lessons Learned

Merge Early, Merge Often - Resolving conflicts sooner prevents larger issues later
localStorage Gotchas - Always use try-catch with JSON.parse, data can be corrupted
Component Organization - Keeping Auth separate from StudyAssistant made code cleaner and more maintainable
Security Analysis - Frontend being ready doesn't mean the system is ready - backend validation is critical
Documentation Value - Writing guides for database management helped team understand the system better
Import Paths - Different folder structures require careful attention to relative paths
Error States - Clear error state when switching modes prevents confusing UX bugs


Conclusion
Successfully implemented JWT-based authentication for the AI Study Assistant frontend. Merged capabilities from feature/addPDFfield and database branches, resolving merge conflicts in application.properties. The authentication system includes user registration, login, session persistence, and secure API communication.
Frontend Status: ✅ Production-ready
Backend Status: ⚠️ Needs JWT validation filter and ownership checks
Integration: ✅ Working end-to-end with current backend
The system provides a solid foundation for user-centric features like saved study materials, quiz history, and collaborative learning tools. Backend security fixes are documented and should be prioritized before production deployment.

Time Spent: ~5 hours
Files Created: 3
Files Modified: 2
Lines Added: ~450
Merge Conflicts Resolved: 1