package com.scm.scm.impl;

import com.scm.scm.dto.MascotaDTO;
import com.scm.scm.model.Mascota;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.service.MascotaService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
// Asegúrate de que esta importación esté presente
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MascotaServiceImpl implements MascotaService {

    private final MascotaRepositorio mascotaRepositorio;
    private final ModelMapper modelMapper;
    private final UsuarioRepositorio usuarioRepositorio;
    private final Path rootLocation = Paths.get(System.getProperty("user.dir"), "uploads", "mascotas");

    @Autowired
    public MascotaServiceImpl(MascotaRepositorio mascotaRepositorio, ModelMapper modelMapper, UsuarioRepositorio usuarioRepositorio) {
        this.mascotaRepositorio = mascotaRepositorio;
        this.modelMapper = modelMapper;
        this.usuarioRepositorio = usuarioRepositorio;
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el almacenamiento de archivos.", e);
        }
    }

    private String guardarFoto(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        try {
            String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
            Files.copy(archivo.getInputStream(), this.rootLocation.resolve(nombreArchivo));
            return nombreArchivo;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la foto: " + e.getMessage());
        }
    }

    private void eliminarFoto(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isEmpty()) return;
        try {
            Path archivo = rootLocation.resolve(nombreArchivo);
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            // Opcional: registrar el error pero no detener la operación
            System.err.println("Error al eliminar la foto: " + e.getMessage());
        }
    }

    // --- ESTE MÉTODO HA SIDO MODIFICADO ---
    private MascotaDTO convertirADTO(Mascota mascota) {
        MascotaDTO dto = modelMapper.map(mascota, MascotaDTO.class);
        if (mascota.getUsuario() != null) {
            dto.setNombreDueno(mascota.getUsuario().getNombre() + " " + mascota.getUsuario().getApellido());
        }

        // --- LÓGICA DE ALERTA AÑADIDA ---
        // (Esto funciona gracias al @Transactional en los métodos que llaman a convertirADTO)

        // 1. Revisa las Dietas
        if (mascota.getDietas() != null) {
            // anyMatch es eficiente, deja de buscar tan pronto encuentra una
            boolean nuevasDietas = mascota.getDietas().stream()
                    .anyMatch(dieta -> !dieta.isVistaPorDueno());
            dto.setTieneNuevasDietas(nuevasDietas);
        } else {
            // Si la lista es nula (aún no se ha cargado o no tiene), no hay nuevas dietas
            dto.setTieneNuevasDietas(false);
        }

        // 2. Revisa las Actividades Físicas
        if (mascota.getActividadesFisicas() != null) {
            boolean nuevasActividades = mascota.getActividadesFisicas().stream()
                    .anyMatch(actividad -> !actividad.isVistaPorDueno());
            dto.setTieneNuevasActividades(nuevasActividades);
        } else {
            dto.setTieneNuevasActividades(false);
        }
        // --- FIN DE LA LÓGICA DE ALERTA ---

        return dto;
    }
    // --- FIN DE LA MODIFICACIÓN ---

    @Override
    @Transactional
    public MascotaDTO crearMascota(MascotaDTO mascotaDTO) {
        Mascota mascota = modelMapper.map(mascotaDTO, Mascota.class);
        Usuario usuario = usuarioRepositorio.findById(mascotaDTO.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + mascotaDTO.getUsuarioId()));
        mascota.setUsuario(usuario);

        if (mascotaDTO.isEsAdoptado()) {
            LocalDate fechaNacimientoAprox = LocalDate.now().minusYears(mascotaDTO.getEdadEstimadaAnios());
            mascota.setFechaNacimiento(fechaNacimientoAprox);
        } else {
            mascota.setFechaNacimiento(mascotaDTO.getFechaNacimiento());
        }

        String nombreFoto = guardarFoto(mascotaDTO.getArchivoFoto());
        mascota.setFoto(nombreFoto);

        Mascota mascotaGuardada = mascotaRepositorio.save(mascota);
        return convertirADTO(mascotaGuardada);
    }

    @Override
    public MascotaDTO obtenerMascotaPorId(Long id) {
        Mascota mascota = mascotaRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + id));
        return convertirADTO(mascota);
    }

    @Override
    @Transactional
    public MascotaDTO actualizarMascota(Long id, MascotaDTO mascotaDTO) {
        Mascota mascotaExistente = mascotaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + id));
        mascotaExistente.setNombre(mascotaDTO.getNombre());
        mascotaExistente.setGenero(mascotaDTO.getGenero());
        mascotaExistente.setFechaNacimiento(mascotaDTO.getFechaNacimiento());
        mascotaExistente.setRaza(mascotaDTO.getRaza());

        if (mascotaDTO.getArchivoFoto() != null && !mascotaDTO.getArchivoFoto().isEmpty()) {
            eliminarFoto(mascotaExistente.getFoto());
            String nuevoNombreFoto = guardarFoto(mascotaDTO.getArchivoFoto());
            mascotaExistente.setFoto(nuevoNombreFoto);
        }

        Mascota mascotaActualizada = mascotaRepositorio.save(mascotaExistente);
        return convertirADTO(mascotaActualizada);
    }

    @Override
    @Transactional
    public void eliminarMascota(Long id) {
        Mascota mascota = mascotaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + id));
        try {
            mascotaRepositorio.delete(mascota);
            eliminarFoto(mascota.getFoto());
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("No se puede eliminar esta mascota porque tiene citas asociadas.");
        }
    }

    @Override
    public Page<MascotaDTO> getAllMascotasPaginadas(Pageable pageable) {
        return mascotaRepositorio.findAll(pageable).map(this::convertirADTO);
    }

    // --- ESTE MÉTODO HA SIDO MODIFICADO ---
    @Override
    @Transactional(readOnly = true) // <-- AÑADIDA ANOTACIÓN
    public List<MascotaDTO> getAllMascotas() {
        return mascotaRepositorio.findAll().stream().map(this::convertirADTO).collect(Collectors.toList());
    }
    // --- FIN DE LA MODIFICACIÓN ---


    // --- ESTE MÉTODO HA SIDO MODIFICADO ---
    @Override
    @Transactional(readOnly = true) // <-- AÑADIDA ANOTACIÓN
    public List<MascotaDTO> obtenerMascotasPorDuenoId(Long duenoId) {
        return mascotaRepositorio.findByUsuario_IdUsuario(duenoId).stream().map(this::convertirADTO).collect(Collectors.toList());
    }
    // --- FIN DE LA MODIFICACIÓN ---

    @Override
    public MascotaDTO obtenerMascotaPorNombre(String nombre) { return null; }
}