package com.scm.scm.controller;

import com.scm.scm.dto.CitaDTO;
import com.scm.scm.dto.DiagnosticoDuenoDTO;
import com.scm.scm.service.CitaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping ("/api/citas")
public class CitaController {
    private  final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }
    //listar citas
    @GetMapping("/listar")
    public ResponseEntity<List<CitaDTO>> listarCitas() {
        List<CitaDTO> citas = citaService.listarCitas();
        return ResponseEntity.ok(citas);
    }

    //Crear cita
    @PostMapping("/crear")
    public  ResponseEntity<CitaDTO> crearCita(@RequestBody  CitaDTO citaDTO) {
        CitaDTO createdCita = citaService.crearCita(citaDTO);
        return ResponseEntity.ok(createdCita);
    }


    @PutMapping("/actualizar/{id}")
    public ResponseEntity<CitaDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody CitaDTO citaDTO) {

        CitaDTO updatedTransaction = citaService.actualizarCita(id, citaDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        citaService.eliminarCita(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
