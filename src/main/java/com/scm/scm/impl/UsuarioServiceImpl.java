package com.scm.scm.impl;

import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.exceptions.CustomExeception;
import com.scm.scm.model.Rol;
import com.scm.scm.model.Usuario;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.RolRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.UsuarioService;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

private  final UsuarioRepositorio usuarioRepositorio;
private  final RolRepositorio rolRepositorio;
private final PasswordEncoder passwordEncoder;
private final VeterinarioRepositorio veterinarioRepositorio;


    private  final ModelMapper modelMapper;
    public UsuarioServiceImpl(UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio, PasswordEncoder passwordEncoder, VeterinarioRepositorio veterinarioRepositorio, ModelMapper modelMapper) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.veterinarioRepositorio = veterinarioRepositorio;
        this.modelMapper = modelMapper;
    }

    public UsuarioDTO findByEmail(String email) {
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new CustomExeception("Usuario no encontrado"));
        return modelMapper.map(usuario, UsuarioDTO.class);
    }


    @Override
    @Transactional
    public UsuarioDTO createUser(UsuarioDTO usuarioDTO) {
        // Verificar si ya existe el email
        if (usuarioRepositorio.findByEmail(usuarioDTO.getEmail()).isPresent()) {
            throw new CustomExeception("Ya existe un usuario con ese email");
        }

        Usuario usuario = modelMapper.map(usuarioDTO, Usuario.class);

        // Encriptar contraseña
        if (usuarioDTO.getContrasena() != null && !usuarioDTO.getContrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(usuarioDTO.getContrasena()));
        } else {
            throw new CustomExeception("La contraseña no puede estar vacía");
        }

        // Procesar foto
        MultipartFile archivo = usuarioDTO.getArchivoFoto();
        if (archivo != null && !archivo.isEmpty()) {
            try {
                // Ruta absoluta dentro del proyecto
                Path rutaUploads = Paths.get(System.getProperty("user.dir"), "uploads");
                Files.createDirectories(rutaUploads); // crea carpeta si no existe

                // Nombre único para la foto
                String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();

                // Guardar archivo en disco
                Path rutaArchivo = rutaUploads.resolve(nombreArchivo);
                archivo.transferTo(rutaArchivo.toFile());

                // Guardar el nombre de la foto en la entidad
                usuario.setFoto(nombreArchivo);
            } catch (IOException e) {
                throw new CustomExeception("Error al guardar la foto: " + e.getMessage());
            }
        }

        // Asignar rol
        Rol rol = rolRepositorio.findById(usuarioDTO.getRolId())
                .orElseThrow(() -> new CustomExeception("Rol no encontrado con ID: " + usuarioDTO.getRolId()));
        usuario.setRol(rol);

        // Guardar usuario en DB
        usuario = usuarioRepositorio.save(usuario);

        // Si es veterinario → crear registro en veterinarios
        if ("Veterinario".equalsIgnoreCase(rol.getRol())) {
            if (usuarioDTO.getEspecialidad() == null || usuarioDTO.getVeterinaria() == null) {
                throw new CustomExeception("Debe especificar especialidad y clínica para el veterinario");
            }
            Veterinario veterinario = new Veterinario();
            veterinario.setEspecialidad(usuarioDTO.getEspecialidad());
            veterinario.setVeterinaria(usuarioDTO.getVeterinaria());
            veterinario.setUsuario(usuario);
            veterinarioRepositorio.save(veterinario);
        }

        // Preparar respuesta DTO
        UsuarioDTO response = modelMapper.map(usuario, UsuarioDTO.class);
        response.setContrasena(null); // No enviar contraseña
        return response;
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
