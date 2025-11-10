# Spring Security Fix - AI Assistant Session Log

**Date:** November 10, 2025  
**Student:** Hao She  
**Issue:** Spring Security blocking API endpoints with 401/403 errors

---

## Session 1: Initial Problem

**My Prompt:**
```
[ERROR] Failures: 
[ERROR]   QuizControllerTest.testGenerateQuiz_Success:75 Status expected:<200> but was:<403>
[ERROR]   QuizControllerTest.testTestEndpoint_ReturnsSuccess:257 Status expected:<200> but was:<401>
[ERROR] Tests run: 34, Failures: 12, Errors: 0, Skipped: 0

i failed to do mvn build, what happened?
```

**Claude's Response:**
Your tests are failing because Spring Security is blocking the requests. The tests expect status codes like 200 or 400, but they're getting 403 (Forbidden) and 401 (Unauthorized).

Solution: Add `@AutoConfigureMockMvc(addFilters = false)` to your test class to disable security filters.

---

## Session 2: Runtime Issue

**My Prompt:**
```bash
curl -X POST http://localhost:8080/api/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{"studyMaterial": "...", "count": 3, "difficulty": "medium"}'
# Result: 0 bytes received

why is this not working?
```

**Claude's Response:**
The request is being blocked by Spring Security. Run with `-v` flag to see the response:

```bash
curl -X POST http://localhost:8080/api/quiz/generate ... -v
```

Result showed: `HTTP/1.1 401` - authentication required.

You need to update your SecurityConfig to allow public access to these endpoints.

---

## Session 3: Finding Configuration

**My Prompt:**
```
there is no security config file
```

**Claude's Solution:**
Create a SecurityConfig file to explicitly configure which endpoints are public.

---

## Final Solution

Created `src/main/java/ie/tcd/scss/aichat/config/SecurityConfig.java`:

```java
package ie.tcd.scss.aichat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/quiz/**").permitAll()
                .requestMatchers("/api/flashcards/**").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }
}
```

**Result:** All tests pass ✓ API endpoints accessible ✓