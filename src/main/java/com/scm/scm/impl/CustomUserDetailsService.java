package com.scm.scm.impl;

import com.scm.scm.model.Usuario;
import com.scm.scm.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));


        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getContrasena())
                .roles(usuario.getRol().getRol())
                .build();
    }
}