package com.scm.scm.impl;

import com.scm.scm.dto.CitaDTO;
import com.scm.scm.model.Cita;
import com.scm.scm.model.Diagnosticodueno;
import com.scm.scm.model.Mascota;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.CitaRepositorio;
import com.scm.scm.repository.DiagnosticoDuenoRepositorio;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.CitaService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.IllegalFormatCodePointException;
import java.util.List;

@Service

public class CitaServiceImpl implements CitaService {
    private final CitaRepositorio citaRepositorio;
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
    public void eliminarCita(Long id) {
            if (citaRepositorio.existsById(id)){
                citaRepositorio.deleteById(id);

            }else {
                throw new RuntimeException("No existe una cita con el ID: " + id);
            }

    }

    @Override
    public List<CitaDTO> listarCitas() {
        List<Cita> citas=citaRepositorio.findAll();
        return citas.stream()
                .map(cita -> modelMapper.map(cita, CitaDTO.class))
                .toList();
    }
}
