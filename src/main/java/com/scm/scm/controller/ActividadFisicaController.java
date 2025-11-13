package com.scm.scm.controller;

import com.scm.scm.dto.ActividadFisicaDTO;
import com.scm.scm.dto.DiagnosticoDuenoDTO; // <-- Importado
import com.scm.scm.dto.MascotaDTO;
import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.model.Veterinario; // <-- Importado
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio; // <-- Importado
import com.scm.scm.service.ActividadFisicaService;
import com.scm.scm.service.MascotaService;
import com.scm.scm.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired; // <-- Importado
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // <-- Importado
import org.springframework.validation.BindingResult; // <-- IMPORTAR
import jakarta.validation.Valid; // <-- IMPORTAR
import java.util.List;

@Controller
@RequestMapping("/actividad") // Ruta base para todo lo de actividad
public class ActividadFisicaController {

    // --- Inyecciones (Añadido @Autowired para claridad) ---
    @Autowired private ActividadFisicaService actividadFisicaService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private MascotaService mascotaService;
    @Autowired private UsuarioRepositorio usuarioRepositorio;
    @Autowired private VeterinarioRepositorio veterinarioRepositorio; // <-- Inyección añadida

    // (El constructor se puede eliminar si usas @Autowired en los campos)

    // --- AÑADIDO: Carga datos globales para la barra lateral ---
    @ModelAttribute
    public void agregarAtributosGlobales(Model model, Authentication auth) {
        model.addAttribute("diagnosticoDTO", new DiagnosticoDuenoDTO());
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
            model.addAttribute("usuario", usuario);
            model.addAttribute("logueado", true);
        } else {
            model.addAttribute("logueado", false);
        }
        // (Estos se pueden dejar vacíos, el controlador específico los llenará)
        model.addAttribute("mascotas", List.of());
        model.addAttribute("veterinarios", List.of());
    }

    // --- MÉTODO 'seleccionarDueno' MODIFICADO ---
    @GetMapping("/seleccionar-dueno")
    public String seleccionarDueno(Model model, Authentication auth) {

        // 1. Obtener el ID del Veterinario logueado
        String email = auth.getName();
        Usuario usuarioLogueado = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Asumo que tienes un método findByUsuario en VeterinarioRepositorio
        Veterinario veterinario = veterinarioRepositorio.findByUsuario(usuarioLogueado)
                .orElseThrow(() -> new RuntimeException("Perfil de veterinario no encontrado"));
        Long veterinarioId = veterinario.getIdVeterinario();

        // 2. Obtener SÓLO los dueños con citas terminadas
        List<UsuarioDTO> listaDuenos = usuarioService.obtenerDuenosConCitasTerminadas(veterinarioId);

        model.addAttribute("listaDuenos", listaDuenos);

        // 3. Mensaje si la lista está vacía
        if(listaDuenos.isEmpty()) {
            model.addAttribute("mensajeInfo", "Aún no tienes pacientes con citas terminadas. Marca una cita como 'Terminada' para poder asignarle una actividad.");
        }

        return "veterinarios/seleccionarDuenoActividad"; // Vista de selección
    }

    // --- MÉTODO 'seleccionarMascota' MODIFICADO (para más robustez) ---
    @GetMapping("/crear/seleccionar-mascota")
    public String seleccionarMascota(@RequestParam("duenoId") Long duenoId, Model model, Authentication authentication) {

        String email = authentication.getName();
        Usuario usuarioLogueado = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Veterinario veterinario = veterinarioRepositorio.findByUsuario_IdUsuario(usuarioLogueado.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Perfil de veterinario no encontrado"));

        model.addAttribute("idVeterinario", veterinario.getIdVeterinario());
        List<MascotaDTO> listaMascotas = mascotaService.obtenerMascotasPorDuenoId(duenoId);
        model.addAttribute("listaMascotas", listaMascotas);

        // --- ¡NUEVO! Pasamos un DTO vacío para el th:object ---
        ActividadFisicaDTO actividadDTO = new ActividadFisicaDTO();
        actividadDTO.setVeterinarioId(veterinario.getIdVeterinario()); // Pre-llenamos
        model.addAttribute("actividadFisicaDTO", actividadDTO);

        return "veterinarios/crearActividadFisica";
    }

    @PostMapping("/crear")
    public String crearActividad(
            @Valid @ModelAttribute("actividadFisicaDTO") ActividadFisicaDTO actividadFisicaDTO, // 1. Añade @Valid
            BindingResult result, // 2. Añade BindingResult
            @RequestParam(value = "archivoFoto", required = false) MultipartFile archivoFoto,
            RedirectAttributes redirectAttributes,
            Model model, // 3. Añade Model
            Authentication authentication) { // 4. Añade Authentication

        // 5. LÓGICA DE VALIDACIÓN
        if (result.hasErrors()) {
            // Si hay errores, volvemos al formulario

            if (actividadFisicaDTO.getMascotaId() != null) {
                Long duenoId = mascotaService.obtenerMascotaPorId(actividadFisicaDTO.getMascotaId()).getUsuarioId();
                List<MascotaDTO> listaMascotas = mascotaService.obtenerMascotasPorDuenoId(duenoId);
                model.addAttribute("listaMascotas", listaMascotas);
            } else {
                model.addAttribute("listaMascotas", List.of());
            }

            String email = authentication.getName();
            Usuario usuarioLogueado = usuarioRepositorio.findByEmail(email).orElseThrow();
            Veterinario veterinario = veterinarioRepositorio.findByUsuario_IdUsuario(usuarioLogueado.getIdUsuario()).orElseThrow();
            model.addAttribute("idVeterinario", veterinario.getIdVeterinario());

            return "veterinarios/crearActividadFisica"; // Devuelve la VISTA
        }

        try {
            actividadFisicaDTO.setArchivoFoto(archivoFoto);
            actividadFisicaService.crearActividadFisica(actividadFisicaDTO);

            redirectAttributes.addFlashAttribute("mensajeExito", "¡Actividad creada! Puede registrar otra.");
            MascotaDTO mascota = mascotaService.obtenerMascotaPorId(actividadFisicaDTO.getMascotaId());
            Long duenoId = mascota.getUsuarioId();
            redirectAttributes.addAttribute("duenoId", duenoId);

            return "redirect:/actividad/crear/seleccionar-mascota";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear la actividad: " + e.getMessage());
            return "redirect:/actividad/seleccionar-dueno";
        }
    }
}