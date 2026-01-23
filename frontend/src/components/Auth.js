import React, { useState } from 'react';
import './Auth.css';

function Auth({ onLoginSuccess }) {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    rememberMe: false
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState({ score: 0, feedback: [] });

  // Industry-standard password validation
  const validatePassword = (password) => {
    const feedback = [];
    let score = 0;

    if (password.length >= 8) {
      score++;
    } else {
      feedback.push('At least 8 characters');
    }

    if (password.length >= 12) {
      score++;
    }

    if (/[A-Z]/.test(password)) {
      score++;
    } else {
      feedback.push('One uppercase letter');
    }

    if (/[a-z]/.test(password)) {
      score++;
    } else {
      feedback.push('One lowercase letter');
    }

    if (/[0-9]/.test(password)) {
      score++;
    } else {
      feedback.push('One number');
    }

    if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
      score++;
    } else {
      feedback.push('One special character (!@#$%^&*...)');
    }

    return { score, feedback, isValid: feedback.length === 0 };
  };

  const getStrengthLabel = (score) => {
    if (score <= 2) return { label: 'Weak', className: 'weak' };
    if (score <= 4) return { label: 'Medium', className: 'medium' };
    return { label: 'Strong', className: 'strong' };
  };

  const handleChange = (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setFormData({
      ...formData,
      [e.target.name]: value
    });
    setError('');

    // Update password strength indicator for registration
    if (e.target.name === 'password' && !isLogin) {
      setPasswordStrength(validatePassword(value));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (!formData.username || !formData.password) {
      setError('Username and password are required');
      setLoading(false);
      return;
    }

    if (!isLogin && !formData.email) {
      setError('Email is required for registration');
      setLoading(false);
      return;
    }

    // Industry-standard password validation for registration
    if (!isLogin) {
      const validation = validatePassword(formData.password);
      if (!validation.isValid) {
        setError(`Password requirements: ${validation.feedback.join(', ')}`);
        setLoading(false);
        return;
      }
    }

    // try {
    //   const endpoint = isLogin ? '/api/auth/login' : '/api/auth/register';
    //   const payload = isLogin 
    //     ? { username: formData.username, password: formData.password, rememberMe: formData.rememberMe }
    //     : { username: formData.username, email: formData.email, password: formData.password };

    try {
      const API_URL = "https://ai-study-tutor-9vvp.onrender.com";       
      const endpoint = isLogin 
        ? `${API_URL}/api/auth/login` 
        : `${API_URL}/api/auth/register`;    
      const payload = isLogin 
        ? { username: formData.username, password: formData.password, rememberMe: formData.rememberMe }
        : { username: formData.username, email: formData.email, password: formData.password };

      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload)
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Authentication failed');
      }

      if (formData.rememberMe) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify({
          id: data.id,
          username: data.username,
          email: data.email
        }));
      } else {
        sessionStorage.setItem('token', data.token);
        sessionStorage.setItem('user', JSON.stringify({
          id: data.id,
          username: data.username,
          email: data.email
        }));
      }

      onLoginSuccess(data);

    } catch (err) {
      setError(err.message || 'An error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const toggleMode = () => {
    setIsLogin(!isLogin);
    setError('');
    setPasswordStrength({ score: 0, feedback: [] });
    setFormData({ username: '', email: '', password: '', rememberMe: false });
  };

  const strengthInfo = getStrengthLabel(passwordStrength.score);

  return (
    <div className="auth-container">
      <div className="auth-background">
        <div className="bg-shape shape-1"></div>
        <div className="bg-shape shape-2"></div>
        <div className="bg-shape shape-3"></div>
      </div>
      
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-logo">
            <span className="logo-icon">📚</span>
          </div>
          <h1>Study Assistant</h1>
          <p>{isLogin ? 'Welcome back! Sign in to continue' : 'Create your account to get started'}</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="Enter your username"
              disabled={loading}
              autoComplete="username"
            />
          </div>

          {!isLogin && (
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="Enter your email"
                disabled={loading}
                autoComplete="email"
              />
            </div>
          )}

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Enter your password"
              disabled={loading}
              autoComplete={isLogin ? "current-password" : "new-password"}
            />
            {!isLogin && (
              <>
                <div className="password-requirements">
                  <p className="requirements-title">Password must contain:</p>
                  <ul className="requirements-list">
                    <li className={formData.password.length >= 8 ? 'met' : ''}>
                      <span className="req-icon">{formData.password.length >= 8 ? '✓' : '○'}</span>
                      At least 8 characters
                    </li>
                    <li className={/[A-Z]/.test(formData.password) ? 'met' : ''}>
                      <span className="req-icon">{/[A-Z]/.test(formData.password) ? '✓' : '○'}</span>
                      One uppercase letter
                    </li>
                    <li className={/[a-z]/.test(formData.password) ? 'met' : ''}>
                      <span className="req-icon">{/[a-z]/.test(formData.password) ? '✓' : '○'}</span>
                      One lowercase letter
                    </li>
                    <li className={/[0-9]/.test(formData.password) ? 'met' : ''}>
                      <span className="req-icon">{/[0-9]/.test(formData.password) ? '✓' : '○'}</span>
                      One number
                    </li>
                    <li className={/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(formData.password) ? 'met' : ''}>
                      <span className="req-icon">{/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(formData.password) ? '✓' : '○'}</span>
                      One special character
                    </li>
                  </ul>
                </div>
                {formData.password && (
                  <div className="password-strength">
                    <div className="strength-bar">
                      <div 
                        className={`strength-fill ${strengthInfo.className}`}
                        style={{ width: `${(passwordStrength.score / 6) * 100}%` }}
                      />
                    </div>
                    <span className={`strength-label ${strengthInfo.className}`}>
                      {strengthInfo.label}
                    </span>
                  </div>
                )}
              </>
            )}
          </div>

          {isLogin && (
            <div className="form-group checkbox-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="rememberMe"
                  checked={formData.rememberMe}
                  onChange={handleChange}
                  disabled={loading}
                />
                <span className="checkbox-custom"></span>
                <span>Remember me</span>
              </label>
            </div>
          )}

          {error && (
            <div className="auth-error">
              <span className="error-icon">⚠</span>
              {error}
            </div>
          )}

          <button 
            type="submit"
            className="auth-submit-btn"
            disabled={loading}
          >
            {loading ? (
              <>
                <span className="btn-spinner"></span>
                Processing...
              </>
            ) : (
              isLogin ? 'Sign In' : 'Create Account'
            )}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            {isLogin ? "Don't have an account?" : 'Already have an account?'}
            <button 
              type="button"
              onClick={toggleMode}
              className="toggle-btn"
              disabled={loading}
            >
              {isLogin ? 'Sign Up' : 'Sign In'}
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Auth;
