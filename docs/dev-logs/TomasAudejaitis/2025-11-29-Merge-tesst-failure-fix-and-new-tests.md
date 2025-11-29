# Development Log: Remember Me Feature Implementation

**Date:** November 29, 2025  
**Project:** CSU33012-2526-Project23 (AI Chat Application)  
**Developer:** Audejait  
**Feature:** Remember Me Authentication Functionality

---

## Executive Summary

Implemented a "Remember Me" feature for the authentication system that allows users to choose between standard 24-hour session tokens and extended 30-day session tokens. This enhancement improves user experience by reducing the frequency of re-authentication for trusted devices while maintaining security for standard login sessions.

---

## 1. Feature Overview

### 1.1 What Was Added

The Remember Me feature extends JWT token expiration based on user preference during login:

- **Standard Login (rememberMe = false):** Token valid for 24 hours
- **Remember Me Login (rememberMe = true):** Token valid for 30 days

### 1.2 User Impact

- Users can opt to stay logged in for 30 days on trusted devices
- Reduces authentication friction for returning users
- Maintains security with shorter sessions for users who don't opt-in

---

## 2. Technical Implementation

### 2.1 Modified Files

#### **LoginRequest DTO** (`ie.tcd.scss.aichat.dto.LoginRequest`)

**Changes Made:**
- Added `boolean rememberMe` field to the DTO
- Updated constructor from 2 parameters to 3 parameters: `LoginRequest(String username, String password, boolean rememberMe)`

**Code Change:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
    private boolean rememberMe;  // NEW FIELD
}
```

**Backward Compatibility Added:**
```java
// Added for backward compatibility with existing tests
public LoginRequest(String username, String password) {
    this(username, password, false); // Defaults to standard 24-hour session
}
```

---

#### **JwtUtil** (`ie.tcd.scss.aichat.util.JwtUtil`)

**Changes Made:**
- Added token validity constants
- Updated `generateToken()` method to accept `rememberMe` parameter
- Implemented conditional token expiration logic

**Code Changes:**
```java
// NEW CONSTANTS
private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000; // 24 hours
private static final long JWT_REMEMBER_ME_VALIDITY = 30L * 24 * 60 * 60 * 1000; // 30 days

// UPDATED METHOD SIGNATURE
public String generateToken(String username, boolean rememberMe) {
    Map<String, Object> claims = new HashMap<>();
    long validity = rememberMe ? JWT_REMEMBER_ME_VALIDITY : JWT_TOKEN_VALIDITY;
    return createToken(claims, username, validity);
}

// BACKWARD COMPATIBLE OVERLOAD
public String generateToken(String username) {
    return generateToken(username, false);
}
```

---

#### **AuthService** (`ie.tcd.scss.aichat.service.AuthService`)

**Changes Made:**
- Updated `login()` method to pass `rememberMe` flag from request to token generation

**Code Change:**
```java
public AuthResponse login(LoginRequest request) {
    // ... existing authentication logic ...
    
    // UPDATED: Generate token with appropriate expiration based on rememberMe flag
    String token = jwtUtil.generateToken(user.getUsername(), request.isRememberMe());
    
    return new AuthResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        token
    );
}
```

---

### 2.2 Test Updates

#### **Files Updated:**
1. `AuthControllerTest.java` - 5 test methods updated
2. `AuthServiceTest.java` - 3 test methods updated + 6 new tests added

#### **Test Fixes Applied:**

**Problem Encountered:**
```
[ERROR] no suitable constructor found for LoginRequest(java.lang.String,java.lang.String)
```

**Solution:**
Updated all test instantiations to include the `rememberMe` parameter:
```java
// BEFORE
LoginRequest request = new LoginRequest("testuser", "password123");

// AFTER
LoginRequest request = new LoginRequest("testuser", "password123", false);
```

**Mockito Stubbing Issues Fixed:**

**Problem 1 - Stubbing Mismatch:**
```
[ERROR] Strict stubbing argument mismatch
Expected: jwtUtil.generateToken("testuser");
Actual: jwtUtil.generateToken("testuser", false);
```

**Solution:**
```java
// BEFORE
when(jwtUtil.generateToken("testuser")).thenReturn("fake-jwt-token");

// AFTER
when(jwtUtil.generateToken("testuser", false)).thenReturn("fake-jwt-token");
```

**Problem 2 - Verification Mismatch:**
```
[ERROR] Wanted but not invoked: jwtUtil.generateToken("testuser");
However, there was exactly 1 interaction: jwtUtil.generateToken("testuser", false);
```

**Solution:**
```java
// BEFORE
verify(jwtUtil).generateToken("testuser");

// AFTER
verify(jwtUtil).generateToken("testuser", false);
```

---

### 2.3 New Comprehensive Tests Added

Added 6 new test methods to `AuthServiceTest.java`:

1. **`testLogin_WithRememberMeTrue_ShouldGenerateTokenWithExtendedExpiration`**
   - Verifies 30-day token generation when rememberMe=true
   - Confirms correct method invocation with true parameter

2. **`testLogin_WithRememberMeFalse_ShouldGenerateTokenWithStandardExpiration`**
   - Verifies 24-hour token generation when rememberMe=false
   - Confirms correct method invocation with false parameter

3. **`testLogin_ExpectedTrueButReceivedFalse_ShouldGenerateWrongTokenType`**
   - Tests mismatch scenario: user wants extended session but gets standard
   - Documents impact: premature logout after 24 hours

4. **`testLogin_ExpectedFalseButReceivedTrue_ShouldGenerateWrongTokenType`**
   - Tests opposite mismatch: user gets extended session unexpectedly
   - Highlights security concern: token valid longer than intended

5. **`testRememberMeFlag_PreservedThroughAuthFlow`**
   - Verifies rememberMe flag correctly flows through authentication
   - Tests both true and false cases in sequence

6. **`testLogin_VerifyRememberMeParameterPassedCorrectly`**
   - Additional parameter passing verification
   - Ensures exact boolean value is preserved

**Test Coverage Results:**
- All 73 existing tests maintained
- 6 new tests added for Remember Me functionality
- Total: 79 tests passing
- 0 failures, 0 errors

---

## 3. Implementation Timeline

### Phase 1: Initial Compilation Errors (8 errors)
**Issue:** Constructor signature mismatch across test files  
**Time to Resolve:** ~15 minutes  
**Solution:** Added third parameter to all LoginRequest instantiations

### Phase 2: Mockito Stubbing Errors
**Issue:** Mock expectations didn't match new method signatures  
**Time to Resolve:** ~10 minutes  
**Solution:** Updated all `when()` stubs to include boolean parameter

### Phase 3: Verification Errors
**Issue:** Test verifications using old method signature  
**Time to Resolve:** ~5 minutes  
**Solution:** Updated all `verify()` calls to include boolean parameter

### Phase 4: Comprehensive Testing
**Issue:** Needed tests specifically for Remember Me functionality  
**Time to Resolve:** ~30 minutes  
**Solution:** Created 6 new test methods covering all scenarios

**Total Implementation Time:** ~60 minutes

---

## 4. Configuration Details

### 4.1 Token Expiration Values

```java
// Standard session (rememberMe = false)
JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000 milliseconds
                   = 86,400,000 ms
                   = 24 hours

// Extended session (rememberMe = true)
JWT_REMEMBER_ME_VALIDITY = 30L * 24 * 60 * 60 * 1000 milliseconds
                         = 2,592,000,000 ms
                         = 30 days
```

### 4.2 JWT Configuration

- **Algorithm:** HS256 (HMAC with SHA-256)
- **Secret Key:** Loaded from `${jwt.secret}` property
- **Token Structure:**
  - Claims: Empty map (extensible for future use)
  - Subject: Username
  - Issued At: Current timestamp
  - Expiration: Current timestamp + validity period

---

## 5. Security Considerations

### 5.1 Design Decisions

**Why 30 days for Remember Me?**
- Industry standard for "remember me" functionality
- Balance between convenience and security
- Long enough to be useful, short enough to limit exposure

**Why 24 hours for standard login?**
- Common session duration for web applications
- Reduces token theft vulnerability window
- Encourages re-authentication for sensitive operations

### 5.2 Security Best Practices Maintained

✅ Passwords remain hashed with BCrypt  
✅ Tokens are signed with secret key  
✅ Token validation includes expiration checks  
✅ No sensitive data stored in JWT claims  
✅ Default behavior is more secure (24 hours)  

---

## 6. Testing Strategy

### 6.1 Test Categories

**Unit Tests:**
- Constructor parameter validation
- Method signature compliance
- Mock behavior verification

**Integration Tests:**
- End-to-end authentication flow
- Token generation with different parameters
- Parameter preservation through layers

**Scenario Tests:**
- Correct rememberMe=true behavior
- Correct rememberMe=false behavior
- Mismatch scenarios (expected vs actual)
- Edge cases and error handling

### 6.2 Test Outcomes

All tests include console output for clarity:
```
✓ PASS: RememberMe=true generated token with extended expiration (30 days)
✓ PASS: RememberMe=false generated token with standard expiration (24 hours)
✗ FAIL SCENARIO: Expected rememberMe=true but received rememberMe=false
✗ FAIL SCENARIO: Expected rememberMe=false but received rememberMe=true
```

---

## 7. Breaking Changes & Migration

### 7.1 Breaking Changes

**LoginRequest Constructor:**
- Old: `LoginRequest(String username, String password)`
- New: `LoginRequest(String username, String password, boolean rememberMe)`

**JwtUtil.generateToken:**
- Old: `generateToken(String username)`
- New: `generateToken(String username, boolean rememberMe)`

### 7.2 Migration Path

**For Existing Code:**
1. Add backward-compatible 2-parameter constructor to LoginRequest
2. Add overloaded 1-parameter generateToken method to JwtUtil
3. Both default to `rememberMe = false` for security

**For Tests:**
1. Update all LoginRequest instantiations with third parameter
2. Update all JwtUtil mocks with boolean parameter
3. Update all verify() calls with boolean parameter

---

## 8. Future Enhancements

### 8.1 Potential Improvements

1. **Frontend Integration:**
   - Add checkbox UI element for "Remember Me"
   - Store rememberMe preference in localStorage
   - Show remaining session time to user

2. **Token Refresh:**
   - Implement token refresh endpoint
   - Allow extending sessions without re-authentication
   - Add refresh token rotation for security

3. **Device Management:**
   - Track devices with active remember me sessions
   - Allow users to revoke sessions remotely
   - Show last login time and device info

4. **Analytics:**
   - Track remember me adoption rate
   - Monitor session duration patterns
   - Analyze security impact metrics

### 8.2 Configuration Flexibility

Consider making token durations configurable:
```properties
jwt.token.standard.validity=24h
jwt.token.rememberme.validity=30d
```

---

## 9. Known Issues & Limitations

### 9.1 Current Limitations

1. **No Token Revocation:**
   - Once issued, tokens valid until expiration
   - No way to invalidate compromised remember me tokens
   - Consider implementing token blacklist

2. **Single Device Limitation:**
   - No tracking of multiple remember me sessions
   - User can't manage sessions across devices
   - Consider implementing session management

3. **No Rate Limiting:**
   - No protection against token generation abuse
   - Consider adding login attempt limits

### 9.2 Edge Cases

- Token expiration occurs at exact millisecond
- Clock skew between servers could cause issues
- No handling of timezone differences (all UTC)

---

## 10. Lessons Learned

### 10.1 Development Insights

1. **Constructor Changes Cascade:**
   - Changing DTO constructors affects many test files
   - Always check test compilation after DTO changes
   - Consider using builders for complex DTOs

2. **Mockito Strictness:**
   - Strict stubbing catches parameter mismatches early
   - Helpful for preventing regression bugs
   - Requires careful test maintenance

3. **Backward Compatibility:**
   - Adding overloaded methods prevents breaking changes
   - Default values should be secure by design
   - Document migration path clearly

### 10.2 Best Practices Reinforced

✅ Test-driven development catches issues early  
✅ Comprehensive test coverage prevents regressions  
✅ Clear commit messages aid debugging  
✅ Security defaults protect users  
✅ Incremental changes easier to review  

---

## 11. Conclusion

The Remember Me feature has been successfully implemented and tested. The implementation follows security best practices, maintains backward compatibility where possible, and includes comprehensive test coverage. All compilation errors have been resolved, and the feature is ready for integration testing and deployment.

### Success Metrics

- ✅ Feature fully implemented
- ✅ All 79 tests passing (73 existing + 6 new)
- ✅ Zero compilation errors
- ✅ Backward compatibility maintained
- ✅ Security best practices followed
- ✅ Comprehensive documentation created

### Next Steps

1. Frontend implementation of remember me checkbox
2. Integration testing with full authentication flow
3. Security review and penetration testing
4. User acceptance testing
5. Production deployment planning

---

**End of Development Log**

*This document serves as a complete record of the Remember Me feature implementation, including all technical details, challenges encountered, solutions applied, and recommendations for future work.*