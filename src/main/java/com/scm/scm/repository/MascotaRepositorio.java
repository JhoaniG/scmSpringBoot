package com.scm.scm.repository;

import com.scm.scm.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MascotaRepositorio extends JpaRepository<Mascota,Long> {
    List<Mascota> findByUsuario_IdUsuario(Long idUsuario);
    // Esta consulta busca en las citas el ID de un veterinario y devuelve
    // una lista de las mascotas asociadas, sin duplicados.
    @Query("SELECT DISTINCT c.mascota FROM Cita c WHERE c.veterinario.idVeterinario = :idVeterinario")
    List<Mascota> findPacientesByVeterinarioId(@Param("idVeterinario") Long idVeterinario);


}
