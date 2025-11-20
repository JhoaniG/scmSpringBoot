package com.scm.scm.controller;

import com.scm.scm.dto.CalendarioEventoDTO;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;

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
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        try {
            // Busca al veterinario de forma segura
            String email = authentication.getName();
            Usuario usuarioLogueado = usuarioRepositorio.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Veterinario vet = veterinarioService.buscarPorUsuario(usuarioLogueado);

            // Asigna el ID del veterinario al DTO
            citaDTO.setVeterinarioId(vet.getIdVeterinario());

            // Intenta crear la cita
            citaService.crearCita(citaDTO);

            redirectAttributes.addFlashAttribute("mensajeExito", "¡Cita creada exitosamente!");

            // --- LÍNEAS ELIMINADAS ---
            // Ya no pasamos el 'mascotaIdParaAccion' ni 'duenoIdParaAccion' aquí

        } catch (Exception e) {
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

            // --- ¡CAMBIO IMPORTANTE! ---
            // Pasamos los IDs necesarios para los botones de Dieta/Actividad
            redirectAttributes.addFlashAttribute("mensajeExito", "Cita marcada como terminada.");
            redirectAttributes.addFlashAttribute("citaTerminadaId", idCita);
            redirectAttributes.addFlashAttribute("mascotaIdParaAccion", citaDTO.getMascotaId());
            redirectAttributes.addFlashAttribute("duenoIdParaAccion", citaDTO.getDuenoId()); // Asumiendo que tu DTO tiene duenoId
            // --- FIN DEL CAMBIO ---

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

    @GetMapping("/eventos/citas-veterinario")
    @ResponseBody // <-- Devuelve JSON
    public List<CalendarioEventoDTO> getCitasParaCalendario(Authentication authentication) {

        // 1. Obtener el Veterinario de forma segura
        String email = authentication.getName();
        Usuario usuarioLogueado = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Veterinario vet = veterinarioService.buscarPorUsuario(usuarioLogueado);

        // 2. Obtener sus citas (¡Ahora sí vienen con fecha y hora!)
        List<CitaDTO> citasDTO = citaService.obtenerCitasPorVeterinario(vet.getIdVeterinario());

        // 3. Convertir CitaDTO a CalendarioEventoDTO
        List<CalendarioEventoDTO> eventos = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        for (CitaDTO cita : citasDTO) {
            String color;
            String borde;

            // 4. Lógica de Colores
            // AHORA SÍ PODEMOS USAR GETFECHA() Y GETHORA()
            LocalDateTime fechaCitaCompleta = LocalDateTime.of(cita.getFecha(), cita.getHora());

            if ("Terminada".equals(cita.getEstadoCita())) {
                color = "#198754"; // Verde
                borde = "#198754";
            } else if (fechaCitaCompleta.isBefore(ahora)) {
                color = "#dc3545"; // Rojo (Pasada y no terminada)
                borde = "#dc3545";
            } else {
                color = "#0d6efd"; // Azul (Pendiente)
                borde = "#0d6efd";
            }

            eventos.add(new CalendarioEventoDTO(
                    cita.getNombreMascota() + ": " + cita.getMotivoCita(),
                    fechaCitaCompleta,
                    color,
                    borde
            ));
        }
        return eventos;
    }



}
