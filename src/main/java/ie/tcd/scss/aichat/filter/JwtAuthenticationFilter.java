package ie.tcd.scss.aichat.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import ie.tcd.scss.aichat.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Authentication Filter
 * Intercepts every HTTP request to validate JWT tokens and set up Spring Security authentication.
 * 
 * This filter:
 * 1. Extracts JWT token from Authorization header
 * 2. Validates the token signature and expiration
 * 3. Loads user details from database
 * 4. Sets authentication in Spring Security context
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Extract Authorization header
        final String authorizationHeader = request.getHeader("Study-Auth");

        String username = null;
        String jwtToken = null;

        // Check if header contains Bearer token
        if (authorizationHeader != null) {
            jwtToken = authorizationHeader;
            
            try {
                username = jwtUtil.extractUsername(jwtToken);
            } catch (Exception e) {
                // Invalid token - continue without authentication
                logger.warn("Failed to extract username from JWT: " + e.getMessage());
            }
        }

        // If we have a username and no authentication is set yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            try {
                // Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Validate token
                if (jwtUtil.validateToken(jwtToken, userDetails.getUsername())) {
                    
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authenticationToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    
                    authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Set authentication in Spring Security context
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    
                    logger.debug("JWT authentication successful for user: " + username);
                } else {
                    logger.warn("JWT token validation failed for user: " + username);
                }
            } catch (Exception e) {
                logger.error("Error during JWT authentication: " + e.getMessage());
            }
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
