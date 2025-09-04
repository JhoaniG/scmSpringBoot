package com.scm.scm.service;

import com.scm.scm.dto.CitaDTO;

import java.util.List;

public interface CitaService {
    CitaDTO crearCita(CitaDTO citaDTO);
    CitaDTO obtenerCitaPorId(Long id);
    CitaDTO actualizarCita(Long id, CitaDTO citaDTO);
    void eliminarCita(Long id);
    List<CitaDTO>listarCitas();
}
