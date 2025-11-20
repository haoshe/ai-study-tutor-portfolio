Date: November 21, 2025
Developer: Fiachra
Branch: feature/addPDFfield

Objective

Confirm the backend API URL for manual testing, resolve CLI request issues, and diagnose the true cause behind flashcard generation failures when processing large PDFs.

Background

Flashcards and quiz generation were repeatedly failing when processing large files (e.g., Great Expectations). The frontend displayed a 403 suggesting "text too short or unsupported," but this message did not align with the actual backend logs.
Manual curl testing was attempted to isolate the backend behaviour independently of the frontend.

Step 1: Confirming Backend URL

A manual request was attempted via:

bashcurl -X POST http://localhost:8080/api/flashcards


Issue:
User accidentally typed bashcurl instead of curl, resulting in:

bash: bashcurl: command not found


Fix:
Corrected CLI command:

curl -X POST http://localhost:8080/api/flashcards \
  -H "Content-Type: application/json" \
  -d '{ "text": "Your test content here", "count": 5 }'


Result:
Request successfully reached backend.

Step 2: Verifying Backend URL From Logs

Spring Boot output contained:

Tomcat started on port 8080 (http) with context path '/'


Conclusion:
The backend is running at:

http://localhost:8080

This confirms that all frontend and manual tools must target port 8080, not the frontend's 3000 port.

Step 3: Discovering the Real Error (NOT a 403)

Backend logs showed the critical information:

HTTP 429 - Request too large for gpt-5-nano
Limit 200000, Requested 256633 tokens


Key findings:

PDF parsing succeeded — text extraction worked correctly.

Backend forwarded ~256k tokens to OpenAI in a single request.

Model in use (gpt-5-nano) only allows 200k tokens per minute.

OpenAI returned 429 Too Many Tokens.

Our backend currently wraps all errors → frontend sees a misleading 403 Forbidden.

Root Cause:
We were sending the entire book to OpenAI at once.

Step 4: Fix Strategy (Chunking Required)

Three potential fixes were considered:

Truncate text

Switch to a higher-capacity model

Split text into chunks and generate per-chunk → combine results

Chosen: Option 3 (Chunking)
This gives correct output, handles large inputs, avoids token limits, and works across all models.

Proposed backend change:

Chunk input text to ~20k tokens (80k chars)

Generate flashcards per chunk

Merge results

Gracefully handle per-chunk errors

Avoid sending oversized requests

Step 5: Files to Modify

Location of backend logic:

src/main/java/ie/tcd/scss/aichat/service/


Files requiring updates:

FlashcardService.java

QuizService.java

Changes implemented:

Added chunking logic to both services

Added per-chunk generation helpers

Replaced monolithic generate methods

Added logging + error resilience

Sign-off

Status:

Backend URL confirmed

CLI request fixed

Token overflow issue identified

Chunking implementation completed in both flashcard & quiz services

Impact:
Large PDFs (e.g., Great Expectations) are now fully supported without hitting OpenAI token limits.