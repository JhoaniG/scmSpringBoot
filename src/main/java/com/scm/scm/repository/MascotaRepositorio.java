package com.scm.scm.repository;

import com.scm.scm.model.Mascota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MascotaRepositorio extends JpaRepository<Mascota,Long> {
    List<Mascota> findByUsuario_IdUsuario(Long idUsuario);
    // Esta consulta busca en las citas el ID de un veterinario y devuelve
    // una lista de las mascotas asociadas, sin duplicados.
    /**@Query("SELECT DISTINCT c.mascota FROM Cita c WHERE c.veterinario.idVeterinario = :idVeterinario")
    List<Mascota> findPacientesByVeterinarioId(@Param("idVeterinario") Long idVeterinario);
     **/
    @Query("SELECT DISTINCT c.mascota FROM Cita c " +
            "WHERE c.veterinario.idVeterinario = :veterinarioId")
    Page<Mascota> findPacientesByVeterinarioId(@Param("veterinarioId") Long veterinarioId, Pageable pageable);

    // --- MÉTODO CORREGIDO (CON FILTRO) ---
    // Empezamos desde Cita (c) y filtramos por la mascota (c.mascota) o el dueño (c.mascota.usuario)
    @Query("SELECT DISTINCT c.mascota FROM Cita c " +
            "WHERE c.veterinario.idVeterinario = :veterinarioId " +
            "AND (" +
            "   LOWER(c.mascota.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
            "   OR LOWER(c.mascota.usuario.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
            "   OR LOWER(c.mascota.usuario.apellido) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
            ")")
    Page<Mascota> findPacientesByVeterinarioIdAndFiltro(@Param("veterinarioId") Long veterinarioId, @Param("filtro") String filtro, Pageable pageable);


}
