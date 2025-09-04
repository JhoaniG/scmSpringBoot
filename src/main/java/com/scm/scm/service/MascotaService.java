package com.scm.scm.service;

import com.scm.scm.dto.MascotaDTO;

import java.util.List;

public interface MascotaService {
    MascotaDTO crearMascota(MascotaDTO mascotaDTO);
    MascotaDTO obtenerMascotaPorNombre(String nombre);
    MascotaDTO obtenerMascotaPorId(Long id);
    MascotaDTO actualizarMascota(Long id, MascotaDTO mascotaDTO);
    void eliminarMascota(Long id);
    List<MascotaDTO>getAllMascotas();
}
