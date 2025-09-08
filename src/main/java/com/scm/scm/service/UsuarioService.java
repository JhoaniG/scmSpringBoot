package com.scm.scm.service;

import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;

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
