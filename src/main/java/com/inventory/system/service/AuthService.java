package com.inventory.system.service;

import com.inventory.system.dto.AuthDTO;
import com.inventory.system.entity.Usuario;
import com.inventory.system.exception.RecursoNoEncontradoException;
import com.inventory.system.exception.ValidacionException;
import com.inventory.system.repository.UsuarioRepository;
import com.inventory.system.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de autenticación y gestión de tokens JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Autentica un usuario y genera tokens JWT.
     */
    @Transactional(readOnly = true)
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        // Autenticar con Spring Security
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Obtener usuario
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", "email: " + request.getEmail()));

        if (!usuario.getActivo()) {
            throw new ValidacionException(List.of("La cuenta está desactivada"));
        }

        // Generar tokens
        String accessToken = jwtTokenProvider.generateAccessToken(usuario);
        String refreshToken = jwtTokenProvider.generateRefreshToken(usuario);

        // Actualizar último login
        usuarioRepository.updateUltimoLogin(usuario.getId());

        // Construir respuesta
        AuthDTO.UsuarioInfo usuarioInfo = AuthDTO.UsuarioInfo.builder()
            .id(usuario.getId())
            .email(usuario.getEmail())
            .nombreCompleto(usuario.getNombreCompleto())
            .rol(usuario.getRol().name())
            .especialidadId(usuario.getEspecialidad() != null ? usuario.getEspecialidad().getId() : null)
            .especialidadNombre(usuario.getEspecialidad() != null ? usuario.getEspecialidad().getNombre() : null)
            .especialidadCodigo(usuario.getEspecialidad() != null ? usuario.getEspecialidad().getCodigo() : null)
            .build();

        return AuthDTO.LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
            .usuario(usuarioInfo)
            .build();
    }

    /**
     * Renueva el access token usando refresh token.
     */
    @Transactional(readOnly = true)
    public AuthDTO.LoginResponse refreshToken(AuthDTO.RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new ValidacionException(List.of("Refresh token inválido o expirado"));
        }

        String email = jwtTokenProvider.getEmailFromToken(request.getRefreshToken());
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", email));

        if (!usuario.getActivo()) {
            throw new ValidacionException(List.of("La cuenta está desactivada"));
        }

        String accessToken = jwtTokenProvider.generateAccessToken(usuario);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(usuario);

        AuthDTO.UsuarioInfo usuarioInfo = AuthDTO.UsuarioInfo.builder()
            .id(usuario.getId())
            .email(usuario.getEmail())
            .nombreCompleto(usuario.getNombreCompleto())
            .rol(usuario.getRol().name())
            .especialidadId(usuario.getEspecialidad() != null ? usuario.getEspecialidad().getId() : null)
            .especialidadNombre(usuario.getEspecialidad() != null ? usuario.getEspecialidad().getNombre() : null)
            .especialidadCodigo(usuario.getEspecialidad() != null ? usuario.getEspecialidad().getCodigo() : null)
            .build();

        return AuthDTO.LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(newRefreshToken)
            .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
            .usuario(usuarioInfo)
            .build();
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     */
    @Transactional
    public void cambiarPassword(String email, AuthDTO.CambioPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", email));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
            throw new ValidacionException(List.of("La contraseña actual es incorrecta"));
        }

        // Verificar que la nueva contraseña coincida con la confirmación
        if (!request.getPasswordNueva().equals(request.getPasswordConfirmacion())) {
            throw new ValidacionException(List.of("La nueva contraseña y su confirmación no coinciden"));
        }

        // Verificar que la nueva contraseña sea diferente a la actual
        if (passwordEncoder.matches(request.getPasswordNueva(), usuario.getPasswordHash())) {
            throw new ValidacionException(List.of("La nueva contraseña debe ser diferente a la actual"));
        }

        // Actualizar contraseña
        usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }

    /**
     * Obtiene el usuario autenticado actual.
     */
    @Transactional(readOnly = true)
    public AuthDTO.UsuarioInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", email));

        return AuthDTO.UsuarioInfo.builder()
            .id(usuario.getId())
            .email(usuario.getEmail())
            .nombreCompleto(usuario.getNombreCompleto())
            .rol(usuario.getRol().name())
            .especialidadId(usuario.getEspecialidad() != null ? usuario.getEspecialidad().getId() : null)
            .especialidadNombre(usuario.getEspecialidad() != null ? usuario.getEspecialidad().getNombre() : null)
            .especialidadCodigo(usuario.getEspecialidad() != null ? usuario.getEspecialidad().getCodigo() : null)
            .build();
    }

    /**
     * Valida un token y retorna el email.
     */
    public String validateTokenAndGetEmail(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new ValidacionException(List.of("Token inválido o expirado"));
        }
        return jwtTokenProvider.getEmailFromToken(token);
    }
}