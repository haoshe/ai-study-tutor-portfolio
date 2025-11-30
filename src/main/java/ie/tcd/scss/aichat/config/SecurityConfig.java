package ie.tcd.scss.aichat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import ie.tcd.scss.aichat.filter.JwtAuthenticationFilter;

/**
 * Security configuration for the AI Chat application.
 * Configures JWT-based stateless authentication and authorization rules.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disable CSRF for stateless JWT API
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // No sessions - use JWT tokens
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()        // Allow public access to login/register
                .requestMatchers("/api/slides/**").permitAll()      // Allow document upload (will secure in Job 3)
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**").permitAll()  // Allow Swagger UI access
                .anyRequest().authenticated()                        // All other endpoints require valid JWT
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  // Add JWT validation before auth
        
        return http.build();
    }
}
