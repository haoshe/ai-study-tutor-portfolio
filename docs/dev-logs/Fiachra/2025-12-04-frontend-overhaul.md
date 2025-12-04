Date: December 4, 2025
Developer: Fiachra
Branch: Frontend

### Objective

Redesign the frontend to use a three-panel interface while ensuring full compatibility with all backend endpoints.
Improve user experience for flashcard and quiz generation, fix critical bugs, implement correct behaviors (flipping flashcards, real quiz submission, proper panel expansion), and shift the theme from dark to a bright blue–white–green palette.

# Background

The original UI consisted of a simple single-panel interface with limited functionality and several bugs.
After uploading all frontend and backend files, the team identified several new requirements and improvements needed:

# New UI Requirements

NotebookLM-style three-panel layout:

Left: File upload + Sources

Middle: Flashcard & Quiz generation + Settings + History

Right: AI Chat assistant

Each panel must have an expand/collapse feature.

Entire UI must adopt a blue, white, and green theme (instead of the previous dark theme).

# Feature Improvements Needed

Flashcards and quizzes must stay on the active tab during generation and show a loading spinner.

Flashcards should flip on click using 3D animation.

Quizzes should act like real tests:

User selects answers

User presses Submit Quiz

UI shows correctness and score breakdown

# Existing System Components Referenced

Source upload & SourceManager logic

SavedSources UI structure

Shared components: Panel, WarningBanner, Spinner, etc.

Panel container structure informed layout corrections

Warning banners and spinner reused in new flow

# Development Process
# Step 1: Analyze backend endpoints

Prompt: “Can you walk me through what all the controllers in my backend do so I know what endpoints I’m supposed to connect to?”

Action: Reviewed controllers for flashcards, quizzes, sources, file uploads, AI chat, and authentication to map the correct frontend integrations.

Learning:
This improved understanding of how frontend and backend communicate, how REST resources are structured, and how React components depend on backend response shapes.

# Step 2: Create the new three-panel NotebookLM layout

Prompt: “How can I create a three-panel NotebookLM-style layout with collapsible panels in React?”

Action:

Built a CSS grid layout with three equal columns.

Added expand/collapse buttons to each panel.

Applied grid logic:

Left expanded → 1fr 0 0

Middle expanded → 0 1fr 0

Right expanded → 0 0 1fr

Applied .hidden class to minimize collapsed panels.

Learning:
This taught correct use of grid-template-columns and how expanding one region requires explicitly collapsing others. Also learned to separate layout logic from component content.

# Step 3: Implement new light blue–white–green theme

Prompt: “How do I restyle this component using blue/white/green while keeping my existing Panel component structure?”

Action:

Replaced dark backgrounds with pure white surfaces.

Used NotebookLM-inspired blues (#1E88E5) and greens (#4CAF50).

Applied subtle gray borders similar to existing components.

Learning:
This introduced consistent color systems and taught how to create a unified visual identity based on reusable tokens.

# Step 4: Fix quiz generation and scoring

Prompt: “My quiz always marks answers as wrong. Can you help me debug the scoring logic?”

Action:

Inspected backend quiz response format.

Found mismatch between frontend userAnswers[i] and backend correctAnswer index.

Fixed logic to compare indices correctly.

Learning:
Improved debugging skills by checking data shapes, verifying backend assumptions, and mapping state transitions correctly.

# Step 5: Add real quiz-taking flow

Prompt: “Can you make the quiz behave like a real test where the user submits answers at the end?”

Action:

Added radio button answer selection.

Added Submit Quiz button.

Implemented scoring logic.

UI now shows:

Correct/incorrect marking

Score summary

Buttons for Retake Quiz and New Quiz

Learning:
Introduced UI state machine thinking → distinguishing “in-progress”, “submitted”, and “review” states.

# Step 6: Flashcard flipping animation

Prompt: “How do I make flashcards flip on click using CSS transforms?”

Action:

Removed “Show Answer” button.

Implemented 3D flip using transform: rotateY(180deg).

Added smooth transitions and backface-hiding logic.

Learning:
Gained understanding of 3D transforms, preserve-3d, and how to manage per-card flipped state.

# Step 7: Improve generation UX

Prompt: “When generating flashcards, the UI switches tabs automatically — how do I keep the user on the same tab and show a loading indicator?”

Action:

Generating now switches to the correct tab (Flashcards or Quiz).

Shows Spinner while loading.

Results display on the same tab when complete.

Learning:
Understood importance of predictable UX flow and how loading states fit within tabbed UI systems.

# Step 8: Add source selection warnings

Prompt: “How can I show a warning when the user tries to generate without selecting any sources?”

Action:

Added WarningBanner before API call.

Prevented request when no sources selected.

Learning:
Improved error-handling patterns and learned to implement guard clauses in frontend flows.

# Step 9: Add ability to generate multiple sets

Prompt: “Add a ‘New Quiz’ button after results display so I can regenerate from the same tab.”

Action:

Added New Flashcard Set and New Quiz Set actions.

Reset logic updated to allow multiple generations seamlessly.

Learning:
Better understanding of UX loops and designing repeatable workflows in the UI.

# Step 10: Fix expansion bugs in side panels

Prompt: “Can you fix the expand animation so it doesn't push the other panels sideways?”

Action:

Debugged incorrect grid collapse behavior.

Ensured wrappers matched Panel.tsx container structure.

Applied width constraints and flexbox fixes.

Learning:
Grew more confident debugging nested layout systems and recognizing container–child relationships.

# Outcome

All major issues were resolved and the frontend is now fully functional and visually consistent:

Fully redesigned NotebookLM-style three-panel UI

Working expand/collapse for all three panels

Real quiz submission and accurate scoring

Flashcards with flip animation

Proper loading flow and source validation

Full compatibility with existing backend endpoints

Clean new blue–white–green theme

Ability to generate multiple flashcard/quiz sets

This significantly improves the application's stability, usability, and overall user experience.

# Bugs Found During Review

Saved Quiz grading always marked answers incorrect.

Generating without selecting any sources did not warn the user.

Side panel expand buttons broke the grid layout.

After generating a flashcard or quiz set, there was no way to generate another set.

CSS truncation caused missing layout styles on first redesign attempt.

#  Backend Limitations Identified
During frontend integration, these backend issues were confirmed:

# Saved Quizzes do not save correct answer indices
When Quiz initially generated it knows which anwers are correct. However when Quiz opened from the history tab it marks all possible answers as incorrect.


# AI chat lacks session persistence
conversationId is created, but backend does not reliably:
store message history


reload past conversations


assign conversations per user consistently


# UI Bugs

Drag-and-drop sometimes triggers browser navigation



# Impact Assessment
3 panel style complete
AI Chat UI implemented
Drag-and-drop and text source creation implemented
Issues Highlighted


# Files Modified
frontend/src/components/StudyAssistant.jsx
frontend/src/components/StudyAssistant.css
frontend/src/Auth.css 
frontend/src/Auth.jsx
frontend/src/App.jsx 
frontend/src/App.css 
