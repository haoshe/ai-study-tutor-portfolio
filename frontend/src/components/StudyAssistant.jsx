import React, { useState } from 'react';
import './StudyAssistant.css';

const API_BASE_URL = ''; // Empty because we're using proxy

function StudyAssistant() {
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

  // File Upload Handler
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
      
      // Extract text from all sections and store in background
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

  // Generate Flashcards
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

  return (
    <div className="study-assistant">
      <h1>AI Study Assistant</h1>
      
      {/* Input Section */}
      <div className="input-section">
        {/* File Upload */}
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

        <textarea
          placeholder="Enter your study material here..."
          value={studyMaterial}
          onChange={(e) => setStudyMaterial(e.target.value)}
          rows={8}
        />
        
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
              onChange={(e) => setQuizCount(Math.0, Math.max(1, parseInt(e.target.value) || 1)))}
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
      </div>
    </div>
  );
}

export default StudyAssistant;