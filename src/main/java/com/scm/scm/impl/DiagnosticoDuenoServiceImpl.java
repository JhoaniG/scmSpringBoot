package com.scm.scm.impl;

import com.scm.scm.dto.DiagnosticoDuenoDTO;
import com.scm.scm.model.Diagnosticodueno;
import com.scm.scm.model.Mascota;
import com.scm.scm.model.Veterinario;
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

import java.util.List;
@Service

public class DiagnosticoDuenoServiceImpl implements DiagnosticoDuenoService {
    private  final DiagnosticoDuenoRepositorio diagnosticoDuenoRepositorio;
    private final ModelMapper modelMapper;
    private  final VeterinarioRepositorio veterinarioRepositorio;
    private final MascotaRepositorio mascotaRepositorio;

    public DiagnosticoDuenoServiceImpl(DiagnosticoDuenoRepositorio diagnosticoDuenoRepositorio, ModelMapper modelMapper, VeterinarioRepositorio veterinarioRepositorio, MascotaRepositorio mascotaRepositorio) {
        this.diagnosticoDuenoRepositorio = diagnosticoDuenoRepositorio;
        this.modelMapper = modelMapper;
        this.veterinarioRepositorio = veterinarioRepositorio;
        this.mascotaRepositorio = mascotaRepositorio;
    }

    @Override

    public DiagnosticoDuenoDTO crearDiagnosticoDueno(DiagnosticoDuenoDTO diagnosticoDuenoDTO) {

        Diagnosticodueno diagnosticodueno=modelMapper.map(diagnosticoDuenoDTO, Diagnosticodueno.class);



        if (diagnosticoDuenoDTO.getVeterinarioId() != null){

            Veterinario veterinario = veterinarioRepositorio.findByUsuarioId(diagnosticoDuenoDTO.getVeterinarioId())

                    .orElseThrow(() -> new RuntimeException("No existe un veterinario con el usuario ID: " + diagnosticoDuenoDTO.getVeterinarioId()));

            diagnosticodueno.setVeterinario(veterinario);

        }

        if (diagnosticoDuenoDTO.getMascotaId() != null){

            Mascota mascota=mascotaRepositorio.findById(diagnosticoDuenoDTO.getMascotaId()).orElseThrow(() -> new RuntimeException("No existe una mascota con el ID: " + diagnosticoDuenoDTO.getMascotaId()));

            diagnosticodueno.setMascota(mascota);

        }







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
    public List<DiagnosticoDuenoDTO> listarDiagnosticosPorVeterinario(Long veterinarioId) {
        List<Diagnosticodueno> diagnosticos = diagnosticoDuenoRepositorio.findByVeterinario_IdVeterinario(veterinarioId);

        return diagnosticos.stream().map(d -> {
            DiagnosticoDuenoDTO dto = modelMapper.map(d, DiagnosticoDuenoDTO.class);

            // Setear nombres
            dto.setNombreM(d.getMascota().getNombre());
            dto.setNombreDueno(d.getMascota().getUsuario().getNombre());

            return dto;
        }).toList();
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
