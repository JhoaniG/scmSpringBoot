package com.scm.scm.repository;

import com.scm.scm.model.PasswordResetToken;
import com.scm.scm.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken>findByToken(String token);
    Optional<PasswordResetToken>findByUsuario(Usuario usuario);


}
