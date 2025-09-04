package com.scm.scm.service;

import com.scm.scm.dto.UsuarioDTO;

import java.util.List;

public interface UsuarioService
{
    UsuarioDTO createUser(UsuarioDTO usuarioDTO);
    UsuarioDTO updateUser(Long id, UsuarioDTO usuarioDTO);
    UsuarioDTO getUserById(Long id);
    void deleteUser(Long id);
    UsuarioDTO authenticateUser(String email, String password);
    List<UsuarioDTO> findAllUsers();


}
