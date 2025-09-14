package com.scm.scm.service;

import com.scm.scm.dto.MascotaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MascotaService {
    MascotaDTO crearMascota(MascotaDTO mascotaDTO);
    MascotaDTO obtenerMascotaPorNombre(String nombre);
    MascotaDTO obtenerMascotaPorId(Long id);
    MascotaDTO actualizarMascota(Long id, MascotaDTO mascotaDTO);
    void eliminarMascota(Long id);
    List<MascotaDTO>getAllMascotas();
    List<MascotaDTO> obtenerMascotasPorDuenoId(Long duenoId);
    Page<MascotaDTO> getAllMascotasPaginadas(Pageable pageable);
}
