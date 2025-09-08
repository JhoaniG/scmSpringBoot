package com.scm.scm.repository;

import com.scm.scm.model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VeterinarioRepositorio extends JpaRepository<Veterinario, Long> {
    @Query("SELECT v FROM Veterinario v WHERE v.usuario.idUsuario = :idUsuario")
    Optional<Veterinario> findByUsuarioId(@Param("idUsuario") Long idUsuario);


}
