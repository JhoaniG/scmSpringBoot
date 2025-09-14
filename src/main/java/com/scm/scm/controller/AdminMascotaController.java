package com.scm.scm.controller;

import com.scm.scm.dto.MascotaDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.service.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/mascotas")
public class AdminMascotaController {

    @Autowired
    private MascotaService mascotaService;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @GetMapping("/listar")
    public String listarMascotas(Model model, @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "6") int size, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        Pageable pageable = PageRequest.of(page, size);
        Page<MascotaDTO> paginaMascotas = mascotaService.getAllMascotasPaginadas(pageable);
        model.addAttribute("mascotas", paginaMascotas.getContent());
        model.addAttribute("currentPage", paginaMascotas.getNumber());
        model.addAttribute("totalPages", paginaMascotas.getTotalPages());
        return "admin/mascotas/listar";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        model.addAttribute("mascotaDTO", new MascotaDTO());
        // Asumiendo que rolId=2 es para "Dueño de Mascota"
        model.addAttribute("listaDuenos", usuarioRepositorio.findByRol_IdRol(2));
        return "admin/mascotas/crear";
    }

    @PostMapping("/crear")
    public String crearMascota(@ModelAttribute MascotaDTO mascotaDTO, RedirectAttributes redirectAttributes) {
        try {
            mascotaService.crearMascota(mascotaDTO);
            redirectAttributes.addFlashAttribute("mensajeExito", "Mascota registrada exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/mascotas/crear";
        }
        return "redirect:/admin/mascotas/listar";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        model.addAttribute("mascotaDTO", mascotaService.obtenerMascotaPorId(id));
        return "admin/mascotas/editar";
    }

    @PostMapping("/editar/{id}")
    public String actualizarMascota(@PathVariable Long id, @ModelAttribute MascotaDTO mascotaDTO, RedirectAttributes redirectAttributes) {
        try {
            mascotaService.actualizarMascota(id, mascotaDTO);
            redirectAttributes.addFlashAttribute("mensajeExito", "Mascota actualizada exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/mascotas/editar/" + id;
        }
        return "redirect:/admin/mascotas/listar";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarMascota(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            mascotaService.eliminarMascota(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Mascota eliminada exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/mascotas/listar";
    }
}