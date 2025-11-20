Date: November 20, 2025
Developer: Fiachra
Branch: frontend_file_upload

Objective
Merge two versions of the StudyAssistant component to combine file upload capabilities (PDF/PowerPoint support) with advanced quiz features (scoring, real-time feedback, customizable parameters) and flashcard enhancements (toggleable answers, warnings).

Background
Two separate implementations of the StudyAssistant component existed:

File 1: Advanced quiz functionality (scoring system, feedback, answer selection tracking, customizable counts/difficulty, flashcard toggling, warning messages, loading states, reset functionality)
File 2: File upload capability (PDF/PowerPoint document parsing, text extraction, upload UI with file display)
The goal was to create a unified component that provides all functionality from both implementations while maintaining clean code structure and proper state management.

Development Process
Step 1: Analyze Component Structure
Components to Merge:

File 1 Features:

Advanced quiz scoring with visual progress bar
Real-time answer feedback (correct/incorrect indicators)
Toggle flashcard answer visibility
Customizable flashcard count (1-10)
Customizable quiz count (1-10)
Difficulty selector (EASY/MEDIUM/HARD)
Warning messages for both flashcards and quizzes
Separate loading states per content type (loadingType)
Reset buttons with × symbol in tabs
Loading spinners
Score calculation logic
Answer tracking for quizzes
File 2 Features:

PDF/PowerPoint file upload
Automatic text extraction from documents
File validation (type checking)
Uploaded filename display with checkmark
OR divider between upload and manual input
Support for using either uploaded content or manual text input
File input hidden with label button
Key Differences to Reconcile:

File 1 used separate loadingType state to track what's loading
File 2 lacked advanced quiz features but had file upload
Both had different error handling approaches
File 2 didn't persist the uploaded content separately from input text
Step 2: Design Unified State Management
Final State Variables:

JavaScript
// Study Material States
const [studyMaterial, setStudyMaterial] = useState('');
const [uploadedContent, setUploadedContent] = useState('');
const [uploadedFileName, setUploadedFileName] = useState('');

// Flashcard States
const [flashcards, setFlashcards] = useState([]);
const [visibleAnswers, setVisibleAnswers] = useState({});
const [flashcardCount, setFlashcardCount] = useState(5);
const [flashcardWarning, setFlashcardWarning] = useState('');

// Quiz States
const [quizzes, setQuizzes] = useState([]);
const [userAnswers, setUserAnswers] = useState({});
const [quizCount, setQuizCount] = useState(5);
const [difficulty, setDifficulty] = useState('MEDIUM');
const [quizWarning, setQuizWarning] = useState('');

// UI States
const [loading, setLoading] = useState(false);
const [error, setError] = useState('');
const [activeTab, setActiveTab] = useState('flashcards');
const [loadingType, setLoadingType] = useState('');
Rationale:

Separate uploadedContent from studyMaterial to allow switching between sources
Maintained loadingType from File 1 for precise loading state tracking
Preserved all warning states for both flashcards and quizzes
Step 3: Implement Unified Content Logic
Content Selection Strategy:

JavaScript
const contentToUse = uploadedContent || studyMaterial;

if (!contentToUse.trim()) {
  setError('Please upload a document or enter some study material');
  return;
}
Decision:

Prioritize uploaded content if available
Fall back to manual text input
Show unified error message supporting both input methods
Step 4: Merge File Upload Handler
Implementation:

JavaScript
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const validTypes = ['application/pdf', 'application/vnd.ms-powerpoint', 
                      'application/vnd.openxmlformats-officedocument.presentationml.presentation'];
  if (!validTypes.includes(file.type)) {
    setError('Please upload a PDF or PowerPoint file');
    return;
  }

  setLoading(true);
  setError('');
  setUploadedFileName(file.name);

  try {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${API_BASE_URL}/api/slides/upload`, {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
      throw new Error('Failed to upload document');
    }

    const data = await response.json();
    const extractedText = data.sections
      .map(section => section.content)
      .join('\n\n');
    
    setUploadedContent(extractedText);
    setError('');
  } catch (err) {
    setError('Error: ' + err.message);
    setUploadedFileName('');
    setUploadedContent('');
  } finally {
    setLoading(false);
  }
};
Features Preserved:

File type validation
FormData handling for multipart requests
Error recovery (clearing states on failure)
Filename display feedback
Step 5: Merge Flashcard Generation
Implementation with Enhanced Error Handling:

JavaScript
const generateFlashcards = async () => {
  const contentToUse = uploadedContent || studyMaterial;
  
  if (!contentToUse.trim()) {
    setError('Please upload a document or enter some study material');
    return;
  }

  setLoading(true);
  setLoadingType('flashcards');
  setError('');
  setFlashcardWarning('');
  setVisibleAnswers({});
  
  try {
    const response = await fetch(`${API_BASE_URL}/api/flashcards/generate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        studyMaterial: contentToUse,
        count: flashcardCount
      })
    });

    if (!response.ok) {
      throw new Error('Flashcards failed to generate');
    }

    const data = await response.json();
    
    // Handle multiple response formats
    const flashcardsData = data.flashcards || data.data || data;
    
    if (Array.isArray(flashcardsData)) {
      setFlashcards(flashcardsData);
      setFlashcardWarning(data.warning || '');
    } else {
      throw new Error('Invalid flashcard data format');
    }
    
    setError('');
  } catch (err) {
    console.error('Flashcard generation error:', err);
    setError(err.message);
    setFlashcards([]);
    setFlashcardWarning('');
  } finally {
    setLoading(false);
    setLoadingType('');
  }
};
Key Features:

Flexible response format handling (3 different API response patterns)
Proper loadingType tracking
Warning message extraction
Visible answers reset on generation
Console logging for debugging
Step 6: Merge Quiz Generation
Implementation with Score Calculation:

JavaScript
const generateQuiz = async () => {
  const contentToUse = uploadedContent || studyMaterial;
  
  if (!contentToUse.trim()) {
    setError('Please upload a document or enter some study material');
    return;
  }

  setLoading(true);
  setLoadingType('quiz');
  setError('');
  setQuizWarning('');
  setUserAnswers({});
  
  try {
    const response = await fetch(`${API_BASE_URL}/api/quiz/generate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        studyMaterial: contentToUse,
        count: quizCount, 
        difficulty: difficulty
      })
    });

    if (!response.ok) {
      throw new Error('Quiz failed to generate');
    }

    const data = await response.json();
    
    // Handle multiple response formats
    const quizData = data.questions || data.data || data;
    
    if (Array.isArray(quizData)) {
      setQuizzes(quizData);
      setQuizWarning(data.warning || '');
    } else {
      throw new Error('Invalid quiz data format');
    }
    
    setError('');
  } catch (err) {
    console.error('Quiz generation error:', err);
    setError(err.message);
    setQuizzes([]);
    setQuizWarning('');
  } finally {
    setLoading(false);
    setLoadingType('');
  }
};

const calculateScore = () => {
  let correct = 0;
  quizzes.forEach((question, index) => {
    if (userAnswers[index] === question.options[question.correctAnswer]) {
      correct++;
    }
  });
  return correct;
};
Advanced Features:

Real-time score calculation
Quiz parameter support (count, difficulty)
Proper user answer tracking
Multiple API response format support
User answers reset on generation
Step 7: Merge UI Components
Input Section Layout:

jsx
<div className="input-section">
  {/* File Upload from File 2 */}
  <div className="upload-section">
    <label htmlFor="file-upload" className="upload-button">
      📄 Upload PDF/PowerPoint
    </label>
    <input
      id="file-upload"
      type="file"
      accept=".pdf,.ppt,.pptx"
      onChange={handleFileUpload}
      style={{ display: 'none' }}
      disabled={loading}
    />
    {uploadedFileName && (
      <span className="uploaded-file-name">✓ {uploadedFileName}</span>
    )}
  </div>

  <div className="divider">OR</div>

  {/* Manual Input from File 2 */}
  <textarea
    placeholder="Enter your study material here..."
    value={studyMaterial}
    onChange={(e) => setStudyMaterial(e.target.value)}
    rows={8}
  />
  
  {/* Settings from File 1 */}
  <div className="settings-section">
    <div className="setting-group">
      <label htmlFor="flashcard-count">Flashcards:</label>
      <input
        id="flashcard-count"
        type="number"
        min="1"
        max="20"
        value={flashcardCount}
        onChange={(e) => setFlashcardCount(Math.min(20, Math.max(1, parseInt(e.target.value) || 1)))}
      />
    </div>
    
    <div className="setting-group">
      <label htmlFor="quiz-count">Quiz Questions:</label>
      <input
        id="quiz-count"
        type="number"
        min="1"
        max="20"
        value={quizCount}
        onChange={(e) => setQuizCount(Math.min(20, Math.max(1, parseInt(e.target.value) || 1)))}
      />
    </div>
    
    <div className="setting-group">
      <label htmlFor="difficulty">Difficulty:</label>
      <select
        id="difficulty"
        value={difficulty}
        onChange={(e) => setDifficulty(e.target.value)}
      >
        <option value="EASY">Easy</option>
        <option value="MEDIUM">Medium</option>
        <option value="HARD">Hard</option>
      </select>
    </div>
  </div>
  
  <div className="button-group">
    <button onClick={generateFlashcards} disabled={loading}>
      {loading && loadingType === 'flashcards' ? 'Generating...' : 'Generate Flashcards'}
    </button>
    <button onClick={generateQuiz} disabled={loading}>
      {loading && loadingType === 'quiz' ? 'Generating...' : 'Generate Quiz'}
    </button>
  </div>

  {error && <div className="error-message">{error}</div>}
</div>
Results Section - Flashcards Tab:

jsx
{activeTab === 'flashcards' && (
  <div className="flashcards-container">
    {flashcardWarning && (
      <div className="warning-message">⚠️ {flashcardWarning}</div>
    )}
    {loading && loadingType === 'flashcards' ? (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Generating flashcards...</p>
      </div>
    ) : flashcards.length === 0 ? (
      <p className="no-results">No flashcards generated yet</p>
    ) : (
      flashcards.map((card, index) => (
        <div key={index} className="flashcard">
          <div className="question">
            <strong>Q:</strong> {card.question}
          </div>
          <button 
            onClick={() => toggleAnswer(index)}
            className="toggle-answer-btn"
          >
            {visibleAnswers[index] ? 'Hide Answer' : 'Show Answer'}
          </button>
          {visibleAnswers[index] && (
            <div className="answer">
              <strong>A:</strong> {card.answer}
            </div>
          )}
        </div>
      ))
    )}
  </div>
)}
Results Section - Quiz Tab with Scoring:

jsx
{activeTab === 'quiz' && (
  <div className="quiz-container">
    {quizWarning && (
      <div className="warning-message">⚠️ {quizWarning}</div>
    )}
    {loading && loadingType === 'quiz' ? (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Generating quiz...</p>
      </div>
    ) : quizzes.length === 0 ? (
      <p className="no-results">No quiz generated yet</p>
    ) : (
      <>
        {/* Score Display with Progress Bar */}
        <div className="score-display">
          <h2>Score: {calculateScore()} / {quizzes.length}</h2>
          <div className="score-bar">
            <div 
              className="score-fill"
              style={{ 
                width: `${quizzes.length > 0 ? (calculateScore() / quizzes.length) * 100 : 0}%`,
                backgroundColor: quizzes.length > 0 && (calculateScore() / quizzes.length) >= 0.7 ? '#4CAF50' : 
                                 quizzes.length > 0 && (calculateScore() / quizzes.length) >= 0.5 ? '#FF9800' : '#f44336'
              }}
            ></div>
          </div>
          <p className="score-percentage">
            {quizzes.length > 0 ? Math.round((calculateScore() / quizzes.length) * 100) : 0}%
          </p>
        </div>

        {/* Quiz Questions with Feedback */}
        {quizzes.map((question, index) => (
          <div key={index} className="quiz-question">
            <h3>Question {index + 1}</h3>
            <p className="question-text">{question.question}</p>
            <div className="options">
              {question.options && question.options.map((option, optIndex) => {
                const isSelected = userAnswers[index] === option;
                const isCorrect = optIndex === question.correctAnswer;
                const showFeedback = userAnswers[index] !== undefined;
                
                return (
                  <div 
                    key={optIndex} 
                    className={`option ${isSelected && showFeedback ? (isCorrect ? 'correct' : 'incorrect') : ''}`}
                    onClick={() => handleAnswerSelect(index, option)}
                    style={{ cursor: 'pointer' }}
                  >
                    <input 
                      type="radio" 
                      name={`question-${index}`} 
                      id={`q${index}-opt${optIndex}`}
                      checked={isSelected}
                      onChange={() => {}}
                    />
                    <label htmlFor={`q${index}-opt${optIndex}`} style={{ cursor: 'pointer' }}>
                      {option}
                    </label>
                    {isSelected && showFeedback && (
                      <span className="feedback">
                        {isCorrect ? (
                          <>
                            <span className="feedback-text">Correct</span>
                            <span className="feedback-symbol">✓</span>
                          </>
                        ) : (
                          <>
                            <span className="feedback-text">Incorrect</span>
                            <span className="feedback-symbol">✗</span>
                          </>
                        )}
                      </span>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </>
    )}
  </div>
)}
Step 8: Merge CSS Styling
Combined Stylesheet Strategy:

Kept all base styling from both files
Added file upload styles from File 1 (.upload-section, .upload-button, .uploaded-file-name, .divider)
Kept all File 2 styling for quiz options, flashcards, and layout
Added File 1 advanced features (loading spinner, score display, reset button, settings section)
Key CSS Classes Maintained:

CSS
/* File Upload Styles from File 1 */
.upload-section { }
.upload-button { }
.uploaded-file-name { }
.divider { }

/* Settings from File 1 */
.settings-section { }
.setting-group { }

/* Loading and Feedback from File 1 */
.loading-container { }
.spinner { }
.score-display { }
.score-bar { }
.reset-btn-small { }
.warning-message { }

/* Base Layout (Both Files) */
.study-assistant { }
.input-section { }
.results-section { }
.tabs { }
.flashcards-container { }
.quiz-container { }

/* Display Components (File 2) */
.flashcard { }
.quiz-question { }
.option { }
Integration Points
Content Flow
Code
File Upload 
    ↓
Extract Text → Store in uploadedContent
    ↓
Generate Button (Flashcards/Quiz)
    ↓
Use uploadedContent OR studyMaterial
    ↓
API Request with proper parameters
    ↓
Parse Response (multiple formats supported)
    ↓
Display Results with Warnings
State Management
Code
Component Initialization
    ↓
User uploads file → setUploadedContent()
    ↓
User types text → setStudyMaterial()
    ↓
User adjusts settings → setFlashcardCount/setQuizCount/setDifficulty()
    ↓
User clicks Generate → setLoading() + setLoadingType()
    ↓
API returns → setFlashcards/setQuizzes + setWarning()
    ↓
User interacts → setVisibleAnswers/setUserAnswers
Testing Performed
Manual Testing Scenarios
Test 1: File Upload Integration

- Upload PDF file successfully
- Display filename with checkmark
- Extract text correctly
- Generate flashcards from uploaded content
- Generate quiz from uploaded content
Test 2: Manual Text Input

- Enter study material via textarea
- Generate flashcards from manual text
- Generate quiz from manual text
Test 3: Mixed Scenario

- Upload file → generate flashcards
- Clear file → enter manual text → generate quiz
- Uploaded content takes priority when both present
Test 4: Advanced Quiz Features

- Score calculation updates in real-time
- Answer feedback shows correct/incorrect
- Score bar color changes (green ≥70%, orange ≥50%, red <50%)
- Progress percentage displays accurately
Test 5: Flashcard Features

- Toggle answer visibility on/off
-  Multiple cards work independently
- Clear answers when generating new set
Test 6: Settings Customization

- Adjust flashcard count (1-20)
- Adjust quiz count (1-20)
- Select difficulty level
- Settings apply to generation
Test 7: Warning Messages

- Display flashcard warnings when applicable
- Display quiz warnings when applicable
- Clear warnings on new generation
Test 8: Error Handling

- Show error if no content provided
- Show error if invalid file type
- Show error if upload fails
- Show error if API fails
Test 9: Loading States

- Show loading spinner while generating
- Disable buttons during loading
- Show correct content type being generated
- Clear loading state on completion
Test 10: Tab Navigation

- Switch between flashcards and quiz tabs
- Maintain data when switching tabs
- Reset buttons work from tab headers
Files Modified
Code
frontend/src/components/StudyAssistant.jsx   (Complete merge - ~350 lines)
frontend/src/components/StudyAssistant.css   (Complete merge - ~480 lines)
Component Features Summary
Input Capabilities
- File upload (PDF/PowerPoint)
- Manual text input
- Customizable flashcard count (1-20)
- Customizable quiz count (1-20)
- Difficulty selector (EASY/MEDIUM/HARD)

Flashcard Features
- Toggle answer visibility
- Warning messages for insufficient content
- Reset functionality
- Loading state feedback

Quiz Features
- Real-time score calculation
- Visual progress bar with color coding
- Answer selection with immediate feedback
- Correct/incorrect indicators (✓/✗)
- Warning messages for partial generation
- Reset functionality

User Experience
- Proper loading spinners
- Clear error messages
- File upload success feedback
- Tab-based content organization
- Responsive design
- Accessible form controls

Impact Assessment
Issues Resolved
- Single unified component for all study features
- File upload + text input both supported
- Advanced quiz scoring working correctly
- Flashcard toggling functioning
- Warning messages displaying
- Flexible API response format handling
- Proper loading state management

Code Quality Improvements
- Reduced code duplication
- Consistent error handling
- Better state organization
- Improved debugging with console logs
- Comprehensive null safety checks

User-Facing Improvements
- More flexible content input methods
- Better feedback during operations
- Clear visual score representation
- Helpful warning messages
- Smoother user experience

Future Enhancements
Potential Additions:

Drag-and-drop file upload
Copy/export functionality for flashcards
Quiz result history/analytics
Favorite flashcards marking
Study session timer
Performance statistics