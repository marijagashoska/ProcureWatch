package com.procurewatchbackend.web.controller;

import com.procurewatchbackend.dto.auth.AuthResponse;
import com.procurewatchbackend.dto.auth.CurrentUserResponse;
import com.procurewatchbackend.dto.auth.LoginRequest;
import com.procurewatchbackend.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }
}