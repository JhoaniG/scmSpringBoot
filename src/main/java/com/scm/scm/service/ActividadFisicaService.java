package com.scm.scm.service;

import com.scm.scm.dto.ActividadFisicaDTO;

import java.util.List;

public interface ActividadFisicaService {
    ActividadFisicaDTO crearActividadFisica(ActividadFisicaDTO actividadFisicaDTO);
    ActividadFisicaDTO obtenerActividadFisicaPorId(Long id);
    ActividadFisicaDTO actualizarActividadFisica(Long id, ActividadFisicaDTO actividadFisicaDTO);
    void eliminarActividadFisica(Long id);
    List<ActividadFisicaDTO>encontrartodasLasActividades();
}
