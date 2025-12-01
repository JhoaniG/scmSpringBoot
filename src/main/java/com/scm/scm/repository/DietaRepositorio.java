package com.scm.scm.repository;

import com.scm.scm.model.Dieta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DietaRepositorio extends JpaRepository<Dieta, Long> {
    List<Dieta> findByMascota_IdMascota(Long idMascota);
    long countByVeterinario_IdVeterinario(Long idVeterinario);

 }
