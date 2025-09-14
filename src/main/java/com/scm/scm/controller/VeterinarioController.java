package com.scm.scm.controller;

import com.scm.scm.dto.CitaDTO;
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
    private final VeterinarioService veterinarioService;
    private final VeterinarioRepositorio veterinarioRepositorio;
    private  final com.scm.scm.service.CitaService citaService;

    public VeterinarioController(VeterinarioRepositorio veterinarioRepositorio, VeterinarioService veterinarioService, VeterinarioRepositorio veterinarioRepositorio1, com.scm.scm.service.CitaService citaService) {
        this.veterinarioService = veterinarioService;
        this.veterinarioRepositorio = veterinarioRepositorio1;
        this.citaService = citaService;
    }

    @GetMapping("/api/veterinarios/listar")
    public ResponseEntity<List<VeterinarioDTO>> listarVeterinairos() {
        List<VeterinarioDTO> veterinairos = veterinarioService.getAllVeterinarios();
        return ResponseEntity.ok(veterinairos);
    }

    @PostMapping("/api/veterinarios/crear")
    public ResponseEntity<VeterinarioDTO> crearVeterinario(@RequestBody VeterinarioDTO veterinarioDTO) {
        VeterinarioDTO createdVeterinario = veterinarioService.crearVeterinario(veterinarioDTO);
        return ResponseEntity.ok(createdVeterinario);
    }

    @GetMapping("/api/veterinarios/obtener/{id}")
    public ResponseEntity<VeterinarioDTO> obtenerVeterinarioPorId(Long id) {
        VeterinarioDTO veterinario = veterinarioService.obtenerVeterinarioPorId(id);
        return ResponseEntity.ok(veterinario);
    }

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
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/veterinario/index")
    public String veterinarioIndex(Model model, Authentication authentication, HttpSession session) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

        model.addAttribute("usuario", usuario);
        session.setAttribute("usuario", usuario);

        // Obtener el objeto Veterinario
        Optional<Veterinario> optVet = veterinarioRepositorio.findByUsuarioId(usuario.getIdUsuario());
        Veterinario vet = optVet.orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        // Guardar el ID del veterinario en la sesión
        session.setAttribute("idVeterinario", vet.getIdVeterinario());
        // Guardar el objeto completo si es necesario para otras funcionalidades
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




    @GetMapping("/citas")
    public String listarCitasVeterinario(HttpSession session, Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

        model.addAttribute("usuario", usuario);
        session.setAttribute("usuario", usuario);

        // Recuperar el veterinario desde sesión
        Long idVeterinario = (Long) session.getAttribute("idVeterinario");

        if (idVeterinario == null) {
            return "redirect:/login"; // Si no hay sesión, redirigir
        }

        // Llamar al servicio pasando el id del veterinario
        List<CitaDTO> citas = citaService.obtenerCitasPorVeterinario(idVeterinario);

        model.addAttribute("listaCitas", citas);
        return "veterinarios/citas"; // tu JSP
    }
}