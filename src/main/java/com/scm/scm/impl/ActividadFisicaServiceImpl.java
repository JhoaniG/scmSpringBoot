package com.scm.scm.impl;

import com.scm.scm.dto.ActividadFisicaDTO;
import com.scm.scm.dto.ActividadVistaDTO;
import com.scm.scm.model.ActividadFisica;
import com.scm.scm.model.Mascota;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.ActividadFisicaRepositorio;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.ActividadFisicaService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
// Asegúrate de que esta importación esté presente
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActividadFisicaServiceImpl implements ActividadFisicaService {

    private final ActividadFisicaRepositorio actividadFisicaRepositorio;
    private final ModelMapper modelMapper;
    private final MascotaRepositorio mascotaRepositorio;
    private final VeterinarioRepositorio veterinarioRepositorio;

    public ActividadFisicaServiceImpl(ActividadFisicaRepositorio actividadFisicaRepositorio, ModelMapper modelMapper, MascotaRepositorio mascotaRepositorio, VeterinarioRepositorio veterinarioRepositorio) {
        this.actividadFisicaRepositorio = actividadFisicaRepositorio;
        this.modelMapper = modelMapper;
        this.mascotaRepositorio = mascotaRepositorio;
        this.veterinarioRepositorio = veterinarioRepositorio;
    }

    @Override
    public ActividadFisicaDTO crearActividadFisica(ActividadFisicaDTO actividadFisicaDTO) {
        ActividadFisica actividadFisica = modelMapper.map(actividadFisicaDTO, ActividadFisica.class);
        // Lógica para guardar la foto
        MultipartFile archivo = actividadFisicaDTO.getArchivoFoto();
        if (archivo != null && !archivo.isEmpty()) {
            try {
                Path rutaUploads = Paths.get(System.getProperty("user.dir"), "uploads/actividades");
                Files.createDirectories(rutaUploads);
                String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
                Path rutaArchivo = rutaUploads.resolve(nombreArchivo);
                archivo.transferTo(rutaArchivo.toFile());
                actividadFisica.setFoto(nombreArchivo);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la foto: " + e.getMessage());
            }
        } else {
            actividadFisica.setFoto(null);
        }

        // Asociar Mascota y Veterinario
        if (actividadFisicaDTO.getMascotaId() != null) {
            Mascota mascota = mascotaRepositorio.findById(actividadFisicaDTO.getMascotaId())
                    .orElseThrow(() -> new RuntimeException("No existe una mascota con el ID: " + actividadFisicaDTO.getMascotaId()));
            actividadFisica.setMascota(mascota);
        }

        if (actividadFisicaDTO.getVeterinarioId() != null) {
            Veterinario veterinario = veterinarioRepositorio.findById(actividadFisicaDTO.getVeterinarioId())
                    .orElseThrow(() -> new RuntimeException("No existe un veterinario con el ID: " + actividadFisicaDTO.getVeterinarioId()));
            actividadFisica.setVeterinario(veterinario);
        }

        // Al crear, la alerta se activa (vistaPorDueno es 'false' por defecto en la BD)
        actividadFisica = actividadFisicaRepositorio.save(actividadFisica);
        return modelMapper.map(actividadFisica, ActividadFisicaDTO.class);
    }

    @Override
    public ActividadFisicaDTO obtenerActividadFisicaPorId(Long id) {
        ActividadFisica actividadFisica = actividadFisicaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe una actividad fisica con el ID: " + id));
        return modelMapper.map(actividadFisica, ActividadFisicaDTO.class);
    }

    @Override
    public ActividadFisicaDTO actualizarActividadFisica(Long id, ActividadFisicaDTO actividadFisicaDTO) {
        if (actividadFisicaRepositorio.existsById(id)) {
            ActividadFisica actividadFisica = actividadFisicaRepositorio.findById(id)
                    .orElseThrow(() -> new RuntimeException("No existe una actividad fisica con el ID: " + id));
            actividadFisica.setDescripcion(actividadFisicaDTO.getDescripcion());
            actividadFisica.setTipoActividad(actividadFisicaDTO.getTipoActividad());

            // Lógica para actualizar la foto si se envía una nueva
            if (actividadFisicaDTO.getArchivoFoto() != null && !actividadFisicaDTO.getArchivoFoto().isEmpty()) {
                // ... (aquí iría la lógica para guardar la nueva foto)
            } else {
                actividadFisica.setFoto(actividadFisicaDTO.getFoto());
            }

            if (actividadFisicaDTO.getMascotaId() != null) {
                Mascota mascota = mascotaRepositorio.findById(actividadFisicaDTO.getMascotaId())
                        .orElseThrow(() -> new RuntimeException("No existe una mascota con el ID: " + actividadFisicaDTO.getMascotaId()));
                actividadFisica.setMascota(mascota);
            }
            if (actividadFisicaDTO.getVeterinarioId() != null) {
                Veterinario veterinario = veterinarioRepositorio.findById(actividadFisicaDTO.getVeterinarioId())
                        .orElseThrow(() -> new RuntimeException("No existe un veterinario con el ID: " + actividadFisicaDTO.getVeterinarioId()));
                actividadFisica.setVeterinario(veterinario);
            }
            actividadFisica = actividadFisicaRepositorio.save(actividadFisica);
            return modelMapper.map(actividadFisica, ActividadFisicaDTO.class);
        } else {
            throw new RuntimeException("No existe una actividad fisica con el ID: " + id);
        }
    }

    @Override
    public void eliminarActividadFisica(Long id) {
        if (actividadFisicaRepositorio.existsById(id)) {
            actividadFisicaRepositorio.deleteById(id);
        } else {
            throw new RuntimeException("No existe una actividad fisica con el ID: " + id);
        }
    }

    @Override
    public List<ActividadFisicaDTO> encontrartodasLasActividades() {
        List<ActividadFisicaDTO> actividadFisicaDTOS = actividadFisicaRepositorio.findAll().stream()
                .map(actividadFisica -> modelMapper.map(actividadFisica, ActividadFisicaDTO.class))
                .toList();
        return actividadFisicaDTOS;
    }

    // --- ESTE MÉTODO HA SIDO MODIFICADO ---
    @Override
    @Transactional
    public List<ActividadVistaDTO> obtenerActividadesPorMascotaId(Long mascotaId) {
        LocalDate hoy = LocalDate.now();
        List<ActividadFisica> todasLasActividades = actividadFisicaRepositorio.findByMascota_IdMascota(mascotaId);

        // Filtramos solo las activas
        List<ActividadFisica> actividadesActivas = todasLasActividades.stream()
                .filter(actividad -> !actividad.getFechaFin().isBefore(hoy))
                .collect(Collectors.toList());

        // Marcar como visto
        List<ActividadFisica> actividadesParaActualizar = actividadesActivas.stream()
                .filter(actividad -> !actividad.isVistaPorDueno())
                .toList();

        if (!actividadesParaActualizar.isEmpty()) {
            for (ActividadFisica actividad : actividadesParaActualizar) {
                actividad.setVistaPorDueno(true);
            }
            actividadFisicaRepositorio.saveAll(actividadesParaActualizar);
        }

        return actividadesActivas.stream().map(actividad -> {
            ActividadVistaDTO dto = new ActividadVistaDTO();
            dto.setIdActividadFisica(actividad.getIdActividadFisica());
            dto.setDescripcion(actividad.getDescripcion());
            dto.setTipoActividad(actividad.getTipoActividad());
            dto.setFoto(actividad.getFoto());

            // --- ESTO ES LO QUE FALTABA ---
            dto.setFechaInicio(actividad.getFechaInicio());
            dto.setFechaFin(actividad.getFechaFin());
            // ------------------------------

            if (actividad.getMascota() != null) {
                dto.setNombreMascota(actividad.getMascota().getNombre());
            }
            if (actividad.getVeterinario() != null && actividad.getVeterinario().getUsuario() != null) {
                dto.setNombreVeterinario(actividad.getVeterinario().getUsuario().getNombre());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<ActividadFisicaDTO> getAllActividadesPaginadas(Pageable pageable) {
        return actividadFisicaRepositorio.findAll(pageable).map(this::convertirADTO);
    }

    private ActividadFisicaDTO convertirADTO(ActividadFisica actividad) {
        ActividadFisicaDTO dto = modelMapper.map(actividad, ActividadFisicaDTO.class);
        if (actividad.getMascota() != null) {
            dto.setNombreMascota(actividad.getMascota().getNombre());
        }
        if (actividad.getVeterinario() != null && actividad.getVeterinario().getUsuario() != null) {
            dto.setNombreVeterinario(actividad.getVeterinario().getUsuario().getNombre());
        }
        return dto;
    }


    @Override
    public void terminarActividad(Long id) {
        ActividadFisica actividad = actividadFisicaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        // Para "terminar", establecemos la fecha fin a AYER, así el filtro la oculta
        actividad.setFechaFin(LocalDate.now().minusDays(1));
        actividadFisicaRepositorio.save(actividad);
    }

    @Transactional
    @Override
    public List<ActividadVistaDTO> obtenerHistorialActividadesPorMascotaId(Long mascotaId) {
        LocalDate hoy = LocalDate.now();

        // 1. Buscamos todas
        List<ActividadFisica> todas = actividadFisicaRepositorio.findByMascota_IdMascota(mascotaId);

        // 2. Filtramos las que YA TERMINARON (fechaFin < hoy)
        List<ActividadFisica> terminadas = todas.stream()
                .filter(a -> a.getFechaFin().isBefore(hoy))
                .toList();

        // 3. Convertimos a DTO
        return terminadas.stream().map(actividad -> {
            ActividadVistaDTO dto = new ActividadVistaDTO();
            dto.setIdActividadFisica(actividad.getIdActividadFisica());
            dto.setDescripcion(actividad.getDescripcion());
            dto.setTipoActividad(actividad.getTipoActividad());
            dto.setFoto(actividad.getFoto());
            dto.setFechaInicio(actividad.getFechaInicio());
            dto.setFechaFin(actividad.getFechaFin());

            if (actividad.getMascota() != null) {
                dto.setNombreMascota(actividad.getMascota().getNombre());
            }
            if (actividad.getVeterinario() != null && actividad.getVeterinario().getUsuario() != null) {
                dto.setNombreVeterinario(actividad.getVeterinario().getUsuario().getNombre());
            }
            return dto;
        }).collect(Collectors.toList());
    }

}