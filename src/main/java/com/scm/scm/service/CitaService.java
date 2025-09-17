package com.scm.scm.service;

import com.scm.scm.dto.CitaDTO;
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
}
