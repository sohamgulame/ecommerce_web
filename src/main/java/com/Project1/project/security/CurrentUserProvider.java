package com.Project1.project.security;

import com.Project1.project.entity.User;
import com.Project1.project.repository.UserRepository;
import com.Project1.project.service.impl.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username).map(User::getId)
                .orElseThrow(() -> new RuntimeException("Unauthenticated"));
    }

    public User getCurrentUser() {
        Long id = getCurrentUserId();
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
