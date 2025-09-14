package com.scm.scm.controller;

import com.scm.scm.dto.CitaDTO;
import com.scm.scm.dto.DiagnosticoDuenoDTO;
import com.scm.scm.model.Cita;
import com.scm.scm.model.Usuario;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.service.CitaService;
import com.scm.scm.service.VeterinarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class CitaController {
    private final CitaService citaService;
    private final VeterinarioService veterinarioService;
    private final UsuarioRepositorio usuarioRepositorio;

    public CitaController(CitaService citaService, VeterinarioService veterinarioService, UsuarioRepositorio usuarioRepositorio) {
        this.citaService = citaService;
        this.veterinarioService = veterinarioService;
        this.usuarioRepositorio = usuarioRepositorio;
    }
    @ModelAttribute
    public void agregarAtributosGlobales(Model model, Authentication auth) {
        // Siempre agregamos un objeto vacío para que el fragmento no falle
        model.addAttribute("diagnosticoDTO", new DiagnosticoDuenoDTO());

        // Si hay usuario autenticado
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
            model.addAttribute("usuario", usuario);
            model.addAttribute("logueado", true);
        } else {
            model.addAttribute("logueado", false);
        }

        // Inicializamos listas vacías por defecto
        model.addAttribute("mascotas", List.of());
        model.addAttribute("veterinarios", List.of());
    }

    //listar citas
    @GetMapping("/api/citas/listar")
    public ResponseEntity<List<CitaDTO>> listarCitas() {
        List<CitaDTO> citas = citaService.listarCitas();
        return ResponseEntity.ok(citas);
    }

    //Crear cita
    @PostMapping("/api/citas/crear")
    public ResponseEntity<CitaDTO> crearCita(@RequestBody CitaDTO citaDTO) {
        CitaDTO createdCita = citaService.crearCita(citaDTO);
        return ResponseEntity.ok(createdCita);
    }


    @PutMapping("/api/citas/actualizar/{id}")
    public ResponseEntity<CitaDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody CitaDTO citaDTO) {

        CitaDTO updatedTransaction = citaService.actualizarCita(id, citaDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/api/citas/eliminar/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        citaService.eliminarCita(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }


    @PostMapping("/citas/crear")
    public String crearCita(@ModelAttribute CitaDTO citaDTO, HttpSession session, Model model) {
        Veterinario vet = (Veterinario) session.getAttribute("veterinarioSesion");
        citaDTO.setVeterinarioId(vet.getIdVeterinario());

        model.addAttribute("citaDTO", new CitaDTO());

        if (vet == null) {
            // No hay usuario en sesión, redirigir al login o mostrar error
            return "redirect:/login?error=sesionVacia";
        }


        citaService.crearCita(citaDTO);
        return "redirect:/diagnosticos/listar";
    }




    @GetMapping("/duenoMascota/listar")
    public String listarCitasDueno(Authentication authentication, Model model) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<CitaDTO> listaCitas = citaService.listarCitasPorDueno(usuario.getIdUsuario());
        model.addAttribute("listaCitas", listaCitas);

        return "duenoMascota/citas"; // aquí debe ir tu plantilla Thymeleaf
    }



}
