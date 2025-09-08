package com.scm.scm.repository;

import com.scm.scm.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailAndContrasena(String email, String contrasena);
    List<Usuario> findByRolIdRol(Long idRol);
}
