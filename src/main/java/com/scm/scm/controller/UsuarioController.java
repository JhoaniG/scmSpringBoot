package com.scm.scm.controller;

import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    //Listar metodo

    @GetMapping ("/api/usuarios/listar")
    public ResponseEntity<List<UsuarioDTO>>lsitarUsuarios() {
        List<UsuarioDTO> usuarios = usuarioService.findAllUsers();
        return ResponseEntity.ok(usuarios);
    }

    //crear metodo
    @PostMapping("/api/usuarios/crear")
    public ResponseEntity<UsuarioDTO> crearUsuario(@RequestBody  UsuarioDTO usuarioDTO) {
        UsuarioDTO createdUser = usuarioService.createUser(usuarioDTO);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/api/usuarios/actualizar/{id}")
    public ResponseEntity<UsuarioDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody UsuarioDTO usuarioDTO) {

        UsuarioDTO updatedTransaction = usuarioService.updateUser(id, usuarioDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/api/usuarios/eliminar/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        usuarioService.deleteUser(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

}
