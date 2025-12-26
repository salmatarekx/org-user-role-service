package com.example.orguserroleservice.security;

import com.example.orguserroleservice.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmailWithAuth(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return new OrgUserDetails(user);
    }
}
