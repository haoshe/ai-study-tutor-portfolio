# AI Study Tutor - Portfolio Showcase

> **Note:** This is a portfolio fork of a 5-person group project from CSU33012 (Trinity College Dublin, 2025-26).
> This repository highlights **my specific contributions** to the project.

---

##  Project Overview

An AI-powered study assistant that generates flashcards, quizzes, and provides personalized tutoring from uploaded study materials (PDFs, PowerPoint, text). The system uses OpenAI GPT to transform passive study materials into interactive learning experiences.

**Team Size:** 5 developers
**My Role:** AI Integration & Frontend Developer
**Duration:** November - December 2025
**Tech Stack:** Java 21, Spring Boot 3.5, React 18, MySQL, OpenAI API

---

##  My Contributions

### 🤖 Backend AI Integration (Java/Spring Boot)

**What I Built:**
- AI-powered flashcard generation service with OpenAI GPT integration
- Multiple-choice quiz generator with three difficulty levels (Easy, Medium, Hard)
- Smart text chunking algorithm for processing large documents (20k tokens per chunk)
- Spring Security configuration for API endpoint access control
- Comprehensive test suites: 35+ tests with 100% pass rate

**Key Achievement: Solved Critical AI Quality Issues**

Through advanced prompt engineering, I fixed major reliability problems:

| Problem | Solution | Result |
|---------|----------|--------|
| **Quiz answer bias** (95% of correct answers in option A) | Added explicit randomization instructions to prompts | Even distribution across all options (20-25% each) |
| **AI hallucination** (generated off-topic content for gibberish input) | Implemented strict content constraints in prompts | Returns empty array for invalid content |
| **Content quality** (repetitive or low-quality outputs) | Designed validation and warning system | Users get clear feedback on AI-generated content |

**Files I Authored:**
- [FlashcardService.java](src/main/java/ie/tcd/scss/aichat/service/FlashcardService.java) - AI flashcard generation
- [QuizService.java](src/main/java/ie/tcd/scss/aichat/service/QuizService.java) - AI quiz generation
- [FlashcardController.java](src/main/java/ie/tcd/scss/aichat/controller/FlashcardController.java) - REST API
- [QuizController.java](src/main/java/ie/tcd/scss/aichat/controller/QuizController.java) - REST API
- [SecurityConfig.java](src/main/java/ie/tcd/scss/aichat/config/SecurityConfig.java) - Security configuration
- Test files: FlashcardControllerTest, QuizControllerTest, FlashcardServiceTest, QuizServiceTest

**Detailed Documentation:** [My Dev-Logs](docs/dev-logs/HaoShe/)

---

###  Frontend Development (React)

**What I Built:**
- **Initial MVP Frontend** (Nov 13): Complete React application from scratch with study material input, flashcard/quiz generation, tabbed interface
- **History Management Feature** (Dec 3): Full CRUD operations for saved flashcard and quiz sets
  - Fetch and display saved study materials
  - View saved sets with automatic data transformation
  - Delete saved sets with confirmation dialogs
- **Warning Message System** (Nov 18): User feedback for AI content quality and quantity
- **Streaming Support** (Nov 20): Real-time content generation endpoints
- **Production Bug Fixes**: Authorization headers, logout button sizing, EMFILE errors for VM environments

**Key Technical Achievement:**

Fixed critical data format mismatch bug in quiz viewing functionality. The database stored quiz data in format `{optionA, optionB, optionC, optionD, correctAnswer: 'A'}` while the UI expected `{options: [], correctAnswer: 0}`. Implemented a transformation layer to bridge the formats, preventing application crashes.

**Collaborative Work:**

Teammate Fiachra Tobin contributed: three-panel layout redesign, authentication UI, quiz scoring system, and file upload integration. The final frontend represents collaborative iteration (12 commits each to main component).

**Files I Authored/Co-Authored:**
- [StudyAssistant.jsx](frontend/src/components/StudyAssistant.jsx) - Main component (initial version + History feature)
- [StudyAssistant.css](frontend/src/components/StudyAssistant.css) - Component styling
- [App.css](frontend/src/App.css) - Application styling fixes

**Detailed Documentation:** [Frontend Dev-Log](docs/dev-logs/HaoShe/2025-11-13-Frontend-Development.md) | [History Tab Dev-Log](docs/dev-logs/HaoShe/2025-12-03-history-tab-ui-implementation.md)

---

###  Production Debugging & DevOps

**Issues I Resolved:**
- Spring Security blocking API endpoints (401/403 errors) - Created SecurityConfig
- Repository test timing failures - Added @Query annotations and fixed assertions
- React EMFILE errors in VM environments - Configured polling-based file watching
- Maven build configuration issues - Fixed application.properties syntax
- Coder workspace authorization - Implemented custom header handling

**Files Modified:**
- `application.properties` - Fixed syntax errors
- Repository test files - Fixed timing-dependent assertions
- `frontend/.env` - Added CHOKIDAR_USEPOLLING for compatibility
- `frontend/start-frontend.sh` - Created supervisor-friendly startup script

---

##  Development Process & AI-Assisted Development

I documented my entire development process in detailed dev-logs, demonstrating:
- **AI-assisted development workflow** using Claude AI
- **Prompt engineering techniques** for reliable AI outputs
- **Debugging sessions** with screenshots and solutions
- **Testing strategies** with comprehensive coverage
- **Time efficiency analysis**: Achieved 92% time savings through strategic AI use while maintaining production quality

**All Development Logs:** [docs/dev-logs/HaoShe/](docs/dev-logs/HaoShe/)

**AI Usage Documentation:** [developer-notes/ai-usage-002](developer-notes/ai-usage-002)

---

##  Skills Demonstrated

**Backend Development:**
- OpenAI API integration and prompt engineering
- Spring Boot service and controller development
- Spring Security configuration
- JUnit/Mockito testing (35+ tests, 100% pass rate)
- RESTful API design

**Frontend Development:**
- React component development with hooks
- State management and data transformation
- Responsive UI design
- Production debugging

**Software Engineering:**
- AI-assisted development with quality control
- Test-driven development
- Git workflow and CI/CD
- Technical documentation
- Problem-solving and debugging

---

##  Architecture

```
┌─────────────────────────────────────────┐
│     FRONTEND (React)                    │
│  - Study material input                 │
│  - Flashcard/Quiz generation            │
│  - History management (MY WORK)         │
│  - JWT authentication                   │
└──────────────┬──────────────────────────┘
               │ RESTful API (JSON)
┌──────────────▼──────────────────────────┐
│     BACKEND (Spring Boot)               │
│  - FlashcardService (MY WORK)           │
│  - QuizService (MY WORK)                │
│  - OpenAI integration (MY WORK)         │
│  - Security config (MY WORK)            │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│     DATABASE (MySQL)                    │
│  - User authentication                  │
│  - Flashcard/Quiz persistence           │
│  - Chat history                         │
└─────────────────────────────────────────┘
```

---

##  Repository Structure

```
ai-study-tutor-portfolio/
├── README.md                          # This file
├── CONTRIBUTIONS.md                   # Detailed contribution breakdown
├── src/
│   ├── main/java/.../
│   │   ├── service/
│   │   │   ├── FlashcardService.java     #  I wrote this
│   │   │   ├── QuizService.java          #  I wrote this
│   │   │   └── ...                       # Other services (teammates)
│   │   ├── controller/
│   │   │   ├── FlashcardController.java  #  I wrote this
│   │   │   ├── QuizController.java       #  I wrote this
│   │   │   └── ...                       # Other controllers (teammates)
│   │   └── config/
│   │       └── SecurityConfig.java       #  I wrote this
│   └── test/java/.../                    #  I wrote test files
├── frontend/
│   └── src/components/
│       ├── StudyAssistant.jsx            #  I built initial version + History feature
│       ├── StudyAssistant.css            #  I authored styling
│       └── ...                           # Other components (teammates)
├── docs/dev-logs/HaoShe/                 #  My development logs
│   ├── 2025-11-06-flashcard-generation.md
│   ├── 2025-11-07-mcq-generation.md
│   ├── 2025-11-10-Spring-Security-Fix.md
│   ├── 2025-11-13-Frontend-Development.md
│   ├── 2025-11-16-ai-output-sanitization-1.md
│   ├── 2025-11-17-ai-output-sanitization-2.md
│   ├── 2025-11-18-ai-output-sanitization-3.md
│   └── 2025-12-03-history-tab-ui-implementation.md
└── developer-notes/
    ├── ai-usage-001.md                   # General AI usage overview
    └── ai-usage-002                      #  My AI usage documentation
```

---

## 🤝 Team Attribution

This project was built by a team of 5 developers:

- **Hao She (me):** AI Integration, Frontend Development, Testing
- **Abdul Wadood:** Document Upload, PDF/PPT Parsing, Security Audit
- **Fiachra Tobin:** Frontend Components, Login Implementation, UI Redesign
- **Tomas Audejaitis:** CI/CD, DevOps, Merge Management
- **Ngo Hung:** Database Integration, Error Handling, Automated Testing

---

## 🚀 Running the Project

### Prerequisites
- Java 21
- Node.js 18+
- MySQL 8.0+
- OpenAI API key

### Backend Setup

```bash
# Clone repository
git clone https://github.com/haoshe/ai-study-tutor-portfolio.git
cd ai-study-tutor-portfolio

# Set up database
chmod +x setup-database.sh
./setup-database.sh

# Configure environment variables
# Create a .env file in the root directory:
echo "export OPENAI_API_KEY=your_key_here" > .env

# Build and run
source .env
mvn clean install
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`

### Frontend Setup

```bash
# Navigate to frontend
cd frontend

# Create frontend/.env file:
cat > .env << EOF
DANGEROUSLY_DISABLE_HOST_CHECK=true
CHOKIDAR_USEPOLLING=true
EOF

# Install dependencies
npm install

# Start development server
npm start
# OR use the startup script
./start-frontend.sh
```

Frontend runs on `http://localhost:3000`

---

##  Links

- **Portfolio Repository:** https://github.com/haoshe/ai-study-tutor-portfolio
- **My Development Logs:** [docs/dev-logs/HaoShe/](docs/dev-logs/HaoShe/)
- **Detailed Contributions:** [CONTRIBUTIONS.md](CONTRIBUTIONS.md)

---

## 📝 License

This project was developed as part of CSU33012 coursework at Trinity College Dublin (2025-26).

---

##  Contact

**Hao She**
**Email:** hshe@tcd.ie
**LinkedIn:** http://www.linkedin.com/in/hao-she
**GitHub:** https://github.com/haoshe

*Interested in discussing AI integration, prompt engineering, or full-stack development? Let's connect!*
