# Backend & Frontend Enhancements – Persistent Sources, Chat History, and Stability Fixes

Date: December 7, 2025
Developer: Fiachra
Branch: feature/new_ui

# Objective

Merge the updated database branch, database_large_files, into feature/new_ui, Implement persistent storage for study sources, add database-backed chat history, resolve MySQL truncation issues, and enhance frontend UI behavior for consistent persistence and smooth user experience.

Development Process
# Step 0 — Merging database_large_files into feature/new_ui

Prompt:

"I'm trying to merge a branch with an updated database into the branch with the updated frontend. How should I resolve the issues, and what are the consequences?"

Response:

Walked through resolving merge conflicts between the redesigned NotebookLM-style frontend and the backend-focused database_large_files branch. Identified which parts of each branch should be preserved:

Kept all new UI code from feature/new_ui (three-panel layout, updated study workflow, new styling).

Merged backend improvements from database_large_files, including enhanced error handling, improved API_BASE_URL configuration, and updated repository/service logic.

Resolved SecurityConfig and JwtAuthenticationFilter conflicts by combining CORS improvements from the database branch with the correct token expectations from the new UI.

Fixed application.properties conflict (whitespace / no functional differences).

Repaired frontend merge artifacts, removing leftover variables (activeTab, setVisibleAnswers, etc.) that no longer existed in the new UI.

Integrated quiz-answer normalization so reopened quizzes from the database correctly map letter answers (“A”, “B”, “C”, “D”) to indices (0–3).

# Phase 1 — Backend Source Management System
# Step 1: Storing sources
Prompt:
"How would I add a way to store (and delete) sources on the database. 

Response:

Explained the required backend structure (Entity → Repository → Service → Controller).

Clarified why source documents should be owned by the backend for persistence.

Demonstrated how to convert the existing concept of "Material" into a clearer Sources domain model.

# What was learned:
Backend persistence flows require consistent structure, and naming clarity improves maintainability across the project.

# Step 2: Modify DocumentController to save uploaded files as Sources

Prompt:
"How would I add the ability to upload multiple files by selecting them in one go."

Response:

Pointed out the part of the upload handler needing modification.

Instructed how to wrap each uploaded file into a new Sources entity.

Explained why returning only extracted text was insufficient for a persistent system.

# What was learned:
File-processing endpoints should return full stored objects so the frontend can remain simple and stateless.

# Phase 2 — Frontend Integration for Persistent Sources
# Step 1: Load sources from backend on mount

Prompt:
"What are the minimum necessary changes to update frontend."

Response:

Highlighted where in the component lifecycle (useEffect) the source retrieval should occur.

Provided surrounding lines to ensure the code could be inserted in the correct location.

Ensured tokens were included in all authenticated requests.

# What was learned:
Incremental updates are easier and safer when contextual code is shown, preventing accidental breaks.

# Step 2: Persist selectedSources in localStorage

Prompt:
"Is it possible as well to get it to remember which sources were selected as well when they refresh?"

Response:

Recommended serializing the Set of selected IDs into localStorage.

Demonstrated how to restore those values on page load.

What was learned:
Small UI states can significantly improve UX when persisted locally.

# Step 3: Fix “select all” persistence behavior

Prompt:
"It remembers when I select them individually but using select all doesn't remember."

Response:

Identified that the “select all” branch lacked the localStorage write operation.

Showed how to update the stored array in both toggle directions.

# What was learned:
All branches of a toggle must maintain identical side effects to avoid inconsistent UI behavior.

# 4: Backend-connected source deletion

Prompt:
"What happens if you remove a source? Does it actually remove it?"

Response:

Provided DELETE endpoint logic and instructed where to call it from the frontend.

Demonstrated optimistic UI updates to immediately remove deleted items.

# What was learned:
Optimistic UI patterns create a responsive, modern user experience while keeping state consistent.

# Phase 3 — Database-Backed Chat History
# Step 1: Introduce ChatSession + ChatMessage persistence model

Prompt:
"How difficult would it be to implement a way for the UI to remember AI chats?"

Response:

Explained that chat persistence requires two related tables (sessions + messages).

Provided an incremental adoption strategy that would not break existing chat behavior.

Outlined minimal required endpoints for saving and retrieving messages.

# What was learned:
Persistent features require proper data modeling rather than quick frontend fixes.

# Step 2: Replace frontend-generated sessionId with backend-created one

Prompt:
"For this implementation, will it remember my chats if I stop and rerun the backend?"

Response:

Clarified that frontend-generated IDs cannot persist across application restarts.

Instructed how to create sessions via POST /api/chat/history/session.

Showed how to save the returned session ID for subsequent messages.

# What was learned:
Persistent identifiers must be created and owned by the backend.

# Phase 4 — Fixing SQL Truncation (“Data too long for column 'content'”)
Step 1: Diagnose backend failure affecting blank chat messages

Prompt:
"AI chat no longer produces anything but a blank box."

Response:

Interpreted the MySQL error message from logs.

Connected it to the assistant message failing to save, causing the frontend to display empty content.

Identified content column type as the root cause (VARCHAR too small).

# What was learned:
Frontend side effects often originate from backend database constraints.

# Step 2: Update schema using LONGTEXT

Prompt:
"Should I change my setup-database.sh?"

Response:

Recommended adding ALTER TABLE chat_message MODIFY content LONGTEXT; to the automated setup script.

Explained that schema changes must be reproducible across developer machines.

# What was learned:
Database migrations should always be automated to avoid inconsistent environments.

# Phase 5 — Frontend Chat Behavior Fixes
Step 1: Prevent duplicate messages

Prompt:
"Chat seems to be duplicating messages."

Response:

Explained that messages were being appended both during optimistic UI updates and again when reloading history.

Guided restructuring so messages were not pushed into state twice.

# What was learned:
Hydration logic must not overlap with optimistic rendering.

# Step 2: Fix chat suggestions flashing for half a second

Prompt:
"Suggestions load for half a second, then the chat loads—can they be hidden when messages already exist?"

Response:

Pointed to the conditional block controlling suggestion rendering.

Introduced a chatLoaded flag to hide suggestions until message retrieval completed.

# What was learned:
Loading-state conditions require more than simple checks like messages.length === 0.

# Step 3: Fix empty assistant bubbles (post LONGTEXT fix)

Prompt:
"Now it's not duplicating but the AI chat bubble is empty."

Response:

Confirmed that the issue resolved after LONGTEXT migration once messages successfully serialized.

# What was learned:
Database constraints can silently break frontend display without throwing visible UI errors.

# Phase 6 — Database Setup Script Enhancement

Prompt:
"I've added the following file already—add it to the dev log."

Response:

Verified the updated script and confirmed it ensures consistent schema setup across the team.

Explained why Hibernate does not auto-upgrade VARCHAR → LONGTEXT.

# What was learned:
Schema changes must be explicitly enforced in automation scripts.

# Completed Work Summary
# Backend:

Added complete persistent Sources system

Added chat history storage: ChatSession + ChatMessage

Added endpoints for CRUD operations

Fixed MySQL truncation via LONGTEXT migration

Updated setup script to apply schema migrations automatically

# Frontend:

Multi-file upload support
Persistent source selection with localStorage
Automatic loading of saved sources
Proper delete functionality across UI + backend
Chat persistence across page reloads and backend restarts
Removal of duplicate messages
Hiding suggestions when history exists

# Files Modified Summary
#  Backend Files Created
Sources.java    q           Created Entity for persisting study sources
SourcesRepository.java      Created JPA repository for sources
SourcesService.java         Created Service layer for source operations
SourcesController.java      Created REST endpoints for source management
ChatSession.java            Created Entity for chat sessions
ChatMessage.java            Created Entity for chat messages
ChatSessionRepository.java  Created JPA repository for chat sessions
ChatMessageRepository.java  Created JPA repository for chat messages
ChatHistoryService.java     Created Service layer for chat history
ChatHistoryController.java  CreatedREST endpoints for chat history

# Backend Files Modified
DocumentController.java     Now saves uploaded files as Sources entities

# Frontend Files Modified
StudyAssistant.jsx          Complete overhaul of source and chat management

# Database Changes
New table: sources          Stores user study materials
New table: chat_session     Stores chat session metadata
New table: chat_message     Stores individual chat messages

# Other File Modifications:
setup-database.sh          Added automatic database migration to update the chat_message.content column to          LONGTEXT, ensuring that long AI responses can be stored without MySQL truncation errors.
