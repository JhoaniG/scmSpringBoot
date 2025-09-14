package com.scm.scm.repository;

import com.scm.scm.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MascotaRepositorio extends JpaRepository<Mascota,Long> {
    List<Mascota> findByUsuario_IdUsuario(Long idUsuario);


}
