package com.scm.scm.controller;

import com.scm.scm.dto.DietaDTO;
import com.scm.scm.service.DietaService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DietaControllerrest {
    private final DietaService dietaService;

    public DietaControllerrest(DietaService dietaService) {
        this.dietaService = dietaService;
    }

    @GetMapping("/api/dietas/listar")
    public ResponseEntity<List<DietaDTO>> listarDietas() {
        List<DietaDTO> dietas = dietaService.obtenerTodasLasDietas();
        return ResponseEntity.ok(dietas);
    }

    @PostMapping ("/api/dietas/crear")
    public ResponseEntity<DietaDTO> crearDieta(@RequestBody DietaDTO dietaDTO) {
        DietaDTO createdDieta = dietaService.crearDieta(dietaDTO);
        return ResponseEntity.ok(createdDieta);
    }
    @PutMapping("/api/dietas/actualizar/{id}")
    public ResponseEntity<DietaDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody DietaDTO dietaDTO) {

        DietaDTO updatedTransaction = dietaService.actualizarDieta(id, dietaDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/api/dietas/eliminar/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        dietaService.eliminarDieta(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

}
