# AI-Assisted Development Log: Flashcard Generator Feature

**Developer:** Hao (Person 2 - AI Integration)  
**Date:** November 6, 2025  
**Feature:** AI-Powered Flashcard Generation  
**AI Assistant:** Claude (Anthropic)

---

## Session Overview

This log documents how I used Claude AI to design and implement the flashcard generation feature for our AI-Powered Study Assistant project. The session demonstrates effective prompt engineering and iterative development with AI assistance.

---

## 1. Initial Context Setting

### My Prompt:
> "from the talk I had with you for the last few days, you probably know that I have a group project?"

### What Happened:
Claude recalled my project context (5-person AI-Powered Study Assistant, React frontend, Spring Boot backend, OpenAI API integration).

**Key Learning:** Claude has memory of previous conversations, so I could reference prior context without re-explaining everything.

---

## 2. Clarifying My Role

### My Prompt:
> "as you know, we need to show professor a minimum working version. I got person 2's job:
> 
> ### Person 2: AI Integration & Prompt Engineering
> Focus: OpenAI logic, Socratic tutoring behavior, adaptive learning
> [...]
> 
> This is the final version of the job, but for the minimum working version, I want to do something simpler, like using ai to create study questions or flash cards"

### Claude's Response:
Claude suggested flashcards are easier than questions because:
- Simple Q&A structure
- Easier to parse
- Less UI complexity
- Fewer edge cases

**Key Learning:** Being specific about my constraints (MVP, simpler version) helped Claude provide practical advice rather than over-engineering.

---

## 3. Getting Started with Git

### My Prompt:
> "ok, first, I need to create a new branch to implement this job, give me a good branch name"

### Claude's Suggestion:
```bash
feature/ai-flashcard-generator
```

**Key Learning:** Claude follows standard Git conventions. Asking for naming suggestions helps maintain professional code practices.

---

## 4. Addressing Dependencies

### My Prompt:
> "I need to upload some slides for the AI to generate the flash cards, but upload slides and parse slides are another person's job, how can I feed AI study material?"

### Claude's Solution:
Recommended accepting plain text input via API endpoint as a temporary solution:
- Work independently without waiting for file upload feature
- Easy to test immediately
- Simple to integrate later

**Key Learning:** Claude helped me identify a workaround for blockers, enabling parallel development with teammates.

---

## 5. Providing Project Context

### My Action:
Uploaded screenshot of project structure and `pom.xml` file

### My Prompt:
> "I need to upload some of my existing code to you before you show me actual code"

### Claude's Response:
Analyzed the project structure and identified:
- Spring Boot 3.5.7
- Spring AI 1.0.3 
- Java 21
- Lombok for boilerplate reduction
- Existing service/controller/dto pattern

**Key Learning:** Uploading existing code ensures generated code matches my project's style and dependencies. This prevents integration issues.

---

## 6. Code Generation Request

### Claude's Approach:
Without me asking explicitly, Claude proactively:
1. Created 4 Java files matching my project structure
2. Added comprehensive JavaDoc comments
3. Followed my existing naming conventions
4. Used Lombok annotations consistently
5. Included error handling and validation

### Files Generated:
1. **FlashcardRequest.java** - Request DTO
2. **Flashcard.java** - Response DTO  
3. **FlashcardService.java** - Business logic with OpenAI integration
4. **FlashcardController.java** - REST API endpoint

### Code Quality Features:
- Input validation (empty/null checks)
- Regex-based parsing with fallback logic
- Configurable flashcard count with defaults
- RESTful API design
- Proper error responses

**Key Learning:** Claude generated production-ready code, not just examples. The code included edge case handling I might have forgotten.

---

## 7. Test File Generation Issue

### Problem:
First test file made real API calls to OpenAI (expensive and slow)

### My Prompt:
> "can you produce a test based on spring boot?"

### Claude's Solution:
Generated two test files:
1. **FlashcardServiceTest.java** - Unit tests with Mockito
2. **FlashcardControllerTest.java** - Integration tests with MockMvc

**Key Features:**
- Mocked ChatModel to avoid API calls
- 6 unit tests covering different scenarios
- 8 integration tests for API endpoints
- No external dependencies needed

**Key Learning:** Being specific about "Spring Boot" tests led to proper testing practices with mocking frameworks.

---

## 8. Test Data Refinement

### Initial Issue:
Tests used biology examples (mitochondria, photosynthesis)

### My Prompt:
> "no, you misunderstood me, just change to studymaterial to spring related contents"

### Claude's Response:
Updated all test data to Spring Boot topics:
- Spring Boot framework
- @Autowired annotation
- Inversion of Control
- @RestController
- ApplicationContext

**Key Learning:** Short, direct feedback works well. Claude quickly understood and made targeted changes without regenerating everything.

---

## 9. Fixing Compilation Error

### Problem:
Test failed with: "The method call(String) is ambiguous for the type ChatModel"

### My Prompt:
> (Uploaded screenshot of error)
> "what is the problem?"

### Claude's Diagnosis:
ChatModel has multiple overloaded `call()` methods. Using `any()` in Mockito was ambiguous.

### Solution:
Changed from:
```java
when(chatModel.call(any())).thenReturn(...)
```

To:
```java
when(chatModel.call(any(Prompt.class))).thenReturn(...)
```

**Key Learning:** Screenshots help Claude quickly identify issues. Claude provided both the explanation and the fix, teaching me about Mockito best practices.

---

## 10. Testing Challenges

### Problem 1: Postman Not Working
Through forwarded Coder URL, Postman returned HTML instead of JSON.

### Claude's Recommendation:
Use curl in terminal instead - works perfectly on the server and looks professional for demos.

### Problem 2: `jq` Not Installed
curl piped to jq failed.

### My Prompt:
> (Uploaded screenshot of error)

### Claude's Quick Fix:
Use Python instead:
```bash
curl [...] | python3 -m json.tool
```

**Key Learning:** Claude adapted quickly when tools weren't available, providing alternative solutions without me asking.

---

## Prompt Engineering Techniques That Worked

### 1. **Reference Previous Context**
"from the talk I had with you for the last few days..."
- Leveraged Claude's memory feature
- Saved time re-explaining

### 2. **Be Specific About Constraints**
"for the minimum working version, I want to do something simpler"
- Got practical, not theoretical solutions
- Avoided over-engineering

### 3. **Upload Visual Context**
Screenshots of project structure, errors, Postman
- Faster diagnosis than describing in text
- More accurate solutions

### 4. **Ask "Why" Questions**
"which one is easier? generate questions or flash cards?"
- Got comparative analysis
- Made informed decisions

### 5. **Provide Existing Code**
Uploaded pom.xml and project structure
- Generated code matched my style
- Used correct dependencies
- No integration issues

### 6. **Give Direct Feedback**
"no, you misunderstood me, just change to spring related contents"
- Quick corrections
- Avoided full regeneration

---

## Code Quality Achieved

### Generated Code Includes:
✅ Comprehensive error handling  
✅ Input validation  
✅ JavaDoc documentation  
✅ RESTful API design  
✅ Proper HTTP status codes  
✅ Regex parsing with fallback  
✅ Configurable parameters with defaults  
✅ 14 comprehensive tests (6 unit + 8 integration)  
✅ Mockito mocking (no API costs)  
✅ Professional naming conventions  

### Lines of Code Generated:
- FlashcardService.java: ~120 lines
- FlashcardController.java: ~60 lines
- FlashcardServiceTest.java: ~205 lines
- FlashcardControllerTest.java: ~180 lines
- DTOs: ~40 lines
- **Total: ~605 lines of production-ready code**

---

## What I Learned About Working with AI

### Effective Strategies:
1. **Start with context** - Reference previous conversations
2. **Be specific about constraints** - MVP, time limits, team dependencies
3. **Upload code before requesting generation** - Ensures style consistency
4. **Use screenshots for errors** - Faster than describing
5. **Ask comparative questions** - Get guidance on tradeoffs
6. **Iterate with feedback** - Don't expect perfection first try
7. **Request tests separately** - Ensures proper testing practices

### Time Saved:
- **Without AI:** Estimated 8-10 hours (research, coding, testing, debugging)
- **With AI:** 4 hours (reviewing, testing, fixing integration issues)
- **Efficiency Gain:** ~60% time savings

### Quality Benefits:
- More comprehensive error handling than I would have written
- Better test coverage (14 tests vs probably 3-4 I'd write)
- Professional documentation
- Learned Mockito best practices
- Discovered Spring AI features I didn't know

---

## Deliverables from This Session

### Code Files:
- ✅ FlashcardController.java
- ✅ FlashcardService.java  
- ✅ Flashcard.java
- ✅ FlashcardRequest.java
- ✅ FlashcardServiceTest.java
- ✅ FlashcardControllerTest.java

### Documentation:
- ✅ QUICK_START.md
- ✅ FLASHCARD_README.md
- ✅ TESTING_GUIDE.md

### Status:
- ✅ All tests passing (14/14)
- ✅ API functional via curl
- ✅ Ready for MVP demo
- ✅ Ready to integrate with file upload feature

---

## Next Steps

### Planned for Next Session:
1. Integrate with Person 1's file upload feature
2. Add database persistence for flashcards
3. Improve prompt engineering for better question quality
4. Add more sophisticated parsing for edge cases
5. Begin work on Socratic tutoring feature

---

## Reflection

Working with Claude was highly effective for rapid prototyping. The key was treating it as a senior developer pair programming session - providing context, asking thoughtful questions, and iterating based on feedback. The generated code quality exceeded what I would have written alone in the same timeframe, and I learned several Spring Boot best practices along the way.

**Most Valuable Insight:** Uploading existing code before requesting new code generation is crucial. It ensures consistency and prevents integration headaches.

---

**End of Session - November 6, 2025**  
**Time Spent with AI:** 4 hours  
**Lines of Code Generated:** 605  
**Tests Written:** 14  
**Features Completed:** 1 (Flashcard Generation MVP)