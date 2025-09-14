package com.scm.scm.impl;

import com.scm.scm.dto.VeterinarioDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.VeterinarioService;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VeterinarioServiceImpl implements VeterinarioService {

    private  final VeterinarioRepositorio veterinarioRepositorio;
    private final ModelMapper modelMapper;
    private  final UsuarioRepositorio usuarioRepositorio;

    public VeterinarioServiceImpl(VeterinarioRepositorio veterinarioRepositorio, ModelMapper modelMapper, UsuarioRepositorio usuarioRepositorio) {
        this.veterinarioRepositorio = veterinarioRepositorio;
        this.modelMapper = modelMapper;
        this.usuarioRepositorio = usuarioRepositorio;
    }
    public Veterinario findByUsuarioId(Long idUsuario) {
        return veterinarioRepositorio.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
    }
    @Override
    public VeterinarioDTO crearVeterinario(VeterinarioDTO veterinarioDTO) {
        Veterinario veterinario=modelMapper.map( veterinarioDTO, Veterinario.class);
        if(veterinarioDTO.getUsuarioId() != null){
            Usuario usuario= usuarioRepositorio.findById(veterinarioDTO.getUsuarioId()).orElseThrow(()-> new RuntimeException( "No existe un usuario con el ID: " + veterinarioDTO.getUsuarioId()));
            veterinario.setUsuario(usuario);
        }

        veterinario = veterinarioRepositorio.save(veterinario);
        return modelMapper.map(veterinario, VeterinarioDTO.class);
    }

    @Override
    public VeterinarioDTO obtenerVeterinarioPorEmail(String email) {
        return null;
    }

    @Override
    public VeterinarioDTO obtenerVeterinarioPorId(Long id) {
        Veterinario veterinario= veterinarioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe un veterinario con el ID: " + id));
        return modelMapper.map(veterinario, VeterinarioDTO.class);
    }

    @Override
    public VeterinarioDTO actualizarVeterinario(Long id, VeterinarioDTO veterinarioDTO) {
        if (veterinarioRepositorio.existsById(id)) {
            Veterinario veterinario = veterinarioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe un veterinario con el ID: " + id));
            veterinario.setEspecialidad(veterinarioDTO.getEspecialidad());
            veterinario.setVeterinaria(veterinarioDTO.getVeterinaria());
            if (veterinarioDTO.getUsuarioId() != null) {
                Usuario usuario = usuarioRepositorio.findById(veterinarioDTO.getUsuarioId())
                        .orElseThrow(() -> new RuntimeException("No existe un usuario con el ID: " + veterinarioDTO.getUsuarioId()));
                veterinario.setUsuario(usuario);
            }
            veterinario = veterinarioRepositorio.save(veterinario);
            return modelMapper.map(veterinario, VeterinarioDTO.class);
        }else {
            throw new RuntimeException("No existe un veterinario con el ID: " + id);
        }


    }

    @Override
    public void eliminarVeterinario(Long id) {
        // 1. Verificamos si existe
        if (!veterinarioRepositorio.existsById(id)) {
            throw new RuntimeException("No existe un veterinario con el ID: " + id);
        }

        // 2. Intentamos eliminar y capturamos el error de integridad
        try {
            veterinarioRepositorio.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // Este error ocurre si el veterinario tiene citas u otros registros asociados
            throw new RuntimeException("No se puede eliminar este veterinario porque tiene citas asociadas.");
        }
    }

    @Override
    public List<VeterinarioDTO> getAllVeterinarios() {
        List<VeterinarioDTO> veterinarioDTOS=veterinarioRepositorio .findAll()
                .stream()
                .map(veterinario -> modelMapper.map(veterinario, VeterinarioDTO.class))
                .toList();
        return veterinarioDTOS;
    }

    private VeterinarioDTO convertirADTO(Veterinario veterinario) {
        VeterinarioDTO dto = modelMapper.map(veterinario, VeterinarioDTO.class);
        if (veterinario.getUsuario() != null) {
            dto.setNombreUsuario(veterinario.getUsuario().getNombre() + " " + veterinario.getUsuario().getApellido());
            dto.setEmailUsuario(veterinario.getUsuario().getEmail());
            dto.setFotoUsuario(veterinario.getUsuario().getFoto());
        }
        return dto;
    }

    @Override
    public Page<VeterinarioDTO> getAllVeterinariosPaginados(Pageable pageable) {
        Page<Veterinario> paginaVeterinarios = veterinarioRepositorio.findAll(pageable);
        return paginaVeterinarios.map(this::convertirADTO);
    }
}
