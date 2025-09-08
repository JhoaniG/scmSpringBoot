package com.scm.scm.controller;

import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.dto.VeterinarioDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.VeterinarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller

public class VeterinarioController {
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    private final VeterinarioService    veterinarioService ;
    private  final  VeterinarioRepositorio veterinarioRepositorio;

    public VeterinarioController(VeterinarioRepositorio veterinarioRepositorio, VeterinarioService veterinarioService, VeterinarioRepositorio veterinarioRepositorio1) {

        this.veterinarioService = veterinarioService;
        this.veterinarioRepositorio = veterinarioRepositorio1;
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
    public String veterinarioIndex(Model model, Authentication authentication, HttpSession session) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

        model.addAttribute("usuario", usuario);
        session.setAttribute("usuario", usuario);

// Veterinario para usarlo en citas
        Optional<Veterinario> optVet = veterinarioRepositorio.findByUsuarioId(usuario.getIdUsuario());
        Veterinario vet = optVet.orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
        session.setAttribute("veterinarioSesion", vet);
        return "veterinarios/index";
    }


    @GetMapping("/uploads/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> verFoto(@PathVariable String filename) throws MalformedURLException {
        Path path = Paths.get("uploads").resolve(filename).toAbsolutePath();
        Resource recurso = new UrlResource(path.toUri());
        if (!recurso.exists() || !recurso.isReadable()) {
            throw new RuntimeException("No se pudo leer el archivo: " + filename);
        }
        return ResponseEntity.ok().body(recurso);
    }

}
