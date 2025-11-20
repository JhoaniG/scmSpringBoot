package com.scm.scm.service;

import com.scm.scm.dto.ActividadFisicaDTO;
import com.scm.scm.dto.ActividadVistaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ActividadFisicaService {
    ActividadFisicaDTO crearActividadFisica(ActividadFisicaDTO actividadFisicaDTO);
    ActividadFisicaDTO obtenerActividadFisicaPorId(Long id);
    ActividadFisicaDTO actualizarActividadFisica(Long id, ActividadFisicaDTO actividadFisicaDTO);
    void eliminarActividadFisica(Long id);
    List<ActividadFisicaDTO>encontrartodasLasActividades();

    List<ActividadVistaDTO> obtenerActividadesPorMascotaId(Long mascotaId);
    Page<ActividadFisicaDTO> getAllActividadesPaginadas(Pageable pageable);

    void terminarActividad(Long id);

    @Transactional
    List<ActividadVistaDTO> obtenerHistorialActividadesPorMascotaId(Long mascotaId);
}
