package com.inventory.system.controller;

import com.inventory.system.dto.AuthDTO;
import com.inventory.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para autenticación y gestión de tokens.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Inicia sesión y retorna tokens JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDTO.LoginResponse> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        AuthDTO.LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Renueva el access token usando refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthDTO.LoginResponse> refreshToken(@Valid @RequestBody AuthDTO.RefreshTokenRequest request) {
        AuthDTO.LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody AuthDTO.CambioPasswordRequest request) {
        authService.cambiarPassword(
            authService.getCurrentUser().getEmail(),
            request
        );
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene información del usuario autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<AuthDTO.UsuarioInfo> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    /**
     * Valida un token (útil para verificar en frontend).
     */
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                authService.validateTokenAndGetEmail(token);
                return ResponseEntity.ok(true);
            } catch (Exception e) {
                return ResponseEntity.ok(false);
            }
        }
        return ResponseEntity.ok(false);
    }
}