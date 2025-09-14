package com.scm.scm.service;

import com.scm.scm.dto.VeterinarioDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VeterinarioService {
    VeterinarioDTO crearVeterinario(VeterinarioDTO veterinarioDTO);
    VeterinarioDTO obtenerVeterinarioPorEmail(String email);
    VeterinarioDTO obtenerVeterinarioPorId(Long id);
    VeterinarioDTO actualizarVeterinario(Long id, VeterinarioDTO veterinarioDTO);
    void eliminarVeterinario(Long id);
    List<VeterinarioDTO>getAllVeterinarios();
    Page<VeterinarioDTO> getAllVeterinariosPaginados(Pageable pageable);
}
