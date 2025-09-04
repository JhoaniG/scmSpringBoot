package com.scm.scm.impl;

import com.scm.scm.dto.RolDTO;
import com.scm.scm.model.Rol;
import com.scm.scm.repository.RolRepositorio;
import com.scm.scm.service.RolService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolServiceImpl implements RolService {
    private final RolRepositorio rolRepositorio;
    private final ModelMapper modelMapper;

    public RolServiceImpl(RolRepositorio rolRepositorio, ModelMapper modelMapper) {
        this.rolRepositorio = rolRepositorio;
        this.modelMapper = modelMapper;
    }

    @Override
    public RolDTO crearRol(RolDTO rolDTO) {
       Rol rol =modelMapper.map(rolDTO , Rol.class);
       rol=rolRepositorio.save(rol);



        return modelMapper.map(rol, RolDTO.class );
    }

    @Override
    public RolDTO obtenerRolPorId(Long id) {
        Rol rol= (Rol) rolRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol not found with id: " + id));

        return modelMapper.map(rol, RolDTO.class);
    }

    @Override
    public RolDTO obtenerRolPorNombre(String nombre) {
        return null;
    }

    @Override
    public RolDTO actualizarRol(Long id, RolDTO rolDTO) {
        Rol rol = (Rol) rolRepositorio.findById(id)
                .orElseThrow(()-> new RuntimeException( "Rol not found with id: " + id));
        rol.setRol(rolDTO.getRol());
        Rol updatedRol = rolRepositorio.save(rol);
        return  modelMapper.map(updatedRol, RolDTO.class);

    }

    @Override
    public void eliminarRol(Long id) {
        if (rolRepositorio.existsById(id)){
            rolRepositorio.deleteById(id);

        }else{
            throw  new RuntimeException("Rol not found with id: " + id);

        }

    }

    @Override
    public RolDTO convertirARolDTO(Rol rol) {
        return null;
    }

    @Override
    public List<RolDTO> getAllRoles() {
        List<Rol> roles=rolRepositorio.findAll();
        return roles.stream()
                .map(rol -> modelMapper.map(rol, RolDTO.class))
                .toList();
    }
}
