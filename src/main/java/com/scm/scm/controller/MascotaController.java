package com.scm.scm.controller;

import com.scm.scm.dto.DiagnosticoDuenoDTO;
import com.scm.scm.dto.MascotaDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.service.MascotaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;
    private final UsuarioRepositorio usuarioRepositorio;

    public MascotaController(MascotaService mascotaService, UsuarioRepositorio usuarioRepositorio) {
        this.mascotaService = mascotaService;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @ModelAttribute
    public void agregarAtributosGlobales(Model model, Authentication auth) {
        model.addAttribute("diagnosticoDTO", new DiagnosticoDuenoDTO());
        model.addAttribute("logueado", auth != null && auth.isAuthenticated());

        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(email);
            optionalUsuario.ifPresent(usuario -> model.addAttribute("usuario", usuario));
        }
    }

    // LISTAR MASCOTAS PARA EL DUEÑO DE MASCOTA
    @GetMapping("/listar")
    public String listarMascotas(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String emailUsuario = auth.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

        List<MascotaDTO> mascotasDelUsuario = mascotaService.obtenerMascotasPorDuenoId(usuario.getIdUsuario());
        model.addAttribute("mascotas", mascotasDelUsuario);

        return "mascotas/listar";
    }

    // CREAR MASCOTAS PARA EL DUEÑO DE MASCOTA
    @GetMapping("/crear")
    public String mostrarFormulario(Model model) {
        model.addAttribute("mascotaDTO", new MascotaDTO());
        return "mascotas/crear";
    }

    @PostMapping("/crear")
    public String crearMascota(@ModelAttribute MascotaDTO mascotaDTO, Authentication auth, RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            String emailUsuario = auth.getName();
            Usuario usuario = usuarioRepositorio.findByEmail(emailUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

            mascotaDTO.setUsuarioId(usuario.getIdUsuario());
            mascotaService.crearMascota(mascotaDTO);

            return "redirect:/mascotas/listar";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la mascota: " + e.getMessage());
            return "redirect:/mascotas/crear";
        }
    }

    // ELIMINAR MASCOTAS PARA EL DUEÑO DE MASCOTA
    @GetMapping("/eliminar/{id}")
    public String eliminarMascota(@PathVariable("id") Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String emailUsuario = auth.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

        try {
            mascotaService.eliminarMascota(id);

            redirectAttributes.addFlashAttribute("mensajeExito", "La mascota ha sido eliminada correctamente.");
            return "redirect:/mascotas/listar";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensajeError", "No puedes eliminar esta mascota porque tiene registros de actividades, dietas o citas asociadas.");
            return "redirect:/mascotas/listar";
        }
    }

    // CREAR MASCOTAS PARA EL VETERINARIO
    @GetMapping("/crearvet")
    public String mostrarFormularioVet(Model model) {
        model.addAttribute("mascotaDTO", new MascotaDTO());
        return "mascotas/crearDos";
    }

    @PostMapping("/crearvet")
    public String crearMascotavet(@ModelAttribute MascotaDTO mascotaDTO, Authentication auth, RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            String emailUsuario = auth.getName();
            Usuario usuario = usuarioRepositorio.findByEmail(emailUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

            mascotaDTO.setUsuarioId(usuario.getIdUsuario());
            mascotaService.crearMascota(mascotaDTO);
            return "redirect:/mascotas/listarvet";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la mascota: " + e.getMessage());
            return "redirect:/mascotas/crearvet";
        }
    }

    // LISTAR MASCOTAS PARA EL VETERINARIO (CORREGIDO)
    @GetMapping("/listarvet")
    public String listarMascotasVet(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String emailUsuario = auth.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

        // Lógica corregida: obtenemos solo las mascotas del usuario (veterinario)
        List<MascotaDTO> mascotasDelUsuario = mascotaService.obtenerMascotasPorDuenoId(usuario.getIdUsuario());
        model.addAttribute("mascotas", mascotasDelUsuario);

        return "mascotas/listarDos";
    }

    // ELIMINAR MASCOTAS PARA EL VETERINARIO
    @GetMapping("/eliminarvet/{id}")
    public String eliminarMascotaVET(@PathVariable("id") Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            mascotaService.eliminarMascota(id);

            redirectAttributes.addFlashAttribute("mensajeExito", "La mascota ha sido eliminada correctamente.");
            return "redirect:/mascotas/listarvet";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensajeError", "No puedes eliminar esta mascota porque tiene registros de actividades, dietas o citas asociadas.");
            return "redirect:/mascotas/listarvet";
        }
    }
}