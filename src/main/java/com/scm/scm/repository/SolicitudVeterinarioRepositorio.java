package com.scm.scm.repository;

import com.scm.scm.model.SolicitudVeterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudVeterinarioRepositorio extends JpaRepository<SolicitudVeterinario, Long> {

    // Para que el Admin vea solo las pendientes
    List<SolicitudVeterinario> findByEstado(String estado);

    // Para saber si un usuario ya tiene una solicitud en curso (y no dejarle crear otra)
    boolean existsByUsuario_IdUsuarioAndEstado(Long idUsuario, String estado);
}