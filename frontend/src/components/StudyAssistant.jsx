import React, { useState, useRef, useEffect } from 'react';
import './StudyAssistant.css';

const API_BASE_URL = '';

function StudyAssistant({ userId }) {
  // ============ SOURCES PANEL STATE ============
  const [sources, setSources] = useState([]);
  const [selectedSources, setSelectedSources] = useState(new Set());
  const [uploadingFile, setUploadingFile] = useState(false);

  // ============ STUDIO PANEL STATE ============
  const [flashcards, setFlashcards] = useState([]);
  const [quizzes, setQuizzes] = useState([]);
  const [flashcardCount, setFlashcardCount] = useState(5);
  const [quizCount, setQuizCount] = useState(5);
  const [difficulty, setDifficulty] = useState('MEDIUM');
  const [activeStudioTab, setActiveStudioTab] = useState('generate');
  const [flippedCards, setFlippedCards] = useState({});
  const [userAnswers, setUserAnswers] = useState({});
  const [quizSubmitted, setQuizSubmitted] = useState(false);
  const [flashcardHistory, setFlashcardHistory] = useState([]);
  const [quizHistory, setQuizHistory] = useState([]);

  // ============ CHAT PANEL STATE ============
  const [messages, setMessages] = useState([]);
  const [chatInput, setChatInput] = useState('');
  const [sessionId] = useState(() => `session-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`);
  const [isStreaming, setIsStreaming] = useState(false);

  // ============ UI STATE ============
  const [expandedPanel, setExpandedPanel] = useState(null);
  const [loading, setLoading] = useState({ flashcards: false, quiz: false, chat: false });
  const [error, setError] = useState('');

  // ============ REFS ============
  const fileInputRef = useRef(null);
  const chatEndRef = useRef(null);

  // ============ AUTH HELPER ============
  const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  };

  // ============ AUTO-SCROLL CHAT ============
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // ============ LOAD HISTORY ON MOUNT ============
  useEffect(() => {
    loadFlashcardHistory();
    loadQuizHistory();
  }, []);

  // ============ SOURCE MANAGEMENT ============
  const handleFileUpload = async (event) => {
    const files = event.target.files;
    if (!files.length) return;

    const MAX_FILE_SIZE = 10 * 1024 * 1024;
    const validTypes = [
      'application/pdf',
      'application/vnd.ms-powerpoint',
      'application/vnd.openxmlformats-officedocument.presentationml.presentation'
    ];

    setUploadingFile(true);
    setError('');

    for (const file of files) {
      if (file.size > MAX_FILE_SIZE) {
        setError(`File "${file.name}" exceeds 10MB limit`);
        continue;
      }

      if (!validTypes.includes(file.type)) {
        setError(`File "${file.name}" is not a supported format (PDF, PPT, PPTX)`);
        continue;
      }

      try {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`${API_BASE_URL}/api/slides/upload`, {
          method: 'POST',
          body: formData
        });

        if (!response.ok) {
          throw new Error(`Upload failed for ${file.name}`);
        }

        const data = await response.json();
        const extractedText = data.sections?.map(s => s.content).join('\n\n') || '';

        const newSource = {
          id: Date.now() + Math.random(),
          name: file.name,
          type: file.type.includes('pdf') ? 'pdf' : 'ppt',
          content: extractedText,
          uploadedAt: new Date()
        };

        setSources(prev => [...prev, newSource]);
        setSelectedSources(prev => new Set([...prev, newSource.id]));
      } catch (err) {
        setError(`Failed to upload ${file.name}: ${err.message}`);
      }
    }

    setUploadingFile(false);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const toggleSourceSelection = (sourceId) => {
    setSelectedSources(prev => {
      const newSet = new Set(prev);
      if (newSet.has(sourceId)) {
        newSet.delete(sourceId);
      } else {
        newSet.add(sourceId);
      }
      return newSet;
    });
  };

  const selectAllSources = () => {
    if (selectedSources.size === sources.length) {
      setSelectedSources(new Set());
    } else {
      setSelectedSources(new Set(sources.map(s => s.id)));
    }
  };

  const deleteSource = (sourceId) => {
    setSources(prev => prev.filter(s => s.id !== sourceId));
    setSelectedSources(prev => {
      const newSet = new Set(prev);
      newSet.delete(sourceId);
      return newSet;
    });
  };

  const getSelectedContent = () => {
    return sources
      .filter(s => selectedSources.has(s.id))
      .map(s => s.content)
      .join('\n\n');
  };

  // ============ FLASHCARD FUNCTIONS ============
  const generateFlashcards = async () => {
    const content = getSelectedContent();
    if (!content.trim()) {
      setError('Please select at least one source to generate flashcards');
      return;
    }

    // Switch to flashcards tab immediately and show loading there
    setActiveStudioTab('flashcards');
    setLoading(prev => ({ ...prev, flashcards: true }));
    setError('');
    setFlashcards([]);
    setFlippedCards({});

    try {
      const response = await fetch(`${API_BASE_URL}/api/flashcards/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeaders()
        },
        body: JSON.stringify({
          studyMaterial: content,
          count: flashcardCount,
          userId,
          title: 'AI Generated Flashcards'
        })
      });

      if (response.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.reload();
        return;
      }

      if (!response.ok) throw new Error('Failed to generate flashcards');

      const data = await response.json();
      const flashcardsData = data.flashcards || data.data || data;

      if (Array.isArray(flashcardsData)) {
        setFlashcards(flashcardsData);
        loadFlashcardHistory();
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(prev => ({ ...prev, flashcards: false }));
    }
  };

  const loadFlashcardHistory = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/flashcards/history`, {
        headers: getAuthHeaders()
      });
      if (response.ok) {
        const data = await response.json();
        setFlashcardHistory(data);
      }
    } catch (err) {
      console.error('Failed to load flashcard history:', err);
    }
  };

  const loadFlashcardSet = (set) => {
    const cards = set.flashcards.map(f => ({
      question: f.question,
      answer: f.answer
    }));
    setFlashcards(cards);
    setFlippedCards({});
    setActiveStudioTab('flashcards');
  };

  const deleteFlashcardSet = async (id) => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/flashcards/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      });
      if (response.ok) {
        loadFlashcardHistory();
      }
    } catch (err) {
      console.error('Failed to delete flashcard set:', err);
    }
  };

  const toggleCardFlip = (index) => {
    setFlippedCards(prev => ({ ...prev, [index]: !prev[index] }));
  };

  const resetFlashcards = () => {
    setFlashcards([]);
    setFlippedCards({});
    setActiveStudioTab('generate');
  };

  // ============ QUIZ FUNCTIONS ============
  const generateQuiz = async () => {
    const content = getSelectedContent();
    if (!content.trim()) {
      setError('Please select at least one source to generate quiz');
      return;
    }

    // Switch to quiz tab immediately and show loading there
    setActiveStudioTab('quiz');
    setLoading(prev => ({ ...prev, quiz: true }));
    setError('');
    setQuizzes([]);
    setUserAnswers({});
    setQuizSubmitted(false);

    try {
      const response = await fetch(`${API_BASE_URL}/api/quiz/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeaders()
        },
        body: JSON.stringify({
          studyMaterial: content,
          count: quizCount,
          difficulty: difficulty.toLowerCase(),
          userId,
          title: 'AI Generated Quiz'
        })
      });

      if (response.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.reload();
        return;
      }

      if (!response.ok) throw new Error('Failed to generate quiz');

      const data = await response.json();
      if (Array.isArray(data)) {
        setQuizzes(data);
        loadQuizHistory();
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(prev => ({ ...prev, quiz: false }));
    }
  };

  const loadQuizHistory = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/quiz/history`, {
        headers: getAuthHeaders()
      });
      if (response.ok) {
        const data = await response.json();
        setQuizHistory(data);
      }
    } catch (err) {
      console.error('Failed to load quiz history:', err);
    }
  };

  const loadQuizSet = (set) => {
    const questions = set.questions.map(q => ({
      question: q.question,
      options: [q.optionA, q.optionB, q.optionC, q.optionD],
      correctAnswer: q.correctAnswer,
      explanation: q.explanation
    }));
    setQuizzes(questions);
    setUserAnswers({});
    setQuizSubmitted(false);
    setActiveStudioTab('quiz');
  };

  const deleteQuizSet = async (id) => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/quiz/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      });
      if (response.ok) {
        loadQuizHistory();
      }
    } catch (err) {
      console.error('Failed to delete quiz set:', err);
    }
  };

  const handleAnswerSelect = (questionIndex, optionIndex) => {
    if (quizSubmitted) return; // Don't allow changes after submission
    setUserAnswers(prev => ({ ...prev, [questionIndex]: optionIndex }));
  };

  const submitQuiz = () => {
    setQuizSubmitted(true);
  };

  const calculateScore = () => {
    let correct = 0;
    quizzes.forEach((q, i) => {
      if (userAnswers[i] === q.correctAnswer) correct++;
    });
    return correct;
  };

  const resetQuiz = () => {
    setQuizzes([]);
    setUserAnswers({});
    setQuizSubmitted(false);
    setActiveStudioTab('generate');
  };

  const retakeQuiz = () => {
    setUserAnswers({});
    setQuizSubmitted(false);
  };

  // ============ CHAT FUNCTIONS ============
  const sendMessage = async () => {
    if (!chatInput.trim() || isStreaming) return;

    const userMessage = chatInput.trim();
    setChatInput('');
    setMessages(prev => [...prev, { role: 'user', content: userMessage }]);
    setIsStreaming(true);

    // Add context from selected sources
    const sourceContext = getSelectedContent();
    const contextPrefix = sourceContext
      ? `Context from study materials:\n${sourceContext.substring(0, 3000)}...\n\nUser question: `
      : '';

    try {
      const response = await fetch(`${API_BASE_URL}/api/chat/conversation`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeaders()
        },
        body: JSON.stringify({
          sessionId,
          message: contextPrefix + userMessage
        })
      });

      if (!response.ok) throw new Error('Chat failed');

      const data = await response.json();
      setMessages(prev => [...prev, { role: 'assistant', content: data.response }]);
    } catch (err) {
      setMessages(prev => [...prev, { role: 'assistant', content: 'Sorry, I encountered an error. Please try again.' }]);
    } finally {
      setIsStreaming(false);
    }
  };

  const clearChat = async () => {
    try {
      await fetch(`${API_BASE_URL}/api/chat/conversation/${sessionId}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      });
      setMessages([]);
    } catch (err) {
      console.error('Failed to clear chat:', err);
    }
  };

  // ============ PANEL EXPANSION ============
  const togglePanelExpand = (panel) => {
    setExpandedPanel(expandedPanel === panel ? null : panel);
  };

  // ============ RENDER ============
  return (
    <div className={`study-assistant-container ${expandedPanel ? `expanded-${expandedPanel}` : ''}`}>
      {/* ============ LEFT PANEL: SOURCES ============ */}
      <div className={`panel sources-panel ${expandedPanel === 'sources' ? 'expanded' : ''} ${expandedPanel && expandedPanel !== 'sources' ? 'hidden' : ''}`}>
        <div className="panel-header">
          <h2>Sources</h2>
          <button className="expand-btn" onClick={() => togglePanelExpand('sources')} title={expandedPanel === 'sources' ? 'Collapse' : 'Expand'}>
            {expandedPanel === 'sources' ? '⊟' : '⊞'}
          </button>
        </div>

        <div className="panel-content">
          <button className="add-sources-btn" onClick={() => fileInputRef.current?.click()} disabled={uploadingFile}>
            <span className="btn-icon">+</span>
            {uploadingFile ? 'Uploading...' : 'Add sources'}
          </button>
          <input
            ref={fileInputRef}
            type="file"
            accept=".pdf,.ppt,.pptx"
            onChange={handleFileUpload}
            style={{ display: 'none' }}
            multiple
          />

          <p className="source-hint">Upload PDF or PowerPoint files</p>

          {sources.length > 0 && (
            <div className="select-all-row" onClick={selectAllSources}>
              <input
                type="checkbox"
                checked={selectedSources.size === sources.length && sources.length > 0}
                onChange={() => {}}
              />
              <span>Select all sources</span>
            </div>
          )}

          <div className="sources-list">
            {sources.map(source => (
              <div key={source.id} className={`source-item ${selectedSources.has(source.id) ? 'selected' : ''}`}>
                <div className="source-checkbox" onClick={() => toggleSourceSelection(source.id)}>
                  <input
                    type="checkbox"
                    checked={selectedSources.has(source.id)}
                    onChange={() => {}}
                  />
                </div>
                <div className="source-icon">
                  {source.type === 'pdf' ? '📄' : '📊'}
                </div>
                <div className="source-info">
                  <span className="source-name">{source.name}</span>
                </div>
                <button className="delete-source-btn" onClick={() => deleteSource(source.id)} title="Remove source">
                  ×
                </button>
              </div>
            ))}

            {sources.length === 0 && (
              <div className="no-sources">
                <p>No sources added yet</p>
                <p className="hint">Upload documents to get started</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ============ CENTER PANEL: STUDIO ============ */}
      <div className={`panel studio-panel ${expandedPanel === 'studio' ? 'expanded' : ''} ${expandedPanel && expandedPanel !== 'studio' ? 'hidden' : ''}`}>
        <div className="panel-header">
          <h2>Studio</h2>
          <button className="expand-btn" onClick={() => togglePanelExpand('studio')} title={expandedPanel === 'studio' ? 'Collapse' : 'Expand'}>
            {expandedPanel === 'studio' ? '⊟' : '⊞'}
          </button>
        </div>

        <div className="panel-content">
          <div className="studio-tabs">
            <button className={activeStudioTab === 'generate' ? 'active' : ''} onClick={() => setActiveStudioTab('generate')}>
              Generate
            </button>
            <button className={activeStudioTab === 'flashcards' ? 'active' : ''} onClick={() => setActiveStudioTab('flashcards')}>
              Flashcards {flashcards.length > 0 && `(${flashcards.length})`}
            </button>
            <button className={activeStudioTab === 'quiz' ? 'active' : ''} onClick={() => setActiveStudioTab('quiz')}>
              Quiz {quizzes.length > 0 && `(${quizzes.length})`}
            </button>
            <button className={activeStudioTab === 'history' ? 'active' : ''} onClick={() => setActiveStudioTab('history')}>
              History
            </button>
          </div>

          {error && <div className="error-banner">{error}<button className="error-close" onClick={() => setError('')}>×</button></div>}

          {/* Generate Tab */}
          {activeStudioTab === 'generate' && (
            <div className="generate-tab">
              <div className="generate-grid">
                <div className="generate-card">
                  <div className="card-icon">🎴</div>
                  <h3>Flashcards</h3>
                  <p>Generate study flashcards from your sources</p>
                  <div className="card-settings">
                    <label>Count:</label>
                    <input
                      type="number"
                      min="1"
                      max="20"
                      value={flashcardCount}
                      onChange={(e) => setFlashcardCount(Math.min(20, Math.max(1, parseInt(e.target.value) || 1)))}
                    />
                  </div>
                  <button className="generate-btn" onClick={generateFlashcards} disabled={loading.flashcards}>
                    {loading.flashcards ? 'Generating...' : 'Generate Flashcards'}
                  </button>
                </div>

                <div className="generate-card">
                  <div className="card-icon">📝</div>
                  <h3>Quiz</h3>
                  <p>Create multiple choice questions to test knowledge</p>
                  <div className="card-settings">
                    <label>Count:</label>
                    <input
                      type="number"
                      min="1"
                      max="20"
                      value={quizCount}
                      onChange={(e) => setQuizCount(Math.min(20, Math.max(1, parseInt(e.target.value) || 1)))}
                    />
                  </div>
                  <div className="card-settings">
                    <label>Difficulty:</label>
                    <select
                      value={difficulty}
                      onChange={(e) => setDifficulty(e.target.value)}
                    >
                      <option value="EASY">Easy</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="HARD">Hard</option>
                    </select>
                  </div>
                  <button className="generate-btn" onClick={generateQuiz} disabled={loading.quiz}>
                    {loading.quiz ? 'Generating...' : 'Generate Quiz'}
                  </button>
                </div>
              </div>

              <div className="selected-sources-info">
                <span className="info-icon">ℹ️</span>
                <span>{selectedSources.size} source{selectedSources.size !== 1 ? 's' : ''} selected</span>
                {selectedSources.size === 0 && <span className="warning-text"> - Please select sources first</span>}
              </div>
            </div>
          )}

          {/* Flashcards Tab */}
          {activeStudioTab === 'flashcards' && (
            <div className="flashcards-tab">
              {loading.flashcards ? (
                <div className="loading-state">
                  <div className="loading-spinner"></div>
                  <p>Generating flashcards...</p>
                </div>
              ) : flashcards.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-icon">🎴</div>
                  <p>No flashcards generated yet</p>
                  <button onClick={() => setActiveStudioTab('generate')}>Generate Flashcards</button>
                </div>
              ) : (
                <>
                  <div className="flashcards-header">
                    <span>{flashcards.length} Flashcards</span>
                    <button className="new-set-btn" onClick={resetFlashcards}>+ New Set</button>
                  </div>
                  <div className="flashcards-grid">
                    {flashcards.map((card, index) => (
                      <div 
                        key={index} 
                        className={`flashcard-flip ${flippedCards[index] ? 'flipped' : ''}`}
                        onClick={() => toggleCardFlip(index)}
                      >
                        <div className="flashcard-inner">
                          <div className="flashcard-front">
                            <span className="card-label">Question {index + 1}</span>
                            <p>{card.question}</p>
                            <span className="flip-hint">Click to flip</span>
                          </div>
                          <div className="flashcard-back">
                            <span className="card-label">Answer</span>
                            <p>{card.answer}</p>
                            <span className="flip-hint">Click to flip back</span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </>
              )}
            </div>
          )}

          {/* Quiz Tab */}
          {activeStudioTab === 'quiz' && (
            <div className="quiz-tab">
              {loading.quiz ? (
                <div className="loading-state">
                  <div className="loading-spinner"></div>
                  <p>Generating quiz...</p>
                </div>
              ) : quizzes.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-icon">📝</div>
                  <p>No quiz generated yet</p>
                  <button onClick={() => setActiveStudioTab('generate')}>Generate Quiz</button>
                </div>
              ) : (
                <>
                  {quizSubmitted && (
                    <div className="quiz-results">
                      <div className="results-header">
                        <h3>Quiz Results</h3>
                        <div className="score-display">
                          <span className="score-num">{calculateScore()}</span>
                          <span className="score-denom">/ {quizzes.length}</span>
                        </div>
                        <div className="score-percentage">
                          {Math.round((calculateScore() / quizzes.length) * 100)}%
                        </div>
                        <div className="score-bar">
                          <div 
                            className="score-fill"
                            style={{
                              width: `${(calculateScore() / quizzes.length) * 100}%`,
                              backgroundColor: calculateScore() / quizzes.length >= 0.7 ? '#22c55e' : calculateScore() / quizzes.length >= 0.5 ? '#f59e0b' : '#ef4444'
                            }}
                          />
                        </div>
                      </div>
                      <div className="results-actions">
                        <button className="retake-btn" onClick={retakeQuiz}>Retake Quiz</button>
                        <button className="new-quiz-btn" onClick={resetQuiz}>New Quiz</button>
                      </div>
                    </div>
                  )}

                  <div className="quiz-header">
                    <span>{quizzes.length} Questions</span>
                    {!quizSubmitted && <button className="new-set-btn" onClick={resetQuiz}>+ New Quiz</button>}
                  </div>

                  <div className="quiz-questions">
                    {quizzes.map((q, qIndex) => (
                      <div key={qIndex} className={`quiz-question-item ${quizSubmitted ? 'submitted' : ''}`}>
                        <h4>Question {qIndex + 1}</h4>
                        <p className="question-text">{q.question}</p>
                        <div className="options-list">
                          {q.options.map((option, oIndex) => {
                            const isSelected = userAnswers[qIndex] === oIndex;
                            const isCorrect = oIndex === q.correctAnswer;
                            const showResult = quizSubmitted;

                            let optionClass = 'option-item';
                            if (isSelected && !showResult) optionClass += ' selected';
                            if (showResult && isCorrect) optionClass += ' correct';
                            if (showResult && isSelected && !isCorrect) optionClass += ' incorrect';

                            return (
                              <div
                                key={oIndex}
                                className={optionClass}
                                onClick={() => handleAnswerSelect(qIndex, oIndex)}
                              >
                                <span className="option-letter">{String.fromCharCode(65 + oIndex)}</span>
                                <span className="option-text">{option}</span>
                                {showResult && isSelected && (
                                  <span className="result-icon">{isCorrect ? '✓' : '✗'}</span>
                                )}
                                {showResult && isCorrect && !isSelected && (
                                  <span className="result-icon correct-answer">✓</span>
                                )}
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    ))}
                  </div>

                  {!quizSubmitted && (
                    <div className="quiz-submit-section">
                      <p className="answered-count">
                        {Object.keys(userAnswers).length} of {quizzes.length} questions answered
                      </p>
                      <button 
                        className="submit-quiz-btn" 
                        onClick={submitQuiz}
                        disabled={Object.keys(userAnswers).length < quizzes.length}
                      >
                        Submit Quiz
                      </button>
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          {/* History Tab */}
          {activeStudioTab === 'history' && (
            <div className="history-tab">
              <div className="history-section">
                <h3>Flashcard Sets</h3>
                {flashcardHistory.length === 0 ? (
                  <p className="no-history">No saved flashcard sets</p>
                ) : (
                  <div className="history-list">
                    {flashcardHistory.map(set => (
                      <div key={set.id} className="history-item">
                        <div className="history-info" onClick={() => loadFlashcardSet(set)}>
                          <span className="history-icon">🎴</span>
                          <div>
                            <span className="history-title">{set.title}</span>
                            <span className="history-meta">{set.flashcards?.length || 0} cards • {new Date(set.createdAt).toLocaleDateString()}</span>
                          </div>
                        </div>
                        <button className="history-delete" onClick={() => deleteFlashcardSet(set.id)}>×</button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="history-section">
                <h3>Quiz Sets</h3>
                {quizHistory.length === 0 ? (
                  <p className="no-history">No saved quiz sets</p>
                ) : (
                  <div className="history-list">
                    {quizHistory.map(set => (
                      <div key={set.id} className="history-item">
                        <div className="history-info" onClick={() => loadQuizSet(set)}>
                          <span className="history-icon">📝</span>
                          <div>
                            <span className="history-title">{set.title}</span>
                            <span className="history-meta">{set.questions?.length || 0} questions • {set.difficulty} • {new Date(set.createdAt).toLocaleDateString()}</span>
                          </div>
                        </div>
                        <button className="history-delete" onClick={() => deleteQuizSet(set.id)}>×</button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* ============ RIGHT PANEL: CHAT ============ */}
      <div className={`panel chat-panel ${expandedPanel === 'chat' ? 'expanded' : ''} ${expandedPanel && expandedPanel !== 'chat' ? 'hidden' : ''}`}>
        <div className="panel-header">
          <h2>Chat</h2>
          <div className="chat-header-actions">
            <button className="clear-chat-btn" onClick={clearChat} title="Clear conversation">
              🗑️
            </button>
            <button className="expand-btn" onClick={() => togglePanelExpand('chat')} title={expandedPanel === 'chat' ? 'Collapse' : 'Expand'}>
              {expandedPanel === 'chat' ? '⊟' : '⊞'}
            </button>
          </div>
        </div>

        <div className="panel-content chat-content">
          <div className="messages-container">
            {messages.length === 0 && (
              <div className="chat-welcome">
                <div className="welcome-icon">💬</div>
                <h3>AI Study Assistant</h3>
                <p>Ask questions about your study materials</p>
                <div className="suggested-prompts">
                  <button onClick={() => setChatInput('Summarize the key concepts')}>
                    Summarize key concepts
                  </button>
                  <button onClick={() => setChatInput('Explain the main topics')}>
                    Explain main topics
                  </button>
                  <button onClick={() => setChatInput('What should I focus on?')}>
                    Study recommendations
                  </button>
                </div>
              </div>
            )}

            {messages.map((msg, index) => (
              <div key={index} className={`message ${msg.role}`}>
                <div className="message-avatar">
                  {msg.role === 'user' ? '👤' : '🤖'}
                </div>
                <div className="message-content">
                  {msg.content}
                </div>
              </div>
            ))}

            {isStreaming && (
              <div className="message assistant">
                <div className="message-avatar">🤖</div>
                <div className="message-content typing">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            )}

            <div ref={chatEndRef} />
          </div>

          <div className="chat-input-container">
            <div className="sources-context-badge">
              {selectedSources.size} source{selectedSources.size !== 1 ? 's' : ''} selected
            </div>
            <div className="chat-input-wrapper">
              <input
                type="text"
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                placeholder="Start typing..."
                disabled={isStreaming}
              />
              <button className="send-btn" onClick={sendMessage} disabled={isStreaming || !chatInput.trim()}>
                ➤
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default StudyAssistant;