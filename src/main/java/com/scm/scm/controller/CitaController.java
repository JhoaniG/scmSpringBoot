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
import java.util.Map;

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
    public String crearCita(@ModelAttribute CitaDTO citaDTO,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) { // <-- 3. AÑADE RedirectAttributes

        Veterinario vet = (Veterinario) session.getAttribute("veterinarioSesion");
        if (vet == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Tu sesión ha expirado. Por favor, inicia sesión de nuevo.");
            return "redirect:/login?error=sesionVacia";
        }
        citaDTO.setVeterinarioId(vet.getIdVeterinario());

        try {
            citaService.crearCita(citaDTO);
            // 4. MENSAJE DE ÉXITO
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Cita creada exitosamente!");
        } catch (Exception e) {
            // 5. MENSAJE DE ERROR
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear la cita: " + e.getMessage());
        }

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



    @PostMapping("/citas/terminar/{id}")
    public String terminarCita(@PathVariable("id") Long idCita, RedirectAttributes redirectAttributes) {
        try {
            CitaDTO citaDTO = citaService.obtenerCitaPorId(idCita);
            citaDTO.setEstadoCita("Terminada");
            citaService.actualizarCita(idCita, citaDTO);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cita marcada como terminada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar la cita.");
        }
        return "redirect:/citas"; // Vuelve a la lista de citas
    }

    @GetMapping("/api/historial/mascota/{idMascota}")
    @ResponseBody // <-- ¡Muy importante! Devuelve JSON, no una vista HTML.
    public ResponseEntity<Map<String, Object>> getHistorialCompletoMascota(@PathVariable Long idMascota) {
        try {
            // Reutilizamos el método que ya tenías en tu CitaService
            Map<String, Object> datos = citaService.obtenerDatosHistorialClinico(idMascota);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            // Maneja el caso de que la mascota no se encuentre
            return ResponseEntity.notFound().build();
        }
    }



}
