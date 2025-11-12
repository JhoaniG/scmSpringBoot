package com.scm.scm.impl;

import com.scm.scm.dto.*;
import com.scm.scm.model.*;
import com.scm.scm.repository.*;
import com.scm.scm.service.CitaService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

public class CitaServiceImpl implements CitaService {

    @Autowired private DiagnosticoDuenoRepositorio diagnosticoRepositorio;
    @Autowired private DietaRepositorio dietaRepositorio;
    @Autowired private ActividadFisicaRepositorio actividadFisicaRepositorio;
    private final CitaRepositorio citaRepositorio;

    @Override
    public List<CitaDTO> listarCitasPorDueno(Long idUsuario) {
        List<Cita> citas = citaRepositorio.findByDuenoId(idUsuario);
        return citas.stream()
                .map(cita -> {
                    CitaDTO dto = modelMapper.map(cita, CitaDTO.class);
                    dto.setNombreMascota(cita.getMascota().getNombre());
                    dto.setNombreVeterinario(cita.getVeterinario().getUsuario().getNombre() + " " + cita.getVeterinario().getUsuario().getApellido());
                    return dto;
                })
                .toList();
    }

    @Override // <-- Asegúrate de que este método esté en la interfaz CitaService
    public List<CitaDTO> obtenerCitasPorVeterinario(Long idVeterinario) {
        List<Cita> citas = citaRepositorio.findByVeterinario_IdVeterinario(idVeterinario);

        // Mapear entidad -> DTO
        return citas.stream().map(c -> {
            CitaDTO dto = new CitaDTO();

            // --- ESTAS SON LAS LÍNEAS CLAVE QUE FALTABAN ---
            dto.setIdCita(c.getIdCita()); //
            dto.setMascotaId(c.getMascota().getIdMascota()); // <-- Necesario para Dieta
            dto.setDuenoId(c.getMascota().getUsuario().getIdUsuario()); // <-- Necesario para Dieta
            // --- FIN DE LÍNEAS AÑADIDAS ---

            dto.setNombreMascota(c.getMascota().getNombre());
            dto.setNombreDueno(c.getMascota().getUsuario().getNombre());
            dto.setFechaCita(c.getFechaCita());
            dto.setMotivoCita(c.getMotivoCita());
            dto.setEstadoCita(c.getEstadoCita());
            return dto;
        }).toList();
    }

    private CitaDTO convertirADTO(Cita cita) {
        CitaDTO dto = modelMapper.map(cita, CitaDTO.class);
        if (cita.getMascota() != null) {
            dto.setNombreMascota(cita.getMascota().getNombre());
            if (cita.getMascota().getUsuario() != null) {
                dto.setNombreDueno(cita.getMascota().getUsuario().getNombre());
            }
        }
        if (cita.getVeterinario() != null && cita.getVeterinario().getUsuario() != null) {
            dto.setNombreVeterinario(cita.getVeterinario().getUsuario().getNombre());
        }
        return dto;
    }
    @Override
    public Page<CitaDTO> getAllCitasPaginadas(Pageable pageable) {
        return citaRepositorio.findAll(pageable).map(this::convertirADTO);
    }


    private  final ModelMapper modelMapper;
    private final MascotaRepositorio mascotaRepositorio;
    private  final VeterinarioRepositorio veterinarioRepositorio;
    private final DiagnosticoDuenoRepositorio diagnosticoDuenoRepositorio;

    public CitaServiceImpl(CitaRepositorio citaRepositorio, ModelMapper modelMapper, MascotaRepositorio mascotaRepositorio, VeterinarioRepositorio veterinarioRepositorio, DiagnosticoDuenoRepositorio diagnosticoDuenoRepositorio) {
        this.citaRepositorio = citaRepositorio;
        this.modelMapper = modelMapper;
        this.mascotaRepositorio = mascotaRepositorio;
        this.veterinarioRepositorio = veterinarioRepositorio;
        this.diagnosticoDuenoRepositorio = diagnosticoDuenoRepositorio;
    }

    @Override
    public CitaDTO crearCita(CitaDTO citaDTO) {
        Cita cita=modelMapper.map(citaDTO, Cita.class);
        if(citaDTO.getMascotaId() != null){
            Mascota mascota=mascotaRepositorio.findById(citaDTO.getMascotaId()).orElseThrow(() -> new RuntimeException("No existe una mascota con el ID: " + citaDTO.getMascotaId()));
            cita.setMascota(mascota);
        }
        if (citaDTO.getVeterinarioId()!= null){
            Veterinario veterinario=veterinarioRepositorio.findById(citaDTO.getVeterinarioId()).orElseThrow(() -> new RuntimeException("No existe un veterinario con el ID: " + citaDTO.getVeterinarioId()));
            cita.setVeterinario(veterinario);
        }
        if(citaDTO.getDiagnosticoduenoId() != null){
            Diagnosticodueno diagnosticodueno=diagnosticoDuenoRepositorio.findById(citaDTO.getDiagnosticoduenoId()).orElseThrow(() -> new RuntimeException("No existe un diagnóstico de dueño con el ID: " + citaDTO.getDiagnosticoduenoId()));
            cita.setDiagnosticodueno(diagnosticodueno);
        }
        cita.setEstadoCita("Pendiente");
        cita=citaRepositorio.save(cita);
        return modelMapper.map(cita, CitaDTO.class);
    }

    @Override
    public CitaDTO obtenerCitaPorId(Long id) {
        Cita cita = citaRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe una cita con el ID: " + id));
        return modelMapper.map(cita, CitaDTO.class);
    }

    @Override
    public CitaDTO actualizarCita(Long id, CitaDTO citaDTO) {
        if (citaRepositorio.existsById(id)) {
            Cita cita = citaRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe una cita con el ID: " + id));
            cita.setFechaCita(citaDTO.getFechaCita());
            cita.setMotivoCita(citaDTO.getMotivoCita());
            cita.setEstadoCita(citaDTO.getEstadoCita());

            if (citaDTO.getMascotaId() != null) {
                Mascota mascota = mascotaRepositorio.findById(citaDTO.getMascotaId())
                        .orElseThrow(() -> new RuntimeException("No existe una mascota con el ID: " + citaDTO.getMascotaId()));
                cita.setMascota(mascota);
            }

            if (citaDTO.getVeterinarioId() != null) {
                Veterinario veterinario = veterinarioRepositorio.findById(citaDTO.getVeterinarioId())
                        .orElseThrow(() -> new RuntimeException("No existe un veterinario con el ID: " + citaDTO.getVeterinarioId()));
                cita.setVeterinario(veterinario);
            }
            if (citaDTO.getDiagnosticoduenoId() != null) {
                Diagnosticodueno diagnosticodueno = diagnosticoDuenoRepositorio.findById(citaDTO.getDiagnosticoduenoId())
                        .orElseThrow(() -> new RuntimeException("No existe un diagnóstico de dueño con el ID: " + citaDTO.getDiagnosticoduenoId()));
                cita.setDiagnosticodueno(diagnosticodueno);
            }
            cita = citaRepositorio.save(cita);
            return modelMapper.map(cita, CitaDTO.class);
        } else {
            throw new RuntimeException("No existe una cita con el ID: " + id);
        }
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
            throw new RuntimeException("No se puede eliminar esta cita, puede que esté asociada a un diagnóstico u otros registros.");
        }
    }

    @Override
    public List<CitaDTO> listarCitas() {
        List<Cita> citas=citaRepositorio.findAll();
        return citas.stream()
                .map(cita -> modelMapper.map(cita, CitaDTO.class))
                .toList();
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

        // --- INICIO DE LA CORRECCIÓN ---

        // 2. Busca las listas de entidades
        List<Cita> citas = citaRepositorio.findByMascota_IdMascota(idMascota);
        List<Diagnosticodueno> diagnosticos = diagnosticoDuenoRepositorio.findByMascota_IdMascota(idMascota);
        List<Dieta> dietas = dietaRepositorio.findByMascota_IdMascota(idMascota);
        List<ActividadFisica> actividades = actividadFisicaRepositorio.findByMascota_IdMascota(idMascota);

        // 3. Convierte las listas de Entidades a listas de DTOs
        List<CitaDTO> citasDTO = citas.stream()
                .map(this::convertirADTO) // Reutiliza tu método (asumiendo que está en esta clase)
                .collect(Collectors.toList());

        // Mapea los diagnósticos manualmente para asegurarte de que los nombres están
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
                .map(d -> modelMapper.map(d, DietaDTO.class))
                .collect(Collectors.toList());

        List<ActividadFisicaDTO> actividadesDTO = actividades.stream()
                .map(a -> modelMapper.map(a, ActividadFisicaDTO.class))
                .collect(Collectors.toList());

        // 4. Pone los DTOs (objetos planos) en el Map
        datos.put("mascota", mascotaDTO);
        datos.put("citas", citasDTO);
        datos.put("diagnosticos", diagnosticosDTO); // <-- Ahora es una lista de DTOs con nombres
        datos.put("dietas", dietasDTO);
        datos.put("actividades", actividadesDTO);
        // --- FIN DE LA CORRECCIÓN ---

        return datos;
    }
}
