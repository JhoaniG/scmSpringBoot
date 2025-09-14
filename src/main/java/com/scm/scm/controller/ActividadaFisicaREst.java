package com.scm.scm.controller;

import com.scm.scm.dto.ActividadFisicaDTO;
import com.scm.scm.service.ActividadFisicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actividadFisica")
public class ActividadaFisicaREst {
    private  final ActividadFisicaService actividadFisicaService;

    public ActividadaFisicaREst(ActividadFisicaService actividadFisicaService) {
        this.actividadFisicaService = actividadFisicaService;
    }

    @GetMapping("/listar")
    public ResponseEntity <List<ActividadFisicaDTO>>listarActividades(){
        List<ActividadFisicaDTO> actividades= actividadFisicaService.encontrartodasLasActividades();
        return ResponseEntity.ok(actividades);
    }


    @PostMapping("/crear")
    public ResponseEntity<ActividadFisicaDTO> crearActividadFisica(@RequestBody ActividadFisicaDTO actividadFisicaDTO){
        ActividadFisicaDTO createdActividad= actividadFisicaService.crearActividadFisica(actividadFisicaDTO);
        return ResponseEntity.ok(createdActividad);

    }


    @PutMapping("/actualizar/{id}")
    public ResponseEntity<ActividadFisicaDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody ActividadFisicaDTO actividadFisicaDTO) {

        ActividadFisicaDTO updatedTransaction = actividadFisicaService.actualizarActividadFisica(id, actividadFisicaDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        actividadFisicaService.eliminarActividadFisica(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
