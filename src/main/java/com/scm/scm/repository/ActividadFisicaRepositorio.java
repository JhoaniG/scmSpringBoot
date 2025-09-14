package com.scm.scm.repository;

import com.scm.scm.model.ActividadFisica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadFisicaRepositorio extends JpaRepository<ActividadFisica, Long> {
    List<ActividadFisica> findByMascota_IdMascota(Long idMascota);
}
