package com.scm.scm.controller;

import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.dto.VeterinarioDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.VeterinarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller

public class VeterinarioController {
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    private final VeterinarioService    veterinarioService ;

    public VeterinarioController(VeterinarioRepositorio veterinarioRepositorio, VeterinarioService veterinarioService) {

        this.veterinarioService = veterinarioService;
    }

    //LISTAR VETERINARIOS
    @GetMapping( "/api/veterinarios/listar")
    public ResponseEntity<List<VeterinarioDTO>>listarVeterinairos(){
        List<VeterinarioDTO> veterinairos= veterinarioService.getAllVeterinarios();
        return  ResponseEntity.ok(veterinairos);
    }

    //CREAR VETERINARIO
  @PostMapping("/api/veterinarios/crear")
    public ResponseEntity <VeterinarioDTO> crearVeterinario(@RequestBody  VeterinarioDTO veterinarioDTO) {
        VeterinarioDTO createdVeterinario = veterinarioService.crearVeterinario(veterinarioDTO);
        return ResponseEntity.ok(createdVeterinario);
    }
    //OBTENER VETERINARIO POR ID
    @GetMapping("/api/veterinarios/obtener/{id}")
    public ResponseEntity<VeterinarioDTO> obtenerVeterinarioPorId(Long id) {
        VeterinarioDTO veterinario = veterinarioService.obtenerVeterinarioPorId(id);
        return ResponseEntity.ok(veterinario); }


    @PutMapping("/api/veterinarios/actualizar/{id}")
    public ResponseEntity<VeterinarioDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody VeterinarioDTO veterinarioDTO) {

        VeterinarioDTO updatedTransaction = veterinarioService.actualizarVeterinario(id, veterinarioDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/api/veterinarios/eliminar/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        veterinarioService.eliminarVeterinario(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
    @GetMapping("/veterinario/index")
    public String veterinarioIndex(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email);

        model.addAttribute("usuario", usuario);
        return "veterinarios/index";
    }
}
