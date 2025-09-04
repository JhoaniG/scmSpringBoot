package com.scm.scm.impl;

import com.scm.scm.dto.DietaDTO;
import com.scm.scm.model.Dieta;
import com.scm.scm.model.Mascota;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.DietaRepositorio;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.DietaService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
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
        Dieta dieta=modelMapper.map(dietaDTO , Dieta.class);

        if (dietaDTO.getMascotaId() != null){
            Mascota mascota=mascotaRepositorio.findById(dietaDTO.getMascotaId()).orElseThrow(()-> new RuntimeException( "No existe una mascota con el ID: " + dietaDTO.getMascotaId()));
            dieta.setMascota(mascota);


        }

        if (dietaDTO.getVeterinarioId()!= null){
            Veterinario veterinario= veterinarioRepositorio.findById(dietaDTO.getVeterinarioId()).orElseThrow(()-> new RuntimeException( "No existe un veterinario con el ID: " + dietaDTO.getVeterinarioId()));
            dieta.setVeterinario(veterinario);


        }

        dieta=dietaRepositorio.save(dieta);


        return modelMapper.map(dieta , DietaDTO.class);
    }

    @Override
    public DietaDTO obtenerDietaPorId(Long id) {
        Dieta dieta=dietaRepositorio.findById(id).orElseThrow(()-> new RuntimeException("No existe una dieta con el ID: " + id));

        return modelMapper.map(dieta, DietaDTO.class);
    }

    @Override
    public DietaDTO actualizarDieta(Long id, DietaDTO dietaDTO) {
        if (dietaRepositorio.existsById(id)){
            Dieta dieta=dietaRepositorio.findById(id).orElseThrow(()-> new RuntimeException("No existe una dieta con el ID: " + id));
            dieta.setDescripcion(dietaDTO.getDescripcion());
            dieta.setTipoDieta(dietaDTO.getTipoDieta());
            dieta.setFoto(dietaDTO.getFoto());
            if (dietaDTO.getMascotaId()!=null){
                Mascota mascota=mascotaRepositorio.findById(dietaDTO.getMascotaId()).orElseThrow(()-> new RuntimeException( "No existe una mascota con el ID: " + dietaDTO.getMascotaId()));
                dieta.setMascota(mascota);

            }
            if (dietaDTO.getVeterinarioId()!=null){
                Veterinario veterinario=veterinarioRepositorio.findById(dietaDTO.getVeterinarioId()).orElseThrow(()-> new RuntimeException( "No existe un veterinario con el ID: " + dietaDTO.getVeterinarioId()));
                dieta.setVeterinario(veterinario);

            }
            dieta=dietaRepositorio.save(dieta);


            return modelMapper.map(dieta, DietaDTO.class);

        }else {
            throw new RuntimeException("No existe una dieta con el ID: " + id);
        }

    }

    @Override
    public void eliminarDieta(Long id) {
        if (dietaRepositorio.existsById(id)){

            dietaRepositorio.deleteById(id);
        }else{
            throw new RuntimeException("No existe una dieta con el ID: " + id);
        }

    }

    @Override
    public DietaDTO obtenerDietaPorMascotaId(Long mascotaId) {
        return null;
    }

    @Override
    public DietaDTO obtenerDietaPorDuenoId(Long duenoId) {
        return null;
    }

    @Override
    public List<DietaDTO> obtenerTodasLasDietas() {
        List<DietaDTO> dietaDTOS=dietaRepositorio.findAll().stream().map(dieta -> modelMapper.map(dieta, DietaDTO.class)).toList();
        return dietaDTOS;
    }
}
