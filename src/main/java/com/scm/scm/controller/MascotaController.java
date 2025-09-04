package com.scm.scm.controller;

import com.scm.scm.dto.MascotaDTO;
import com.scm.scm.dto.RolDTO;
import com.scm.scm.service.MascotaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping ("/api/mascotas")
public class MascotaController {
    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    //Listar masotas
    @GetMapping ("/listar")
    public ResponseEntity<List<MascotaDTO>>getall(){
        List<MascotaDTO> mascotaDTOS=mascotaService.getAllMascotas();
        return  ResponseEntity.ok(mascotaDTOS);
    }
//Crear mascota
    @PostMapping ("/crear")
    public ResponseEntity<MascotaDTO> crearMascota(@RequestBody MascotaDTO mascotaDTO) {
        MascotaDTO nuevaMascota = mascotaService.crearMascota(mascotaDTO);
        return ResponseEntity.ok(nuevaMascota);
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<MascotaDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody MascotaDTO mascotaDTO) {

        MascotaDTO updatedTransaction = mascotaService.actualizarMascota(id, mascotaDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        mascotaService.eliminarMascota(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
