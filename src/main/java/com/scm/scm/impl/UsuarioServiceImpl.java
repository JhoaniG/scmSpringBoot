package com.scm.scm.impl;

import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.exceptions.CustomExeception;
import com.scm.scm.model.Rol;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.RolRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.service.UsuarioService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

private  final UsuarioRepositorio usuarioRepositorio;
private  final RolRepositorio rolRepositorio;

    private  final ModelMapper modelMapper;
    public UsuarioServiceImpl(UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio, ModelMapper modelMapper) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.modelMapper = modelMapper;
    }


    @Override
    public UsuarioDTO createUser(UsuarioDTO usuarioDTO) {
        if (usuarioRepositorio.findByEmail(usuarioDTO.getEmail()) != null) {
            throw new CustomExeception("Ya existe un usuario con ese email");
        }
        Usuario usuario=modelMapper.map( usuarioDTO, Usuario.class);
        if (usuarioDTO.getRolId() != null){
            Rol rol = rolRepositorio.findById(usuarioDTO.getRolId())
                    .orElseThrow(() -> new CustomExeception("Rol no encontrado con ID: " + usuarioDTO.getRolId()));
            usuario.setRol(rol);
        }


        usuario=usuarioRepositorio.save(usuario);
        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    @Override
    public UsuarioDTO updateUser(Long id, UsuarioDTO usuarioDTO) {
        if (usuarioRepositorio.existsById(id)){
            Usuario usuario=usuarioRepositorio.findById(id)
                    .orElseThrow(() -> new CustomExeception("No existe un usuario con el ID: " + id));
            usuario.setNombre(usuarioDTO.getNombre());
            usuario.setEmail(usuarioDTO.getEmail());
            usuario.setContrasena(usuarioDTO.getContrasena());
            if (usuarioDTO.getRolId() != null){
                Rol rol = rolRepositorio.findById(usuarioDTO.getRolId())
                        .orElseThrow(() -> new CustomExeception("Rol no encontrado con ID: " + usuarioDTO.getRolId()));
                usuario.setRol(rol);
            }
            usuario=usuarioRepositorio.save(usuario);
            return modelMapper.map(usuario, UsuarioDTO.class);


        }else {
            throw new CustomExeception("No existe un usuario con el ID: " + id);
        }



    }

    @Override
    public UsuarioDTO getUserById(Long id) {

        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new CustomExeception("No existe un usuario con el ID: " + id));


        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    @Override
    public void deleteUser(Long id) {

        if (!usuarioRepositorio.existsById(id)) {
            throw new CustomExeception("No existe un usuario con el ID: " + id);
        }


        usuarioRepositorio.deleteById(id);

    }

    @Override
    public UsuarioDTO authenticateUser(String email, String password) {
        Usuario usuario = usuarioRepositorio.findByEmailAndContrasena(email, password)
                .orElseThrow(() -> new CustomExeception("Credenciales inválidas"));

        return modelMapper.map(usuario, UsuarioDTO.class);

    }


    @Override
    public List<UsuarioDTO> findAllUsers() {
        List<Usuario> usuarios=usuarioRepositorio.findAll();
        return usuarios.stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class))
                .toList();
    }


}
