package com.scm.scm.repository;

import com.scm.scm.model.Cita;
import com.scm.scm.model.Dieta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CitaRepositorio extends JpaRepository<Cita, Long> {
    // Buscar citas de todas las mascotas que pertenezcan al dueño (usuario)
    @Query("SELECT c FROM Cita c WHERE c.mascota.usuario.idUsuario = :idUsuario")
    List<Cita> findByDuenoId(@Param("idUsuario") Long idUsuario);
    List<Cita> findByVeterinario_IdVeterinario(Long idVeterinario);
    List<Cita> findByMascota_IdMascota(Long idMascota);
    List<Cita> findByVeterinario_IdVeterinarioAndEstadoCita(Long veterinarioId, String estadoCita);


}
