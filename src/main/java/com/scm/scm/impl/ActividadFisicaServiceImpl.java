package com.scm.scm.impl;

import com.scm.scm.dto.ActividadFisicaDTO;
import com.scm.scm.model.ActividadFisica;
import com.scm.scm.model.Mascota;
import com.scm.scm.repository.ActividadFisicaRepositorio;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.ActividadFisicaService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActividadFisicaServiceImpl implements ActividadFisicaService {
    private final ActividadFisicaRepositorio actividadFisicaRepositorio;
    private final ModelMapper modelMapper;
    private  final MascotaRepositorio mascotaRepositorio;
    private final VeterinarioRepositorio veterinarioRepositorio;

    public ActividadFisicaServiceImpl(ActividadFisicaRepositorio actividadFisicaRepositorio, ModelMapper modelMapper, MascotaRepositorio mascotaRepositorio, VeterinarioRepositorio veterinarioRepositorio) {
        this.actividadFisicaRepositorio = actividadFisicaRepositorio;
        this.modelMapper = modelMapper;
        this.mascotaRepositorio = mascotaRepositorio;
        this.veterinarioRepositorio = veterinarioRepositorio;
    }


    @Override
    public ActividadFisicaDTO crearActividadFisica(ActividadFisicaDTO actividadFisicaDTO) {
        ActividadFisica actividadFisica= modelMapper.map(actividadFisicaDTO , ActividadFisica.class);
        if (actividadFisicaDTO.getMascotaId() !=null){
            Mascota mascota=mascotaRepositorio.findById(actividadFisicaDTO.getMascotaId()).orElseThrow(()-> new RuntimeException("No existe una mascota con el ID: " + actividadFisicaDTO.getMascotaId()));
            actividadFisica.setMascota(mascota);


        }
        if (actividadFisicaDTO.getVeterinarioId()!=null){
            veterinarioRepositorio.findById(actividadFisicaDTO.getVeterinarioId()).orElseThrow(()-> new RuntimeException("No existe un veterinario con el ID: " + actividadFisicaDTO.getVeterinarioId()));
        }
        actividadFisica=actividadFisicaRepositorio.save(actividadFisica);
        return modelMapper.map(actividadFisica, ActividadFisicaDTO.class);

    }

    @Override
    public ActividadFisicaDTO obtenerActividadFisicaPorId(Long id) {
        ActividadFisica actividadFisica=actividadFisicaRepositorio.findById(id).orElseThrow( ()-> new RuntimeException("No existe una actividad fisica con el ID: " + id));

        return modelMapper .map(actividadFisica, ActividadFisicaDTO.class);
    }

    @Override
    public ActividadFisicaDTO actualizarActividadFisica(Long id, ActividadFisicaDTO actividadFisicaDTO) {
        if (actividadFisicaRepositorio.existsById(id)){
            ActividadFisica actividadFisica=actividadFisicaRepositorio.findById(id).orElseThrow(()-> new RuntimeException("No existe una actividad fisica con el ID: " + id));
            actividadFisica.setDescripcion(actividadFisicaDTO.getDescripcion());
            actividadFisica.setTipoActividad(actividadFisicaDTO.getTipoActividad());
            actividadFisica.setFoto(actividadFisicaDTO.getFoto());
            if (actividadFisicaDTO.getMascotaId() !=null){
                Mascota mascota=mascotaRepositorio.findById(actividadFisicaDTO.getMascotaId()).orElseThrow(()-> new RuntimeException("No existe una mascota con el ID: " + actividadFisicaDTO.getMascotaId()));
                actividadFisica.setMascota(mascota);
            }
            if (actividadFisicaDTO.getVeterinarioId()!=null){
                veterinarioRepositorio.findById(actividadFisicaDTO.getVeterinarioId()).orElseThrow(()-> new RuntimeException("No existe un veterinario con el ID: " + actividadFisicaDTO.getVeterinarioId()));
                actividadFisica=actividadFisicaRepositorio.save(actividadFisica);
            }
            actividadFisica=actividadFisicaRepositorio.save(actividadFisica);
            return modelMapper.map(actividadFisica, ActividadFisicaDTO.class);
        }
        else {
            throw new RuntimeException("No existe una actividad fisica con el ID: " + id);
        }
    }

    @Override
    public void eliminarActividadFisica(Long id) {
        if (actividadFisicaRepositorio.existsById(id)){
            actividadFisicaRepositorio.deleteById(id);
        }else {
            throw new RuntimeException("No existe una actividad fisica con el ID: " + id);
        }

    }

    @Override
    public List<ActividadFisicaDTO> encontrartodasLasActividades() {
        List<ActividadFisicaDTO> actividadFisicaDTOS=actividadFisicaRepositorio.findAll() .stream()
                .map(actividadFisica -> modelMapper.map(actividadFisica, ActividadFisicaDTO.class))
                .toList();
        return actividadFisicaDTOS;
    }
}
