package com.scm.scm.service;

import com.scm.scm.dto.DietaDTO;
import com.scm.scm.dto.DietaVistaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DietaService {
    DietaDTO crearDieta(DietaDTO dietaDTO);
    DietaDTO obtenerDietaPorId(Long id);
    DietaDTO actualizarDieta(Long id, DietaDTO dietaDTO);
    void eliminarDieta(Long id);
    DietaDTO obtenerDietaPorMascotaId(Long mascotaId);
    DietaDTO obtenerDietaPorDuenoId(Long duenoId);
    List<DietaDTO> obtenerTodasLasDietas();
    List<DietaVistaDTO> obtenerDietasPorMascotaId(Long mascotaId);
    Page<DietaDTO> getAllDietasPaginadas(Pageable pageable);

    @Transactional
    List<DietaVistaDTO> obtenerHistorialDietasPorMascotaId(Long mascotaId);

    // También necesitamos el método para TERMINAR (cambiar fecha fin a ayer)
    void terminarDieta(Long id);
}
