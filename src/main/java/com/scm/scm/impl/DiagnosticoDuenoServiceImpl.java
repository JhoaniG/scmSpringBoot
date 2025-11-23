package com.scm.scm.impl;

import com.scm.scm.dto.DiagnosticoDuenoDTO;
import com.scm.scm.model.Diagnosticodueno;
import com.scm.scm.model.Mascota;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.CitaRepositorio;
import com.scm.scm.repository.DiagnosticoDuenoRepositorio;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.DiagnosticoDuenoService;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class DiagnosticoDuenoServiceImpl implements DiagnosticoDuenoService {
    private  final DiagnosticoDuenoRepositorio diagnosticoDuenoRepositorio;
    private final ModelMapper modelMapper;
    private  final VeterinarioRepositorio veterinarioRepositorio;
    private final MascotaRepositorio mascotaRepositorio;
    private final CitaRepositorio citaRepositorio;

    public DiagnosticoDuenoServiceImpl(DiagnosticoDuenoRepositorio diagnosticoDuenoRepositorio, ModelMapper modelMapper, VeterinarioRepositorio veterinarioRepositorio, MascotaRepositorio mascotaRepositorio, CitaRepositorio citaRepositorio) {
        this.diagnosticoDuenoRepositorio = diagnosticoDuenoRepositorio;
        this.modelMapper = modelMapper;
        this.veterinarioRepositorio = veterinarioRepositorio;
        this.mascotaRepositorio = mascotaRepositorio;
        this.citaRepositorio = citaRepositorio;
    }

    @Override

    public DiagnosticoDuenoDTO crearDiagnosticoDueno(DiagnosticoDuenoDTO diagnosticoDuenoDTO) {

        Diagnosticodueno diagnosticodueno=modelMapper.map(diagnosticoDuenoDTO, Diagnosticodueno.class);



        if (diagnosticoDuenoDTO.getVeterinarioId() != null) {
            // CORRECTO: Busca por el ID de Veterinario
            Veterinario veterinario = veterinarioRepositorio.findById(diagnosticoDuenoDTO.getVeterinarioId())
                    .orElseThrow(() -> new RuntimeException("No existe un veterinario con el ID: " + diagnosticoDuenoDTO.getVeterinarioId()));
            diagnosticodueno.setVeterinario(veterinario);
        }

        if (diagnosticoDuenoDTO.getMascotaId() != null){

            Mascota mascota=mascotaRepositorio.findById(diagnosticoDuenoDTO.getMascotaId()).orElseThrow(() -> new RuntimeException("No existe una mascota con el ID: " + diagnosticoDuenoDTO.getMascotaId()));

            diagnosticodueno.setMascota(mascota);

        }






        diagnosticodueno.setFechaDiagnostico(LocalDate.now().toString());
        diagnosticodueno=diagnosticoDuenoRepositorio.save(diagnosticodueno);

        return modelMapper.map(diagnosticodueno, DiagnosticoDuenoDTO.class);

    }

    @Override
    public DiagnosticoDuenoDTO obtenerDiagnosticoDuenoPorId(Long id) {
        if (diagnosticoDuenoRepositorio.existsById(id)) {
            Diagnosticodueno diagnosticodueno = diagnosticoDuenoRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe un diagnostico con el ID: " + id));
            return modelMapper.map(diagnosticodueno, DiagnosticoDuenoDTO.class);
        }
        else {
            throw new RuntimeException("No existe un diagnostico con el ID: " + id);
        }
    }

    @Override
    public DiagnosticoDuenoDTO actualizarDiagnosticoDueno(Long id, DiagnosticoDuenoDTO diagnosticoDuenoDTO) {
        if (diagnosticoDuenoRepositorio.existsById(id)) {
            Diagnosticodueno diagnosticodueno = diagnosticoDuenoRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe un diagnostico con el ID: " + id));
            diagnosticodueno.setFechaDiagnostico(diagnosticoDuenoDTO.getFechaDiagnostico());
            diagnosticodueno.setObservaciones(diagnosticoDuenoDTO.getObservaciones());

            if (diagnosticoDuenoDTO.getVeterinarioId() != null) {
                Veterinario veterinario = veterinarioRepositorio.findById(diagnosticoDuenoDTO.getVeterinarioId())
                        .orElseThrow(() -> new RuntimeException("No existe un veterinario con el ID: " + diagnosticoDuenoDTO.getVeterinarioId()));
                diagnosticodueno.setVeterinario(veterinario);

            }
            if (diagnosticoDuenoDTO.getMascotaId()!= null) {
                Mascota mascota = mascotaRepositorio.findById(diagnosticoDuenoDTO.getMascotaId())
                        .orElseThrow(() -> new RuntimeException("No existe una mascota con el ID: " + diagnosticoDuenoDTO.getMascotaId()));
                diagnosticodueno.setMascota(mascota);
            }
            return modelMapper.map(diagnosticoDuenoRepositorio.save(diagnosticodueno), DiagnosticoDuenoDTO.class);
        }else {
            throw new RuntimeException("No existe un diagnostico con el ID: " + id);
        }
    }

    @Override
    @Transactional
    public void eliminarDiagnosticoDueno(Long id) {
        if (!diagnosticoDuenoRepositorio.existsById(id)) {
            throw new RuntimeException("Diagnóstico no encontrado con ID: " + id);
        }
        try {
            // Un diagnóstico puede estar enlazado a una cita, lo que impediría borrarlo.
            diagnosticoDuenoRepositorio.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("No se puede eliminar este diagnóstico porque está asociado a una cita.");
        }
    }
    @Override
    public List<DiagnosticoDuenoDTO> ListartodosDiagnosticosDueno() {
        return List.of();
    }



    @Override
    public Page<DiagnosticoDuenoDTO> listarDiagnosticosPorVeterinario(Long veterinarioId, String filtro, Pageable pageable) {
        Page<Diagnosticodueno> paginaResultados;

        // 1. Decidir qué consulta usar
        if (filtro != null && !filtro.trim().isEmpty()) {
            paginaResultados = diagnosticoDuenoRepositorio.findByVeterinarioAndFiltro(veterinarioId, filtro, pageable);
        } else {
            paginaResultados = diagnosticoDuenoRepositorio.findByVeterinario_IdVeterinario(veterinarioId, pageable);
        }

        // 2. Mapear la página de Entidades a página de DTOs
        return paginaResultados.map(d -> {
            DiagnosticoDuenoDTO dto = modelMapper.map(d, DiagnosticoDuenoDTO.class);

            // A. Nombres
            dto.setNombreM(d.getMascota().getNombre());
            if (d.getMascota().getUsuario() != null) {
                dto.setNombreDueno(d.getMascota().getUsuario().getNombre() + " " + d.getMascota().getUsuario().getApellido());
            }

            // B. IDs
            dto.setMascotaId(d.getMascota().getIdMascota());
            dto.setIdDiagnosticoDueno(d.getIdDiagnosticoDueno());

            // C. Prioridad (si existe en la entidad)
            // dto.setPrioridad(d.getPrioridad()); // Descomentar si la entidad tiene el campo

            // D. Verificar si ya tiene cita agendada
            boolean agendada = citaRepositorio.existsByDiagnosticodueno_IdDiagnosticoDueno(d.getIdDiagnosticoDueno());
            dto.setCitaAgendada(agendada);

            return dto;
        });
    }
    @Override
    public List<DiagnosticoDuenoDTO> listarDiagnosticosPorMascota(Long mascotaId) {
        // Usamos la misma lógica que ya tienes en CitaServiceImpl
        List<Diagnosticodueno> diagnosticos = diagnosticoDuenoRepositorio.findByMascota_IdMascota(mascotaId);

        return diagnosticos.stream()
                .map(diag -> {
                    DiagnosticoDuenoDTO dto = modelMapper.map(diag, DiagnosticoDuenoDTO.class);
                    if (diag.getVeterinario() != null && diag.getVeterinario().getUsuario() != null) {
                        dto.setNombreVeterinario(diag.getVeterinario().getUsuario().getNombre() + " " + diag.getVeterinario().getUsuario().getApellido());
                    }
                    if (diag.getMascota() != null && diag.getMascota().getUsuario() != null) {
                        dto.setNombreDueno(diag.getMascota().getUsuario().getNombre());
                        dto.setNombreMascota(diag.getMascota().getNombre());
                        dto.setPrioridad(diag.getPrioridad());
                        boolean agendada = citaRepositorio.existsByDiagnosticodueno_IdDiagnosticoDueno(diag.getIdDiagnosticoDueno());
                        dto.setCitaAgendada(agendada);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
    private DiagnosticoDuenoDTO convertirADTO(Diagnosticodueno diagnostico) {
        DiagnosticoDuenoDTO dto = modelMapper.map(diagnostico, DiagnosticoDuenoDTO.class);
        if (diagnostico.getMascota() != null) {
            dto.setNombreMascota(diagnostico.getMascota().getNombre());
            if (diagnostico.getMascota().getUsuario() != null) {
                dto.setNombreDueno(diagnostico.getMascota().getUsuario().getNombre());
            }
        }
        if (diagnostico.getVeterinario() != null && diagnostico.getVeterinario().getUsuario() != null) {
            dto.setNombreVeterinario(diagnostico.getVeterinario().getUsuario().getNombre());
        }
        return dto;
    }
    @Override
    public Page<DiagnosticoDuenoDTO> getAllDiagnosticosPaginados(Pageable pageable) {
        return diagnosticoDuenoRepositorio.findAll(pageable).map(this::convertirADTO);
    }

}
