
package com.securebank.controller;

import com.securebank.dto.LoginRequest;
import com.securebank.dto.LoginResponse;
import com.securebank.dto.RegisterRequest;
import com.securebank.entity.User;
import com.securebank.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(request);

        return Map.of(
                "message", "Customer registered successfully",
                "userId", user.getId(),
                "email", user.getEmail()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        LoginResponse response = userService.loginUser(request);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        null,
                        java.util.List.of(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        "ROLE_CUSTOMER"
                                )
                        )
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        httpRequest.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository
                        .SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        return new LoginResponse(
                response.message(),
                response.userId(),
                response.email()
        );
    }

    @PostMapping("/logout")
    public Map<String, String> logout(
            HttpServletRequest request) {

        SecurityContextHolder.clearContext();

        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return Map.of("message", "Logged out successfully");
    }
}
