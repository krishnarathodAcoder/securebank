
package com.securebank.service;

import com.securebank.dto.LoginRequest;
import com.securebank.dto.LoginResponse;
import com.securebank.dto.RegisterRequest;
import com.securebank.entity.User;
import com.securebank.exception.DuplicateEmailException;
import com.securebank.repository.UserRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(
                    "Email is already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(
                passwordEncoder.encode(request.password()));

        return userRepository.save(user);
    }

    public LoginResponse loginUser(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()));

        User user = userRepository.findByEmail(
                        authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return new LoginResponse(
                "Login credentials verified",
                user.getId(),
                user.getEmail());
    }
}
