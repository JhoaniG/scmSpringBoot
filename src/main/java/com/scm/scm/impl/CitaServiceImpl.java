package com.scm.scm.impl;

import com.scm.scm.dto.CitaDTO;
import com.scm.scm.dto.MascotaDTO;
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

    public List<CitaDTO> obtenerCitasPorVeterinario(Long idVeterinario) {
        List<Cita> citas = citaRepositorio.findByVeterinario_IdVeterinario(idVeterinario);

        // Mapear entidad -> DTO
        return citas.stream().map(c -> {
            CitaDTO dto = new CitaDTO();
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
    public Map<String, Object> obtenerDatosHistorialClinico(Long idMascota) {
        Map<String, Object> datos = new HashMap<>();

        // 1. Obtiene la información principal de la mascota
        Mascota mascota = mascotaRepositorio.findById(idMascota)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + idMascota));

        // Mapea la mascota a un DTO para pasarlo a la plantilla
        MascotaDTO mascotaDTO = modelMapper.map(mascota, MascotaDTO.class);
        if (mascota.getUsuario() != null) {
            mascotaDTO.setNombreDueno(mascota.getUsuario().getNombre() + " " + mascota.getUsuario().getApellido());
        }

        // 2. Busca en cada repositorio toda la información relacionada con esa mascota
        List<Cita> citas = citaRepositorio.findByMascota_IdMascota(idMascota);
        List<Diagnosticodueno> diagnosticos = diagnosticoDuenoRepositorio.findByMascota_IdMascota(idMascota);
        List<Dieta> dietas = dietaRepositorio.findByMascota_IdMascota(idMascota);
        List<ActividadFisica> actividades = actividadFisicaRepositorio.findByMascota_IdMascota(idMascota);

        // 3. Pone toda la información recolectada en la "cesta" (el Map)
        datos.put("mascota", mascotaDTO);
        datos.put("citas", citas);
        datos.put("diagnosticos", diagnosticos);
        datos.put("dietas", dietas);
        datos.put("actividades", actividades);

        return datos;
    }
}
