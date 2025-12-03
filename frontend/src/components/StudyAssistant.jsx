import React, { useState } from 'react';
import './StudyAssistant.css';

const API_BASE_URL = ''; // Empty because we're using proxy

function StudyAssistant({userId}) {
  // Study Material States
  const [studyMaterial, setStudyMaterial] = useState('');
  const [uploadedContent, setUploadedContent] = useState('');
  const [uploadedFileName, setUploadedFileName] = useState('');
  const [inputSource, setInputSource] = useState('text'); // 'file' or 'text'

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

  // History States
  const [flashcardHistory, setFlashcardHistory] = useState([]);
  const [quizHistory, setQuizHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);

  // helper function to get auth headers for login
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return token
    ? { 'Study-Auth': token }
    : {};  // No header if no token
};

  // Flashcard Functions
  const toggleAnswer = (index) => {
    setVisibleAnswers(prev => ({
      ...prev,
      [index]: !prev[index]
    }));
  };

  const resetFlashcards = () => {
    setFlashcards([]);
    setVisibleAnswers({});
    setFlashcardWarning('');
    setError('');
  };

  // Quiz Functions
  const calculateScore = () => {
    let correct = 0;
    quizzes.forEach((question, index) => {
      if (userAnswers[index] === question.options[question.correctAnswer]) {
        correct++;
      }
    });
    return correct;
  };

  const handleAnswerSelect = (questionIndex, selectedOption) => {
    setUserAnswers(prev => ({
      ...prev,
      [questionIndex]: selectedOption
    }));
  };

  const resetQuiz = () => {
    setQuizzes([]);
    setUserAnswers({});
    setQuizWarning('');
    setError('');
  };

  // File Upload Handler with size validation
  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB limit
    if (file.size > MAX_FILE_SIZE) {
      setError('File size exceeds 10MB limit. Please upload a smaller file.');
      setUploadedFileName('');
      setUploadedContent('');
      return;
    }

    const validTypes = ['application/pdf', 'application/vnd.ms-powerpoint', 
                        'application/vnd.openxmlformats-officedocument.presentationml.presentation'];
    if (!validTypes.includes(file.type)) {
      setError('Please upload a PDF or PowerPoint file');
      setUploadedFileName('');
      setUploadedContent('');
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
        throw new Error(`Failed to upload document (${response.status}). The file format may not be supported.`);
      }

      const data = await response.json();
      
      // Extract text from all sections and store in background
      const extractedText = data.sections
        .map(section => section.content)
        .join('\n\n');
      
      if (!extractedText.trim()) {
        throw new Error('Document appears to be empty or unreadable');
      }

      setUploadedContent(extractedText);
      setInputSource('file'); // Auto-switch to file input on successful upload
      setError('');
    } catch (err) {
      console.error('File upload error:', err);
      setError(`Upload failed: ${err.message}`);
      setUploadedFileName('');
      setUploadedContent('');
      setInputSource('text'); // Revert to text input on failure
    } finally {
      setLoading(false);
    }
  };

  // Handle text input change - auto-switch to text source
  const handleTextChange = (e) => {
    setStudyMaterial(e.target.value);
    if (e.target.value.trim() && inputSource === 'file' && error) {
      // If switching from file with error to text, offer to clear error
      setError('');
    }
  };

  // Generate Flashcards
  const generateFlashcards = async () => {
    // Get content based on selected source
    let contentToUse = '';
    let sourceLabel = '';

    if (inputSource === 'file') {
      contentToUse = uploadedContent;
      sourceLabel = uploadedFileName;
    } else {
      contentToUse = studyMaterial;
      sourceLabel = 'Text input';
    }
    
    if (!contentToUse.trim()) {
      setError(`Please provide study material via ${inputSource === 'file' ? 'file upload' : 'text input'}`);
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
           ...getAuthHeaders(),
        },
        body: JSON.stringify({
          studyMaterial: contentToUse,
          count: flashcardCount,
          userId: userId, 
          title: 'AI Generated Flashcards' 
        })
      });

      if (response.status === 401) {
        // Token expired or invalid
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.reload(); // Redirect to login
        return;
      }

      if (!response.ok) {
        throw new Error(`Flashcards failed to generate (${response.status}). The source material may be too short or unsupported.`);
      }

      const data = await response.json();
      
      // Handle both response formats
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

  // Generate Quiz
  const generateQuiz = async () => {
    // Get content based on selected source
    let contentToUse = '';
    let sourceLabel = '';

    if (inputSource === 'file') {
      contentToUse = uploadedContent;
      sourceLabel = uploadedFileName;
    } else {
      contentToUse = studyMaterial;
      sourceLabel = 'Text input';
    }
    
    if (!contentToUse.trim()) {
      setError(`Please provide study material via ${inputSource === 'file' ? 'file upload' : 'text input'}`);
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
           ...getAuthHeaders(),
        },
        body: JSON.stringify({
          studyMaterial: contentToUse,
          count: quizCount, 
          difficulty: difficulty,
          userId: userId,
          title: 'AI Generated Quiz'
        })
      });

       if (response.status === 401) {
        // Token expired or invalid
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.reload(); // Redirect to login
        return;
      }

      if (!response.ok) {
        throw new Error(`Quiz failed to generate (${response.status}). The source material may be too short or unsupported.`);
      }

      const data = await response.json();
      
      // Handle both response formats
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

  // Fetch Flashcard History
  const fetchFlashcardHistory = async () => {
    setHistoryLoading(true);
    setError('');

    try {
      const response = await fetch(`${API_BASE_URL}/api/flashcards/history`, {
        method: 'GET',
        headers: {
          ...getAuthHeaders(),
        }
      });

      if (response.status === 401) {
        // Token expired or invalid
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.reload(); // Redirect to login
        return;
      }

      if (!response.ok) {
        throw new Error(`Failed to fetch flashcard history (${response.status})`);
      }

      const data = await response.json();
      setFlashcardHistory(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Flashcard history fetch error:', err);
      setError(err.message);
      setFlashcardHistory([]);
    } finally {
      setHistoryLoading(false);
    }
  };

  // Fetch Quiz History
  const fetchQuizHistory = async () => {
    setHistoryLoading(true);
    setError('');

    try {
      const response = await fetch(`${API_BASE_URL}/api/quiz/history`, {
        method: 'GET',
        headers: {
          ...getAuthHeaders(),
        }
      });

      if (response.status === 401) {
        // Token expired or invalid
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.reload(); // Redirect to login
        return;
      }

      if (!response.ok) {
        throw new Error(`Failed to fetch quiz history (${response.status})`);
      }

      const data = await response.json();
      setQuizHistory(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Quiz history fetch error:', err);
      setError(err.message);
      setQuizHistory([]);
    } finally {
      setHistoryLoading(false);
    }
  };

  // View Flashcard Set from History
  const viewFlashcardSet = (flashcardSet) => {
    // Load flashcards from the saved set
    setFlashcards(flashcardSet.flashcards || []);
    setVisibleAnswers({});
    setFlashcardWarning('');
    // Switch to flashcards tab
    setActiveTab('flashcards');
  };

  // View Quiz Set from History
  const viewQuizSet = (quizSet) => {
    // Transform saved quiz format to match UI format
    const transformedQuestions = (quizSet.questions || []).map(q => {
      // Convert from database format (optionA/B/C/D, correctAnswer='A')
      // to UI format (options=[], correctAnswer=0)
      const options = [q.optionA, q.optionB, q.optionC, q.optionD];
      const correctAnswerIndex = ['A', 'B', 'C', 'D'].indexOf(q.correctAnswer);

      return {
        question: q.question,
        options: options,
        correctAnswer: correctAnswerIndex,
        explanation: q.explanation
      };
    });

    setQuizzes(transformedQuestions);
    setUserAnswers({});
    setQuizWarning('');
    // Switch to quiz tab
    setActiveTab('quiz');
  };

  // Delete Flashcard Set from History
  const deleteFlashcardSet = async (setId) => {
    if (!window.confirm('Are you sure you want to delete this flashcard set?')) {
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/api/flashcards/${setId}`, {
        method: 'DELETE',
        headers: {
          ...getAuthHeaders(),
        }
      });

      if (response.status === 401) {
        // Token expired or invalid
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.reload();
        return;
      }

      if (!response.ok) {
        throw new Error(`Failed to delete flashcard set (${response.status})`);
      }

      // Remove from local state (optimistic update)
      setFlashcardHistory(prev => prev.filter(set => set.id !== setId));
    } catch (err) {
      console.error('Flashcard delete error:', err);
      setError(err.message);
    }
  };

  // Delete Quiz Set from History
  const deleteQuizSet = async (setId) => {
    if (!window.confirm('Are you sure you want to delete this quiz set?')) {
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/api/quiz/${setId}`, {
        method: 'DELETE',
        headers: {
          ...getAuthHeaders(),
        }
      });

      if (response.status === 401) {
        // Token expired or invalid
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.reload();
        return;
      }

      if (!response.ok) {
        throw new Error(`Failed to delete quiz set (${response.status})`);
      }

      // Remove from local state (optimistic update)
      setQuizHistory(prev => prev.filter(set => set.id !== setId));
    } catch (err) {
      console.error('Quiz delete error:', err);
      setError(err.message);
    }
  };

  // Clear file upload
  const clearFileUpload = () => {
    setUploadedContent('');
    setUploadedFileName('');
    setInputSource('text');
    setError('');
    // Reset file input
    const fileInput = document.getElementById('file-upload');
    if (fileInput) fileInput.value = '';
  };

  return (
    <div className="study-assistant">
      <h1>AI Study Assistant</h1>
      
      {/* Input Section */}
      <div className="input-section">
        {/* Input Source Selector */}
        <div className="input-source-selector">
          <label>Select Study Material Source:</label>
          <div className="source-options">
            <label className="radio-label">
              <input
                type="radio"
                value="text"
                checked={inputSource === 'text'}
                onChange={(e) => {
                  setInputSource(e.target.value);
                  setError('');
                }}
              />
              Text Input
            </label>
              <label className="radio-label">
              <input
                type="radio"
                value="file"
                checked={inputSource === 'file'}
                onChange={(e) => setInputSource(e.target.value)}
              />
              File Upload {uploadedFileName && `(${uploadedFileName})`}
            </label>
          </div>
        </div>

        {/* File Upload Section */}
        {inputSource === 'file' ? (
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
              <div className="file-info">
                <span className="uploaded-file-name">✓ {uploadedFileName}</span>
                <button 
                  className="clear-file-btn"
                  onClick={clearFileUpload}
                  disabled={loading}
                >
                  Clear
                </button>
              </div>
            )}
            <p className="source-note">File size limit: 10MB. Supported: PDF, PPT, PPTX</p>
          </div>
        ) : (
          <textarea
            placeholder="Enter your study material here..."
            value={studyMaterial}
            onChange={handleTextChange}
            rows={8}
          />
        )}
        
        {/* Settings */}
        <div className="settings-section">
          <div className="setting-group">
            <label htmlFor="flashcard-count">Flashcards:</label>
            <input
              id="flashcard-count"
              type="number"
              min="1"
              max="10"
              value={flashcardCount}
              onChange={(e) => setFlashcardCount(Math.min(10, Math.max(1, parseInt(e.target.value) || 1)))}
            />
          </div>
          
          <div className="setting-group">
            <label htmlFor="quiz-count">Quiz Questions:</label>
            <input
              id="quiz-count"
              type="number"
              min="1"
              max="10"
              value={quizCount}
              onChange={(e) => setQuizCount(Math.min(10, Math.max(1, parseInt(e.target.value) || 1)))}
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

      {/* Results Section */}
      <div className="results-section">
        <div className="tabs">
          <button
            className={activeTab === 'flashcards' ? 'active' : ''}
            onClick={() => setActiveTab('flashcards')}
          >
            Flashcards ({flashcards.length})
            {flashcards.length > 0 && (
              <button
                className="reset-btn-small"
                onClick={(e) => {
                  e.stopPropagation();
                  resetFlashcards();
                }}
              >
                ×
              </button>
            )}
          </button>
          <button
            className={activeTab === 'quiz' ? 'active' : ''}
            onClick={() => setActiveTab('quiz')}
          >
            Quiz ({quizzes.length})
            {quizzes.length > 0 && (
              <button
                className="reset-btn-small"
                onClick={(e) => {
                  e.stopPropagation();
                  resetQuiz();
                }}
              >
                ×
              </button>
            )}
          </button>
          <button
            className={activeTab === 'history' ? 'active' : ''}
            onClick={() => {
              setActiveTab('history');
              fetchFlashcardHistory();
              fetchQuizHistory();
            }}
          >
            History
          </button>
        </div>

        {/* Flashcards Display */}
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
              <p className="no-results">
                {error && loadingType === 'flashcards' ? error : 'No flashcards generated yet'}
              </p>
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

        {/* Quiz Display */}
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
              <p className="no-results">
                {error && loadingType === 'quiz' ? error : 'No quiz generated yet'}
              </p>
            ) : (
              <>
                {/* Score Display */}
                <div className="score-display">
                  <h2>Score: {calculateScore()} / {quizzes.length}</h2>
                  <div className="score-bar">
                    <div 
                      className="score-fill"
                      style={{ 
                        width: `${quizzes.length > 0 ? (calculateScore() / quizzes.length) * 100 : 0}%`,
                        backgroundColor: quizzes.length > 0 && (calculateScore() / quizzes.length) >= 0.7 ? '#4CAF50' : quizzes.length > 0 && (calculateScore() / quizzes.length) >= 0.5 ? '#FF9800' : '#f44336'
                      }}
                    ></div>
                  </div>
                  <p className="score-percentage">
                    {quizzes.length > 0 ? Math.round((calculateScore() / quizzes.length) * 100) : 0}%
                  </p>
                </div>

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

        {/* History Display */}
        {activeTab === 'history' && (
          <div className="history-container">
            {historyLoading ? (
              <div className="loading-container">
                <div className="spinner"></div>
                <p>Loading history...</p>
              </div>
            ) : (
              <div className="history-content">
                <h2>Saved Flashcard Sets</h2>
                {flashcardHistory.length === 0 ? (
                  <p className="no-results">No saved flashcard sets yet</p>
                ) : (
                  <div className="history-list">
                    {flashcardHistory.map((set) => (
                      <div key={set.id} className="history-item">
                        <div className="history-item-info">
                          <h3>{set.title}</h3>
                          <p className="history-date">
                            Created: {new Date(set.createdAt).toLocaleString()}
                          </p>
                          <p className="history-count">{set.flashcards?.length || 0} flashcards</p>
                        </div>
                        <div className="history-item-actions">
                          <button className="view-btn" onClick={() => viewFlashcardSet(set)}>View</button>
                          <button className="delete-btn" onClick={() => deleteFlashcardSet(set.id)}>Delete</button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                <h2>Saved Quiz Sets</h2>
                {quizHistory.length === 0 ? (
                  <p className="no-results">No saved quiz sets yet</p>
                ) : (
                  <div className="history-list">
                    {quizHistory.map((set) => (
                      <div key={set.id} className="history-item">
                        <div className="history-item-info">
                          <h3>{set.title}</h3>
                          <p className="history-date">
                            Created: {new Date(set.createdAt).toLocaleString()}
                          </p>
                          <p className="history-count">
                            {set.questions?.length || 0} questions - {set.difficulty}
                          </p>
                        </div>
                        <div className="history-item-actions">
                          <button className="view-btn" onClick={() => viewQuizSet(set)}>View</button>
                          <button className="delete-btn" onClick={() => deleteQuizSet(set.id)}>Delete</button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default StudyAssistant;