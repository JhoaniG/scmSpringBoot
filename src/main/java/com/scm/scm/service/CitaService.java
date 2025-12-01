package com.scm.scm.service;

import com.scm.scm.dto.CitaDTO;
import com.scm.scm.dto.VeterinarioDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface CitaService {
    CitaDTO crearCita(CitaDTO citaDTO);
    CitaDTO obtenerCitaPorId(Long id);
    CitaDTO actualizarCita(Long id, CitaDTO citaDTO);
    void eliminarCita(Long id);
    List<CitaDTO>listarCitas();
    List<CitaDTO> listarCitasPorDueno(Long idUsuario);
    List<CitaDTO> obtenerCitasPorVeterinario(Long idVeterinario);
    Page<CitaDTO> getAllCitasPaginadas(Pageable pageable);
    Map<String, Object> obtenerDatosHistorialClinico(Long idMascota);
    List<CitaDTO> listarCitasPorMascota(Long mascotaId);

    // 1. Obtener lista de veterinarios que han atendido a una mascota
    List<VeterinarioDTO> obtenerVeterinariosDeMascota(Long mascotaId);

    // 2. Obtener historial FILTRADO por Veterinario
    Map<String, Object> obtenerHistorialPorVeterinario(Long mascotaId, Long veterinarioId);
    // ...
    long contarCitasPorMascota(Long idMascota);
}
