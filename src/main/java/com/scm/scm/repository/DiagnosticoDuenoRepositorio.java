package com.scm.scm.repository;

import com.scm.scm.model.Cita;
import com.scm.scm.model.Diagnosticodueno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosticoDuenoRepositorio extends JpaRepository<Diagnosticodueno, Long> {
    List<Diagnosticodueno> findByVeterinario_IdVeterinario(Long idVeterinario);
    List<Diagnosticodueno> findByMascota_IdMascota(Long idMascota);

}
