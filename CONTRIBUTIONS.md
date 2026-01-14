# My Contributions to AI Study Tutor Project

**Developer:** Hao She
**Role:** AI Integration & Frontend Developer
**Project Duration:** November - December 2025
**Team Size:** 5 developers

---

## Overview

I was responsible for integrating OpenAI's GPT into the study application and building the initial frontend interface. My work focused on solving AI reliability issues through prompt engineering and creating a functional user interface for flashcard and quiz generation.

---

## Key Contributions

### 1. AI-Powered Content Generation (Backend)

**What I Built:**
- Flashcard generation service using OpenAI API
- Quiz generation service with three difficulty levels (Easy, Medium, Hard)
- REST API controllers for both services
- Comprehensive test suites (35+ tests, 100% pass rate)

**Key Files:**
- `FlashcardService.java` - Core AI integration for flashcard generation
- `QuizService.java` - Quiz generation with difficulty levels
- `FlashcardController.java` & `QuizController.java` - REST endpoints
- Complete test coverage for all services and controllers

---

### 2. Solving AI Quality Issues (Prompt Engineering)

**Problem 1: Quiz Answer Bias**
- **Issue:** AI generated quizzes with 95% of correct answers in option A
- **Solution:** Engineered prompts with explicit randomization instructions
- **Result:** Achieved even distribution (20-25% per option)

**Problem 2: AI Hallucination**
- **Issue:** AI generated irrelevant content when given nonsense input
- **Solution:** Added content validation constraints to prompts
- **Result:** System returns empty results for invalid input instead of hallucinating

**Problem 3: Large Document Handling**
- **Issue:** OpenAI API has token limits
- **Solution:** Implemented text chunking (20k tokens/chunk) with natural boundary splitting
- **Result:** Successfully processes large PDFs and PowerPoints

**Techniques Used:**
- Explicit output format specifications (Q: / A: pattern for parsing)
- Difficulty-specific prompt instructions
- Content validation rules to prevent off-topic generation

---

### 3. Spring Security Configuration

**Problem:** API endpoints blocked with 401/403 errors
**Solution:** Created `SecurityConfig.java` to configure access control
**Result:**
- Public access for flashcard/quiz generation (no authentication needed)
- Protected endpoints for user-specific features (requires JWT)

---

### 4. Frontend Development (React)

**Initial MVP (Nov 13):**
- Built complete React frontend from scratch
- Study material input interface
- Tabbed layout for Flashcards, Quiz, and Chat
- API integration with backend services

**History Feature (Dec 3):**
- Added "History" tab for viewing saved flashcard/quiz sets
- Implemented CRUD operations (view, delete saved sets)
- Fixed critical data format mismatch bug

**Data Format Bug Fix:**
- **Problem:** Database stored quiz data as `{optionA, optionB, optionC, optionD, correctAnswer: 'A'}` but UI expected `{options: [], correctAnswer: 0}`
- **Solution:** Built transformation layer to convert between formats
- **Impact:** Prevented application crashes when loading saved quizzes

**Key Files:**
- `StudyAssistant.jsx` - Main component (co-authored with Fiachra Tobin)
- `StudyAssistant.css` - Component styling

---

### 5. Production Environment Fixes

**Issues Resolved:**
- Spring Security blocking endpoints → Created SecurityConfig
- React file watching crashes in VM environments → Added CHOKIDAR_USEPOLLING
- Repository test timing failures → Fixed assertions and added @Query annotations
- Maven build errors → Fixed application.properties syntax

---

## Team Collaboration

This was a 5-person team project. Work NOT done by me:

**Authentication System** - Abdul Wadood
**Document Upload/Parsing** - Abdul Wadood
**Database Entities** - Ngo Hung
**Frontend Redesign (3-panel layout)** - Fiachra Tobin
**CI/CD & DevOps** - Tomas Audejaitis

The `StudyAssistant.jsx` component was collaborative work with Fiachra Tobin (12 commits each).

---

## Technical Skills Demonstrated

**Backend:**
- OpenAI API integration
- Prompt engineering for AI reliability
- Spring Boot service development
- Spring Security configuration
- JUnit/Mockito testing
- RESTful API design

**Frontend:**
- React with hooks
- State management
- API integration
- Data transformation
- Production debugging

**Software Engineering:**
- Problem-solving (AI bias, data format mismatches)
- Test-driven development
- Technical documentation
- AI-assisted development

---

## Documentation

All my work is documented with timestamps, screenshots, and code examples:

**Dev-Logs:**
1. [Flashcard Generation](docs/dev-logs/HaoShe/2025-11-06-flashcard-generation.md)
2. [Quiz Generation](docs/dev-logs/HaoShe/2025-11-07-mcq-generation.md)
3. [Spring Security Fix](docs/dev-logs/HaoShe/2025-11-10-Spring-Security-Fix.md)
4. [Frontend Development](docs/dev-logs/HaoShe/2025-11-13-Frontend-Development.md)
5. [Answer Bias Fix](docs/dev-logs/HaoShe/2025-11-16-ai-output-sanitization-1.md)
6. [Hallucination Fix](docs/dev-logs/HaoShe/2025-11-17-ai-output-sanitization-2.md)
7. [Warning Integration](docs/dev-logs/HaoShe/2025-11-18-ai-output-sanitization-3.md)
8. [History Tab](docs/dev-logs/HaoShe/2025-12-03-history-tab-ui-implementation.md)

**AI Usage:** [developer-notes/ai-usage-002](developer-notes/ai-usage-002)

---

## Contact

**Hao She**
**Email:** hshe@tcd.ie
**LinkedIn:** http://www.linkedin.com/in/hao-she
**GitHub:** https://github.com/haoshe

---

*This document clearly delineates my contributions for portfolio and employment purposes. All information is verifiable through git history, dev-logs, and documentation.*
