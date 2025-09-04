package com.scm.scm.service;

import com.scm.scm.dto.RolDTO;

import java.util.List;

public interface RolService {
    RolDTO crearRol(RolDTO rolDTO);
    RolDTO obtenerRolPorId(Long id);
    RolDTO obtenerRolPorNombre(String nombre);
    RolDTO actualizarRol(Long id, RolDTO rolDTO);
    void eliminarRol(Long id);
    RolDTO convertirARolDTO(com.scm.scm.model.Rol rol);
    List<RolDTO>getAllRoles();
}
