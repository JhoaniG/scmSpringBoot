package com.scm.scm.controller;

import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.exceptions.CustomExeception;
import com.scm.scm.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
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
    public ResponseEntity<?> crearUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        try {
            UsuarioDTO createdUser = usuarioService.createUser(usuarioDTO);
            return ResponseEntity.ok(createdUser);
        } catch (CustomExeception ex) {
            // devolvemos el mensaje de error en el body
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            // manejamos errores inesperados
            return ResponseEntity.status(500).body("Error interno del servidor: " + ex.getMessage());
        }
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



    @PostMapping("/usuarios/create")
    public String saveUsuario(
            @ModelAttribute("usuarioDTO") UsuarioDTO usuarioDTO,
            BindingResult result,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            return "Modal/RegisterUsuarioModal";
        }

        try {
            usuarioService.createUser(usuarioDTO);
            redirect.addFlashAttribute("success", "Usuario creado con éxito. Ahora inicia sesión.");
            return "redirect:/login";
        } catch (CustomExeception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/home"; // vuelve al home con el modal
        } catch (Exception ex) {
            ex.printStackTrace(); // imprime la excepción completa en consola
            redirect.addFlashAttribute("error", "Error al crear el usuario: " + ex.getMessage());
            return "redirect:/home";
        }
    }

}
