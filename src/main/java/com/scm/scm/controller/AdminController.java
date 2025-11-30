package com.scm.scm.controller;

import com.scm.scm.model.Usuario;
import com.scm.scm.service.UsuarioService;
import com.scm.scm.service.MascotaService;
import com.scm.scm.service.VeterinarioService;
import com.scm.scm.service.DietaService;
import com.scm.scm.service.ActividadFisicaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioService usuarioService;
    private final MascotaService mascotaService;
    private final VeterinarioService veterinarioService;
    private final DietaService dietaService;
    private final ActividadFisicaService actividadFisicaService;
    private final com.scm.scm.repository.UsuarioRepositorio usuarioRepositorio;
    private final com.scm.scm.repository.SolicitudVeterinarioRepositorio solicitudRepo;

    public AdminController(UsuarioService usuarioService, MascotaService mascotaService, VeterinarioService veterinarioService, DietaService dietaService, ActividadFisicaService actividadFisicaService, com.scm.scm.repository.UsuarioRepositorio usuarioRepositorio, com.scm.scm.repository.SolicitudVeterinarioRepositorio solicitudRepo) {
        this.usuarioService = usuarioService;
        this.mascotaService = mascotaService;
        this.veterinarioService = veterinarioService;
        this.dietaService = dietaService;
        this.actividadFisicaService = actividadFisicaService;
        this.usuarioRepositorio = usuarioRepositorio;
        this.solicitudRepo = solicitudRepo;
    }

    @GetMapping("/index")
    public String adminIndex(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

        model.addAttribute("usuario", usuario);
        // Asegurarse de que el usuario es un administrador
        if (authentication == null || !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Admin"))) {
            return "redirect:/login"; // O redirigir a una página de acceso denegado
        }

        // Obtener la cantidad de registros de cada entidad
        model.addAttribute("totalUsuarios", usuarioService.findAllUsers().size());
        model.addAttribute("totalMascotas", mascotaService.getAllMascotas().size());
        model.addAttribute("totalVeterinarios", veterinarioService.getAllVeterinarios().size());
        model.addAttribute("totalDietas", dietaService.obtenerTodasLasDietas().size());
        model.addAttribute("totalActividades", actividadFisicaService.encontrartodasLasActividades().size());


        long solicitudesPendientes = solicitudRepo.findByEstado("PENDIENTE").size();

        if (solicitudesPendientes > 0) {
            model.addAttribute("notificacionSolicitudes", "Tienes " + solicitudesPendientes + " solicitud(es) de veterinario pendientes de revisión.");
        }

        // Puedes agregar más datos si es necesario
        return "admin/index";
    }
}




