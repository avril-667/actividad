package com.inventory.system.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTOs para autenticación y gestión de tokens JWT.
 */
public class AuthDTO {

    /**
     * DTO para solicitud de login.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class LoginRequest {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
        private String password;
    }

    /**
     * DTO para respuesta de login exitoso.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class LoginResponse {

        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private Long expiresIn;
        private UsuarioInfo usuario;
    }

    /**
     * Información del usuario en la respuesta de login.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class UsuarioInfo {

        private Long id;
        private String email;
        private String nombreCompleto;
        private String rol;
        private Long especialidadId;
        private String especialidadNombre;
        private String especialidadCodigo;
    }

    /**
     * DTO para renovar token (refresh token).
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class RefreshTokenRequest {

        @NotBlank(message = "El refresh token es obligatorio")
        private String refreshToken;
    }

    /**
     * DTO para cambiar contraseña.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class CambioPasswordRequest {

        @NotBlank(message = "La contraseña actual es obligatoria")
        private String passwordActual;

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La nueva contraseña debe tener entre 6 y 100 caracteres")
        private String passwordNueva;

        @NotBlank(message = "La confirmación de contraseña es obligatoria")
        private String passwordConfirmacion;
    }
}