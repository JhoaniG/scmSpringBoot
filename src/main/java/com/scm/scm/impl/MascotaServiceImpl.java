package com.scm.scm.impl;

import com.scm.scm.dto.MascotaDTO;
import com.scm.scm.model.Mascota;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.service.MascotaService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class MascotaServiceImpl implements MascotaService {
    private final MascotaRepositorio mascotaRepositorio;
    private final ModelMapper modelMapper;
    private  final UsuarioRepositorio usuarioRepositorio;

    public MascotaServiceImpl(MascotaRepositorio mascotaRepositorio, ModelMapper modelMapper, UsuarioRepositorio usuarioRepositorio) {
        this.mascotaRepositorio = mascotaRepositorio;
        this.modelMapper = modelMapper;

        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Transactional
    @Override
    public MascotaDTO crearMascota(MascotaDTO mascotaDTO) {
        // Mapear DTO a entidad
        Mascota mascota = modelMapper.map(mascotaDTO, Mascota.class);

        // Validar y asignar usuario
        if (mascotaDTO.getUsuarioId() != null) {
            Usuario usuario = usuarioRepositorio.findById(mascotaDTO.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + mascotaDTO.getUsuarioId()));
            mascota.setUsuario(usuario);
        } else {
            throw new RuntimeException("El usuario de la mascota es obligatorio");
        }

        // Procesar foto
        MultipartFile archivo = mascotaDTO.getArchivoFoto();
        if (archivo != null && !archivo.isEmpty()) {
            try {
                // Carpeta de uploads dentro del proyecto
                Path rutaUploads = Paths.get(System.getProperty("user.dir"), "uploads/mascotas");
                Files.createDirectories(rutaUploads);

                // Nombre único para la foto
                String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
                Path rutaArchivo = rutaUploads.resolve(nombreArchivo);

                // Guardar archivo en disco
                archivo.transferTo(rutaArchivo.toFile());

                // Guardar nombre de la foto en la entidad
                mascota.setFoto(nombreArchivo);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la foto: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("La foto de la mascota es obligatoria");
        }

        // Guardar mascota en la base de datos
        Mascota mascotaGuardada = mascotaRepositorio.save(mascota);

        // Mapear entidad a DTO y devolver
        return modelMapper.map(mascotaGuardada, MascotaDTO.class);
    }


    @Override
    public MascotaDTO obtenerMascotaPorNombre(String nombre) {
        return null;
    }

    @Override
    public MascotaDTO obtenerMascotaPorId(Long id) {
        Mascota mascota = (Mascota) mascotaRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + id));
        return modelMapper.map(mascota, MascotaDTO.class);
    }

    @Override
    public MascotaDTO actualizarMascota(Long id, MascotaDTO mascotaDTO) {
        if (mascotaRepositorio.existsById(id)) {
            Mascota mascotaExistente = mascotaRepositorio.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + id));

            // Actualizar los campos de la mascota existente
            mascotaExistente.setFoto(mascotaDTO.getFoto());
            mascotaExistente.setNombre(mascotaDTO.getNombre());
            mascotaExistente.setGenero(mascotaDTO.getGenero());
            mascotaExistente.setFechaNacimiento(mascotaDTO.getFechaNacimiento());
            mascotaExistente.setRaza(mascotaDTO.getRaza());

            if (mascotaDTO.getUsuarioId() != null) {
                Usuario usuario = usuarioRepositorio.findById(mascotaDTO.getUsuarioId())
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + mascotaDTO.getUsuarioId()));
                mascotaExistente.setUsuario(usuario);
            }

            // Guardar la mascota actualizada
            mascotaExistente = mascotaRepositorio.save(mascotaExistente);
            return modelMapper.map(mascotaExistente, MascotaDTO.class);
        }else{
            throw new RuntimeException("Mascota no encontrada con ID: " + id);
        }

    }

    @Override
    public void eliminarMascota(Long id) {
        if (mascotaRepositorio.existsById(id)){
            mascotaRepositorio.deleteById(id);
        }else {
            throw  new RuntimeException("Mascota no encontrada con ID: " + id);
        }

    }

    @Override
    public List<MascotaDTO> getAllMascotas() {
        List<Mascota> mascotas=mascotaRepositorio.findAll();
        return mascotas.stream()
                .map(mascota -> modelMapper.map(mascota, MascotaDTO.class))
                .toList();
    }
}
