package com.scm.scm.impl;

import com.scm.scm.dto.DietaDTO;
import com.scm.scm.dto.DietaVistaDTO;
import com.scm.scm.model.Dieta;
import com.scm.scm.model.Mascota;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.DietaRepositorio;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.DietaService;
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
public class DietaServiceImpl implements DietaService {

    private final DietaRepositorio dietaRepositorio;
    private final ModelMapper modelMapper;
    private final MascotaRepositorio mascotaRepositorio;
    private final VeterinarioRepositorio veterinarioRepositorio;

    public DietaServiceImpl(DietaRepositorio dietaRepositorio, ModelMapper modelMapper, MascotaRepositorio mascotaRepositorio, VeterinarioRepositorio veterinarioRepositorio) {
        this.dietaRepositorio = dietaRepositorio;
        this.modelMapper = modelMapper;
        this.mascotaRepositorio = mascotaRepositorio;
        this.veterinarioRepositorio = veterinarioRepositorio;
    }

    @Override
    public DietaDTO crearDieta(DietaDTO dietaDTO) {
        Dieta dieta = modelMapper.map(dietaDTO, Dieta.class);
        // Procesar foto
        MultipartFile archivo = dietaDTO.getArchivoFoto();
        if (archivo != null && !archivo.isEmpty()) {
            try {
                // Carpeta de uploads dentro del proyecto
                Path rutaUploads = Paths.get(System.getProperty("user.dir"), "uploads/dietas");
                Files.createDirectories(rutaUploads);

                // Nombre único para la foto
                String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
                Path rutaArchivo = rutaUploads.resolve(nombreArchivo);

                // Guardar archivo en disco
                archivo.transferTo(rutaArchivo.toFile());
                // Guardar el nombre del archivo en el DTO para el modelo
                dieta.setFoto(nombreArchivo);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la foto: " + e.getMessage());
            }
        } else {
            // Manejar el caso en que la foto no sea obligatoria
            dieta.setFoto(null);
        }

        if (dietaDTO.getMascotaId() != null) {
            Mascota mascota = mascotaRepositorio.findById(dietaDTO.getMascotaId()).orElseThrow(() -> new RuntimeException("No existe una mascota con el ID: " + dietaDTO.getMascotaId()));
            dieta.setMascota(mascota);
        }

        if (dietaDTO.getVeterinarioId() != null) {
            Veterinario veterinario = veterinarioRepositorio.findById(dietaDTO.getVeterinarioId()).orElseThrow(() -> new RuntimeException("No existe un veterinario con el ID: " + dietaDTO.getVeterinarioId()));
            dieta.setVeterinario(veterinario);
        }

        // Al crear, la alerta se activa (vistaPorDueno es 'false' por defecto en la BD)
        dieta = dietaRepositorio.save(dieta);
        return modelMapper.map(dieta, DietaDTO.class);
    }

    @Override
    public DietaDTO obtenerDietaPorId(Long id) {
        Dieta dieta = dietaRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe una dieta con el ID: " + id));
        return modelMapper.map(dieta, DietaDTO.class);
    }

    @Override
    public DietaDTO actualizarDieta(Long id, DietaDTO dietaDTO) {
        if (dietaRepositorio.existsById(id)) {
            Dieta dieta = dietaRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe una dieta con el ID: " + id));
            dieta.setDescripcion(dietaDTO.getDescripcion());
            dieta.setTipoDieta(dietaDTO.getTipoDieta());
            dieta.setFoto(dietaDTO.getFoto()); // Considera si la foto también se actualiza aquí

            if (dietaDTO.getMascotaId() != null) {
                Mascota mascota = mascotaRepositorio.findById(dietaDTO.getMascotaId()).orElseThrow(() -> new RuntimeException("No existe una mascota con el ID: " + dietaDTO.getMascotaId()));
                dieta.setMascota(mascota);
            }
            if (dietaDTO.getVeterinarioId() != null) {
                Veterinario veterinario = veterinarioRepositorio.findById(dietaDTO.getVeterinarioId()).orElseThrow(() -> new RuntimeException("No existe un veterinario con el ID: " + dietaDTO.getVeterinarioId()));
                dieta.setVeterinario(veterinario);
            }
            dieta = dietaRepositorio.save(dieta);
            return modelMapper.map(dieta, DietaDTO.class);
        } else {
            throw new RuntimeException("No existe una dieta con el ID: " + id);
        }
    }

    @Override
    public void eliminarDieta(Long id) {
        if (dietaRepositorio.existsById(id)) {
            dietaRepositorio.deleteById(id);
        } else {
            throw new RuntimeException("No existe una dieta con el ID: " + id);
        }
    }

    @Override
    public DietaDTO obtenerDietaPorMascotaId(Long mascotaId) {
        return null;
        // Aún no implementado
    }

    @Override
    public DietaDTO obtenerDietaPorDuenoId(Long duenoId) {
        return null;
        // Aún no implementado
    }

    @Override
    public List<DietaDTO> obtenerTodasLasDietas() {
        List<DietaDTO> dietaDTOS = dietaRepositorio.findAll().stream().map(dieta -> modelMapper.map(dieta, DietaDTO.class)).toList();
        return dietaDTOS;
    }

    // --- ESTE MÉTODO HA SIDO MODIFICADO ---
    @Override
    @Transactional
    public List<DietaVistaDTO> obtenerDietasPorMascotaId(Long mascotaId) {
        // 1. Obtenemos la fecha de hoy
        LocalDate hoy = LocalDate.now();

        // 2. Obtenemos TODAS las dietas de la mascota
        List<Dieta> todasLasDietas = dietaRepositorio.findByMascota_IdMascota(mascotaId);

        // 3. Filtramos solo las dietas activas
        List<Dieta> dietasActivas = todasLasDietas.stream()
                .filter(dieta -> !dieta.getFechaFin().isBefore(hoy))
                .collect(Collectors.toList());

        // 4. Lógica para marcar como VISTO
        List<Dieta> dietasParaActualizar = dietasActivas.stream()
                .filter(dieta -> !dieta.isVistaPorDueno())
                .toList();

        if (!dietasParaActualizar.isEmpty()) {
            for (Dieta dieta : dietasParaActualizar) {
                dieta.setVistaPorDueno(true);
            }
            dietaRepositorio.saveAll(dietasParaActualizar);
        }

        // 5. Devolvemos solo las dietas ACTIVAS al DTO
        return dietasActivas.stream().map(dieta -> {
            DietaVistaDTO dietaVistaDTO = new DietaVistaDTO();
            dietaVistaDTO.setIdDieta(dieta.getIdDieta());
            dietaVistaDTO.setTipoDieta(dieta.getTipoDieta());
            dietaVistaDTO.setDescripcion(dieta.getDescripcion());
            dietaVistaDTO.setFoto(dieta.getFoto());

            // --- AGREGAR ESTAS DOS LÍNEAS ---
            dietaVistaDTO.setFechaInicio(dieta.getFechaInicio());
            dietaVistaDTO.setFechaFin(dieta.getFechaFin());
            // --------------------------------

            if (dieta.getMascota() != null) {
                dietaVistaDTO.setNombreMascota(dieta.getMascota().getNombre());
            }
            if (dieta.getVeterinario() != null && dieta.getVeterinario().getUsuario() != null) {
                dietaVistaDTO.setNombreVeterinario(dieta.getVeterinario().getUsuario().getNombre());
            }
            return dietaVistaDTO;
        }).collect(Collectors.toList());
    }
    // --- FIN DE LA MODIFICACIÓN ---


    private DietaDTO convertirADTO(Dieta dieta) {
        DietaDTO dto = modelMapper.map(dieta, DietaDTO.class);
        if (dieta.getMascota() != null) {
            dto.setNombreMascota(dieta.getMascota().getNombre());
        }
        if (dieta.getVeterinario() != null && dieta.getVeterinario().getUsuario() != null) {
            dto.setNombreVeterinario(dieta.getVeterinario().getUsuario().getNombre());
        }
        return dto;
    }

    @Override
    public Page<DietaDTO> getAllDietasPaginadas(Pageable pageable) {
        return dietaRepositorio.findAll(pageable).map(this::convertirADTO);
    }




    @Transactional
    @Override
    public List<DietaVistaDTO> obtenerHistorialDietasPorMascotaId(Long mascotaId) {
        LocalDate hoy = LocalDate.now();

        // 1. Buscamos todas
        List<Dieta> todas = dietaRepositorio.findByMascota_IdMascota(mascotaId);

        // 2. Filtramos las que YA TERMINARON (fechaFin < hoy)
        List<Dieta> terminadas = todas.stream()
                .filter(d -> d.getFechaFin().isBefore(hoy))
                .toList();

        // 3. Convertimos a DTO
        return terminadas.stream().map(dieta -> {
            DietaVistaDTO dto = new DietaVistaDTO();
            dto.setIdDieta(dieta.getIdDieta());
            dto.setTipoDieta(dieta.getTipoDieta());
            dto.setDescripcion(dieta.getDescripcion());
            dto.setFoto(dieta.getFoto());
            dto.setFechaInicio(dieta.getFechaInicio());
            dto.setFechaFin(dieta.getFechaFin());

            if (dieta.getMascota() != null) {
                dto.setNombreMascota(dieta.getMascota().getNombre());
            }
            if (dieta.getVeterinario() != null && dieta.getVeterinario().getUsuario() != null) {
                dto.setNombreVeterinario(dieta.getVeterinario().getUsuario().getNombre());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // También necesitamos el método para TERMINAR (cambiar fecha fin a ayer)
    @Override
    public void terminarDieta(Long id) {
        Dieta dieta = dietaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Dieta no encontrada"));

        // Cambiamos fecha fin a ayer para que pase al historial
        dieta.setFechaFin(LocalDate.now().minusDays(1));
        dietaRepositorio.save(dieta);
    }
}