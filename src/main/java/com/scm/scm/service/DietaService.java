package com.scm.scm.service;

import com.scm.scm.dto.DietaDTO;

import java.util.List;

public interface DietaService {
    DietaDTO crearDieta(DietaDTO dietaDTO);
    DietaDTO obtenerDietaPorId(Long id);
    DietaDTO actualizarDieta(Long id, DietaDTO dietaDTO);
    void eliminarDieta(Long id);
    DietaDTO obtenerDietaPorMascotaId(Long mascotaId);
    DietaDTO obtenerDietaPorDuenoId(Long duenoId);
    List<DietaDTO> obtenerTodasLasDietas();
}
