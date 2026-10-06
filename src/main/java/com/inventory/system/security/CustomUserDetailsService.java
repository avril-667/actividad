package com.inventory.system.security;

import com.inventory.system.entity.Usuario;
import com.inventory.system.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Implementación de UserDetailsService para cargar usuarios desde la BD.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario desactivado: " + email);
        }

        return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getPasswordHash())
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())))
            .build();
    }
}