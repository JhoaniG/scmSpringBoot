package com.scm.scm.impl;

import com.scm.scm.dto.*;
import com.scm.scm.model.*;
import com.scm.scm.repository.*;
import com.scm.scm.service.CitaService;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CitaServiceImpl implements CitaService {

    // --- 1. Dependencias (Inyección por Constructor) ---
    private final CitaRepositorio citaRepositorio;
    private final MascotaRepositorio mascotaRepositorio;
    private final VeterinarioRepositorio veterinarioRepositorio;
    private final DiagnosticoDuenoRepositorio diagnosticoDuenoRepositorio;
    private final DietaRepositorio dietaRepositorio;
    private final ActividadFisicaRepositorio actividadFisicaRepositorio;
    private final ModelMapper modelMapper;

    // Constructor único para todas las dependencias
    public CitaServiceImpl(CitaRepositorio citaRepositorio, ModelMapper modelMapper,
                           MascotaRepositorio mascotaRepositorio, VeterinarioRepositorio veterinarioRepositorio,
                           DiagnosticoDuenoRepositorio diagnosticoDuenoRepositorio,
                           DietaRepositorio dietaRepositorio,
                           ActividadFisicaRepositorio actividadFisicaRepositorio) {
        this.citaRepositorio = citaRepositorio;
        this.modelMapper = modelMapper;
        this.mascotaRepositorio = mascotaRepositorio;
        this.veterinarioRepositorio = veterinarioRepositorio;
        this.diagnosticoDuenoRepositorio = diagnosticoDuenoRepositorio;
        this.dietaRepositorio = dietaRepositorio;
        this.actividadFisicaRepositorio = actividadFisicaRepositorio;
    }

    // --- 2. MÉTODOS PÚBLICOS DEL SERVICIO ---

    @Override
    public List<CitaDTO> listarCitasPorDueno(Long idUsuario) {
        List<Cita> citas = citaRepositorio.findByDuenoId(idUsuario);
        return citas.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    @Override
    public List<CitaDTO> obtenerCitasPorVeterinario(Long idVeterinario) {
        // Usa el método del repositorio que ordena por fecha
        List<Cita> citas = citaRepositorio.findByVeterinario_IdVeterinarioOrderByFechaCitaAsc(idVeterinario);
        return citas.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    @Override
    public Page<CitaDTO> getAllCitasPaginadas(Pageable pageable) {
        return citaRepositorio.findAll(pageable).map(this::convertirADTO);
    }

    @Override
    public CitaDTO obtenerCitaPorId(Long id) {
        Cita cita = citaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe una cita con el ID: " + id));
        return convertirADTO(cita);
    }

    @Override
    @Transactional
    public CitaDTO crearCita(CitaDTO citaDTO) {
        // 1. Convertir fecha y hora del DTO a un solo LocalDateTime
        LocalDateTime fechaYHoraCita;
        try {
            fechaYHoraCita = LocalDateTime.of(citaDTO.getFecha(), citaDTO.getHora());
        } catch (Exception e) {
            throw new RuntimeException("Fecha u hora inválida proporcionada.");
        }

        // --- INICIO DE LA VALIDACIÓN (La parte clave) ---
        // 2. Validar si el horario está ocupado (Usa el ID que el Controller asignó)
        if (citaDTO.getVeterinarioId() != null) {
            boolean citaOcupada = citaRepositorio.existsByVeterinario_IdVeterinarioAndFechaCita(
                    citaDTO.getVeterinarioId(), fechaYHoraCita
            );
            if (citaOcupada) {
                // Lanza un error que el controlador pueda atrapar
                throw new RuntimeException("Horario no disponible: El veterinario ya tiene una cita en esa fecha y hora.");
            }
        }
        // --- FIN DE LA VALIDACIÓN ---

        // 3. Convertir DTO a Entidad (Tu CitaServiceImpl ya debe tener este método)
        Cita cita = convertirAEntidad(citaDTO);

        // 4. Asignar la fecha combinada y el estado
        cita.setFechaCita(fechaYHoraCita);
        cita.setEstadoCita("Pendiente");

        // 5. Guardar
        Cita citaGuardada = citaRepositorio.save(cita);
        return convertirADTO(citaGuardada);
    }

    @Override
    @Transactional
    public CitaDTO actualizarCita(Long id, CitaDTO citaDTO) {
        Cita cita = citaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe una cita con el ID: " + id));

        // Actualizar campos simples
        cita.setMotivoCita(citaDTO.getMotivoCita());
        cita.setEstadoCita(citaDTO.getEstadoCita());

        // Manejo de fecha y hora (si se enviaron)
        if (citaDTO.getFecha() != null && citaDTO.getHora() != null) {
            cita.setFechaCita(LocalDateTime.of(citaDTO.getFecha(), citaDTO.getHora()));
        }
        // Fallback si solo se envió el DTO con la fechaCita de tipo LocalDate
        else if (citaDTO.getFechaCita() != null && cita.getFechaCita() != null) {
            cita.setFechaCita(LocalDateTime.of(citaDTO.getFechaCita(), cita.getFechaCita().toLocalTime()));
        }

        // Re-asignar relaciones
        cita.setMascota(mascotaRepositorio.findById(citaDTO.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada: " + citaDTO.getMascotaId())));
        cita.setVeterinario(veterinarioRepositorio.findById(citaDTO.getVeterinarioId())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado: " + citaDTO.getVeterinarioId())));
        cita.setDiagnosticodueno(diagnosticoDuenoRepositorio.findById(citaDTO.getDiagnosticoduenoId())
                .orElseThrow(() -> new RuntimeException("Diagnóstico no encontrado: " + citaDTO.getDiagnosticoduenoId())));

        Cita citaActualizada = citaRepositorio.save(cita);
        return convertirADTO(citaActualizada);
    }

    @Override
    @Transactional
    public void eliminarCita(Long id) {
        if (!citaRepositorio.existsById(id)) {
            throw new RuntimeException("Cita no encontrada con ID: " + id);
        }
        try {
            citaRepositorio.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("No se puede eliminar esta cita, puede que esté asociada a otros registros.");
        }
    }

    @Override
    public List<CitaDTO> listarCitas() {
        List<Cita> citas = citaRepositorio.findAll();
        return citas.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    @Override
    public List<CitaDTO> listarCitasPorMascota(Long mascotaId) {
        // Usamos la lógica del repositorio que ya estás usando
        List<Cita> citas = citaRepositorio.findByMascota_IdMascota(mascotaId);

        // Mapeamos a DTO (podemos reusar convertirADTO)
        return citas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    @Override
    public Map<String, Object> obtenerDatosHistorialClinico(Long idMascota) {
        Map<String, Object> datos = new HashMap<>();

        Mascota mascota = mascotaRepositorio.findById(idMascota)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + idMascota));
        MascotaDTO mascotaDTO = modelMapper.map(mascota, MascotaDTO.class);
        if (mascota.getUsuario() != null) {
            mascotaDTO.setNombreDueno(mascota.getUsuario().getNombre() + " " + mascota.getUsuario().getApellido());
        }

        List<Cita> citas = citaRepositorio.findByMascota_IdMascota(idMascota);
        List<Diagnosticodueno> diagnosticos = diagnosticoDuenoRepositorio.findByMascota_IdMascota(idMascota);
        List<Dieta> dietas = dietaRepositorio.findByMascota_IdMascota(idMascota);
        List<ActividadFisica> actividades = actividadFisicaRepositorio.findByMascota_IdMascota(idMascota);

        // --- Conversión a DTOs ---
        List<CitaDTO> citasDTO = citas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        // Mapea diagnósticos asegurándote de incluir los nombres
        List<DiagnosticoDuenoDTO> diagnosticosDTO = diagnosticos.stream()
                .map(diag -> {
                    DiagnosticoDuenoDTO dto = modelMapper.map(diag, DiagnosticoDuenoDTO.class);
                    if (diag.getVeterinario() != null && diag.getVeterinario().getUsuario() != null) {
                        dto.setNombreVeterinario(diag.getVeterinario().getUsuario().getNombre() + " " + diag.getVeterinario().getUsuario().getApellido());
                    }
                    if (diag.getMascota() != null && diag.getMascota().getUsuario() != null) {
                        dto.setNombreDueno(diag.getMascota().getUsuario().getNombre());
                        dto.setNombreMascota(diag.getMascota().getNombre());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        List<DietaDTO> dietasDTO = dietas.stream()
                .map(d -> modelMapper.map(d, DietaDTO.class)) // Asumiendo DTO simple
                .collect(Collectors.toList());

        List<ActividadFisicaDTO> actividadesDTO = actividades.stream()
                .map(a -> modelMapper.map(a, ActividadFisicaDTO.class)) // Asumiendo DTO simple
                .collect(Collectors.toList());

        datos.put("mascota", mascotaDTO);
        datos.put("citas", citasDTO);
        datos.put("diagnosticos", diagnosticosDTO);
        datos.put("dietas", dietasDTO);
        datos.put("actividades", actividadesDTO);

        return datos;
    }

    // --- 3. MÉTODOS HELPER PRIVADOS (CENTRALIZADOS Y CORREGIDOS) ---

    private CitaDTO convertirADTO(Cita cita) {
        // Mapea los campos simples que coinciden (ej: motivoCita, estadoCita)
        CitaDTO dto = modelMapper.map(cita, CitaDTO.class);

        // Mapeo Manual de Fechas (el más importante)
        if (cita.getFechaCita() != null) {
            dto.setFechaCita(cita.getFechaCita().toLocalDate()); // Campo LocalDate (para listas)
            dto.setFecha(cita.getFechaCita().toLocalDate());     // Campo LocalDate (para formulario)
            dto.setHora(cita.getFechaCita().toLocalTime());       // Campo LocalTime (para formulario)
        }

        // Mapeo Manual de IDs y Nombres
        dto.setIdCita(cita.getIdCita());
        if (cita.getMascota() != null) {
            dto.setMascotaId(cita.getMascota().getIdMascota());
            dto.setNombreMascota(cita.getMascota().getNombre());
            if (cita.getMascota().getUsuario() != null) {
                dto.setNombreDueno(cita.getMascota().getUsuario().getNombre());
                dto.setDuenoId(cita.getMascota().getUsuario().getIdUsuario());
            }
        }
        if (cita.getVeterinario() != null) {
            dto.setVeterinarioId(cita.getVeterinario().getIdVeterinario());
            if (cita.getVeterinario().getUsuario() != null) {
                dto.setNombreVeterinario(cita.getVeterinario().getUsuario().getNombre() + " " + cita.getVeterinario().getUsuario().getApellido());
            }
        }
        if (cita.getDiagnosticodueno() != null) {
            dto.setDiagnosticoduenoId(cita.getDiagnosticodueno().getIdDiagnosticoDueno());
        }

        return dto;
    }

    private Cita convertirAEntidad(CitaDTO citaDTO) {
        // Creamos una nueva entidad. No usamos ModelMapper para evitar conflictos.
        Cita cita = new Cita();

        // Mapeamos los campos simples manualmente
        cita.setIdCita(citaDTO.getIdCita()); // Si es una actualización, conservará el ID
        cita.setMotivoCita(citaDTO.getMotivoCita());
        cita.setEstadoCita(citaDTO.getEstadoCita());

        // Asignamos las entidades buscando por los IDs del DTO
        cita.setMascota(mascotaRepositorio.findById(citaDTO.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada: " + citaDTO.getMascotaId())));
        cita.setVeterinario(veterinarioRepositorio.findById(citaDTO.getVeterinarioId())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado: " + citaDTO.getVeterinarioId())));
        cita.setDiagnosticodueno(diagnosticoDuenoRepositorio.findById(citaDTO.getDiagnosticoduenoId())
                .orElseThrow(() -> new RuntimeException("Diagnóstico no encontrado: " + citaDTO.getDiagnosticoduenoId())));

        // IMPORTANTE: NO asignamos la fecha aquí.
        // La fecha se asigna en el método 'crearCita' o 'actualizarCita'
        // después de combinar 'fecha' y 'hora'.

        return cita;
    }
}