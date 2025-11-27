package ie.tcd.scss.aichat.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.repository.UserRepository;

/**
 * Custom UserDetailsService that loads users from the MySQL database.
 * Used by Spring Security for authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user by username in database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Convert to Spring Security UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())  // Use username (must match JWT subject)
                .password(user.getPasswordHash())
                .authorities(new ArrayList<>())  // No roles for now
                .build();
    }
}
