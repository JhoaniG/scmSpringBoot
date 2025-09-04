package com.scm.scm.controller;

import com.scm.scm.dto.RolDTO;
import com.scm.scm.dto.VeterinarioDTO;
import com.scm.scm.service.RolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping ("/api/roles")
public class RolController {
    private  final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    //metodo para listar
    @GetMapping("/listar")
    public ResponseEntity<List<RolDTO>>getall(){
        List<RolDTO> rolDTOS =rolService.getAllRoles();
        return ResponseEntity.ok(rolDTOS);
    }

    //metodo para crear
    @PostMapping("/crear")
    public ResponseEntity<RolDTO> crearRol(@RequestBody  RolDTO rolDTO) {
        RolDTO createdRol = rolService.crearRol(rolDTO);
        return ResponseEntity.ok(createdRol);
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<RolDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody RolDTO rolDTO) {

        RolDTO updatedTransaction = rolService.actualizarRol(id, rolDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        rolService.eliminarRol(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
