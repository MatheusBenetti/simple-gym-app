package com.totex.simplegymapp.business.service;

import com.totex.simplegymapp.business.converter.UserConverter;
import com.totex.simplegymapp.infrastructure.exception.ConflictException;
import com.totex.simplegymapp.infrastructure.repository.UserRepository;
import com.totex.simplegymapp.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void emailExists(String email) {
        try {
            boolean exists = verifyExistentEmail(email);

            if (exists) {
                throw new ConflictException("Email already exists.");
            }
        } catch (ConflictException e) {
            throw new ConflictException("Email already exists." + e.getCause());
        }
    }

    public boolean verifyExistentEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
