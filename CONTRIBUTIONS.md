# My Contributions to AI Study Tutor Project

**Developer:** Hao She
**Role:** AI Integration & Frontend Developer
**Project Duration:** November - December 2025
**Team Size:** 5 developers

---

## Files I Authored (100% My Work)

### Backend Services
- `src/main/java/ie/tcd/scss/aichat/service/FlashcardService.java` - 100% authored
- `src/main/java/ie/tcd/scss/aichat/service/QuizService.java` - 100% authored

### Controllers
- `src/main/java/ie/tcd/scss/aichat/controller/FlashcardController.java` - 100% authored
- `src/main/java/ie/tcd/scss/aichat/controller/QuizController.java` - 100% authored

### Configuration
- `src/main/java/ie/tcd/scss/aichat/config/SecurityConfig.java` - 100% authored

### Tests
- `src/test/java/ie/tcd/scss/aichat/controller/FlashcardControllerTest.java` - 100% authored
- `src/test/java/ie/tcd/scss/aichat/controller/QuizControllerTest.java` - 100% authored
- `src/test/java/ie/tcd/scss/aichat/service/FlashcardServiceTest.java` - 100% authored
- `src/test/java/ie/tcd/scss/aichat/service/QuizServiceTest.java` - 100% authored

### DTOs
- `src/main/java/ie/tcd/scss/aichat/dto/FlashcardRequest.java` - 100% authored
- `src/main/java/ie/tcd/scss/aichat/dto/Flashcard.java` - 100% authored
- `src/main/java/ie/tcd/scss/aichat/dto/QuizRequest.java` - 100% authored
- `src/main/java/ie/tcd/scss/aichat/dto/QuizQuestion.java` - 100% authored

---

## Files I Authored/Co-Authored (Frontend)

### React Components
- `frontend/src/components/StudyAssistant.jsx` - **Co-authored with Fiachra Tobin**
  - **My contributions:** Initial MVP implementation (Nov 13), History tab feature (Dec 3), warning message integration, streaming support, bug fixes
  - **Fiachra's contributions:** Three-panel layout redesign, authentication screens, quiz scoring, file upload UI
  - **Contribution ratio:** 12 commits each to main component

- `frontend/src/components/StudyAssistant.css` - **Co-authored with Fiachra Tobin**
  - **My contributions:** Initial styling, History tab styles, tab styling fixes
  - **Fiachra's contributions:** Three-panel layout styling, responsive design enhancements

### Application Files
- `frontend/src/App.js` - Primary author (initial setup)
- `frontend/src/App.css` - Modified (logout button fix)
- `frontend/src/index.js` - 100% authored
- `frontend/src/index.css` - 100% authored

### Configuration & Scripts
- `frontend/start-frontend.sh` - 100% authored
- `frontend/.env` - Configuration additions (CHOKIDAR_USEPOLLING, DANGEROUSLY_DISABLE_HOST_CHECK)
- `frontend/package.json` - Initial setup with proxy configuration

---

## Files I Modified (Collaborative)

### Backend
- `src/main/resources/application.properties` - Fixed syntax error (line 3)
- `src/main/java/ie/tcd/scss/aichat/repository/FlashcardSetRepository.java` - Added @Query annotation
- `src/main/java/ie/tcd/scss/aichat/repository/QuizSetRepository.java` - Added @Query annotation
- `src/test/java/ie/tcd/scss/aichat/repository/FlashcardSetRepositoryTest.java` - Fixed timing issues
- `src/test/java/ie/tcd/scss/aichat/repository/QuizSetRepositoryTest.java` - Fixed timing issues

### Documentation
- `README.md` - Added frontend troubleshooting section (Nov 13), replaced with portfolio version (Jan 2026)

---

## Code Statistics

**Lines Written:**
- Backend Java: ~1,500 lines (services, controllers, tests, DTOs)
- Frontend JavaScript: ~1,200 lines (StudyAssistant component, initial version + History feature)
- Frontend CSS: ~600 lines
- **Total: ~3,300 lines of code**

**Tests Written:**
- 35+ comprehensive tests (unit + integration)
- 100% pass rate achieved

**Git Commits:**
- 15+ commits to backend code
- 12 commits to StudyAssistant.jsx
- Multiple bug fix and configuration commits

---

## Key Technical Decisions & Implementations

### 1. AI Integration Architecture
**Decision:** Use Spring AI framework with OpenAI GPT
**Rationale:** Built-in prompt templating, simplified API calls, better error handling
**Implementation:** FlashcardService.java, QuizService.java

### 2. Prompt Engineering Strategy
**Decision:** Explicit format specifications with fallback parsing
**Rationale:** Ensure consistent AI outputs while handling edge cases
**Key techniques:**
- Explicit output format instructions (Q: A: pattern)
- Randomization constraints for quiz answers
- Content-only constraints to prevent hallucination
- Difficulty-specific instructions

### 3. Text Chunking Algorithm
**Decision:** 20k tokens per chunk with natural boundary splitting
**Rationale:** Handle large documents while respecting API limits
**Implementation:** Split at paragraph/sentence boundaries, proportional distribution

### 4. Security Configuration
**Decision:** Public access for quiz/flashcard endpoints, JWT for others
**Rationale:** Allow unauthenticated generation while protecting user data
**Implementation:** SecurityConfig.java with requestMatchers

### 5. Data Transformation Layer
**Decision:** Transform database format to UI format in viewQuizSet()
**Rationale:** Bridge incompatible data structures without changing database schema
**Implementation:** Map {optionA/B/C/D, correctAnswer:'A'} to {options:[], correctAnswer:0}

### 6. Frontend State Management
**Decision:** React hooks with centralized state in StudyAssistant component
**Rationale:** Simplicity for team collaboration, easier to debug
**Implementation:** useState for flashcards, quizzes, history, warnings

---

## Development Timeline

### Week 1 (Nov 6-7, 2025)
- Implemented FlashcardService with OpenAI integration
- Implemented QuizService with difficulty levels
- Created controllers and DTOs
- Wrote comprehensive test suites

### Week 2 (Nov 10-13, 2025)
- Fixed Spring Security blocking issue
- Built initial React frontend from scratch
- Integrated frontend with backend APIs

### Week 3 (Nov 16-20, 2025)
- Fixed AI output quality issues (answer bias, hallucination)
- Integrated warning message system
- Added streaming support for flashcards/quizzes

### Week 4 (Dec 3, 2025)
- Implemented History tab feature (CRUD operations)
- Fixed data format mismatch bug
- Resolved repository test timing issues
- Created supervisor-friendly startup scripts

---

## Files NOT Authored by Me

### Authentication System
- `AuthController.java`, `AuthService.java`, `JwtTokenProvider.java` (Abdul Wadood)
- `UserDetailsServiceImpl.java` (Abdul Wadood)

### Document Parsing
- `DocumentParsingService.java` (Abdul Wadood)
- PDF/PPT parsing logic (Abdul Wadood)

### Database Entities
- `User.java`, `FlashcardSet.java`, `QuizSet.java`, `ChatSession.java` (Ngo Hung)
- Repository interfaces (base versions by Ngo Hung, modified by me)

### Chat System
- `ChatService.java`, `ChatController.java` (Multiple authors)
- Chat history management (Fiachra)

### Frontend Components (Not Mine)
- `Auth.js` - Login/Registration UI (Fiachra)
- Three-panel layout redesign (Fiachra)
- File upload components (Abdul + Fiachra)

### DevOps & CI/CD
- `.gitlab-ci.yml` (Tomas)
- Docker configuration (Tomas)
- Merge conflict resolution (Tomas)

---

## Verification

All my contributions are documented with:
- ✅ Git commit history showing my authorship
- ✅ Detailed dev-logs with timestamps and screenshots
- ✅ Test results and API testing evidence
- ✅ Original code files with consistent coding style

### Dev-Logs (My Work)
1. [2025-11-06-flashcard-generation.md](docs/dev-logs/HaoShe/2025-11-06-flashcard-generation.md) - Flashcard implementation
2. [2025-11-07-mcq-generation.md](docs/dev-logs/HaoShe/2025-11-07-mcq-generation.md) - Quiz implementation
3. [2025-11-10-Spring-Security-Fix.md](docs/dev-logs/HaoShe/2025-11-10-Spring-Security-Fix.md) - Security config fix
4. [2025-11-13-Frontend-Development.md](docs/dev-logs/HaoShe/2025-11-13-Frontend-Development.md) - Initial frontend
5. [2025-11-16-ai-output-sanitization-1.md](docs/dev-logs/HaoShe/2025-11-16-ai-output-sanitization-1.md) - Answer bias fix
6. [2025-11-17-ai-output-sanitization-2.md](docs/dev-logs/HaoShe/2025-11-17-ai-output-sanitization-2.md) - Hallucination fix
7. [2025-11-18-ai-output-sanitization-3.md](docs/dev-logs/HaoShe/2025-11-18-ai-output-sanitization-3.md) - Warning integration
8. [2025-12-03-history-tab-ui-implementation.md](docs/dev-logs/HaoShe/2025-12-03-history-tab-ui-implementation.md) - History tab

### AI Usage Documentation
- [developer-notes/ai-usage-002](developer-notes/ai-usage-002) - My AI-assisted development process

---

## Contact for Verification

For questions about my contributions or verification:

**Hao She**
**Email:** hshe@tcd.ie
**LinkedIn:** http://www.linkedin.com/in/hao-she
**GitHub:** https://github.com/haoshe

**Original Team Repository:** https://gitlab.scss.tcd.ie/csu33012-2526-group23/csu33012-2526-project23

---

*This document was created to clearly delineate my contributions for portfolio and employment purposes. All information is verifiable through git history, dev-logs, and the original team repository.*
