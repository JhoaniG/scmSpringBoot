package com.scm.scm.repository;

import com.scm.scm.model.Cita;
import com.scm.scm.model.Diagnosticodueno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiagnosticoDuenoRepositorio extends JpaRepository<Diagnosticodueno, Long> {
    List<Diagnosticodueno> findByVeterinario_IdVeterinario(Long idVeterinario);
    List<Diagnosticodueno> findByMascota_IdMascota(Long idMascota);


    Page<Diagnosticodueno> findByVeterinario_IdVeterinario(Long idVeterinario, Pageable pageable);

    // ✅ ÚNICO método con "filtro", y con @Query
    @Query("SELECT d FROM Diagnosticodueno d " +
            "WHERE d.veterinario.idVeterinario = :veterinarioId " +
            "AND (" +
            "   LOWER(d.mascota.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
            "   OR LOWER(d.mascota.usuario.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
            "   OR LOWER(d.mascota.usuario.apellido) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
            "   OR LOWER(d.tipoEnfermedad) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
            ")")
    Page<Diagnosticodueno> findByVeterinarioAndFiltro(
            @Param("veterinarioId") Long veterinarioId,
            @Param("filtro") String filtro,
            Pageable pageable);}
