package ie.tcd.scss.aichat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the AI Chat application.
 * Configures which endpoints require authentication and which are publicly accessible.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/quiz/**").permitAll()       // Allow public access to quiz API
                .requestMatchers("/api/flashcards/**").permitAll() // Allow public access to flashcard API
                .anyRequest().authenticated()                       // All other endpoints require authentication
            )
            .csrf(csrf -> csrf.disable()); // Disable CSRF for API testing (re-enable for production)
        
        return http.build();
    }
}
