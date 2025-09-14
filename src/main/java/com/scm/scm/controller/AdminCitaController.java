package com.scm.scm.controller;

import com.scm.scm.dto.CitaDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/citas")
public class AdminCitaController {

    @Autowired
    private CitaService citaService;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private MascotaRepositorio mascotaRepositorio;
    @Autowired
    private VeterinarioRepositorio veterinarioRepositorio;

    @GetMapping("/listar")
    public String listarCitas(Model model, @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCita").descending());
        Page<CitaDTO> paginaCitas = citaService.getAllCitasPaginadas(pageable);
        model.addAttribute("citas", paginaCitas.getContent());
        model.addAttribute("currentPage", paginaCitas.getNumber());
        model.addAttribute("totalPages", paginaCitas.getTotalPages());
        return "admin/citas/listar";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        model.addAttribute("citaDTO", new CitaDTO());
        model.addAttribute("listaMascotas", mascotaRepositorio.findAll());
        model.addAttribute("listaVeterinarios", veterinarioRepositorio.findAll());
        return "admin/citas/crear";
    }

    @PostMapping("/crear")
    public String crearCita(@ModelAttribute CitaDTO dto, RedirectAttributes redirectAttributes) {
        try {
            citaService.crearCita(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cita agendada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/citas/crear";
        }
        return "redirect:/admin/citas/listar";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);
        model.addAttribute("citaDTO", citaService.obtenerCitaPorId(id));
        return "admin/citas/editar";
    }

    @PostMapping("/editar/{id}")
    public String actualizarCita(@PathVariable Long id, @ModelAttribute CitaDTO dto, RedirectAttributes redirectAttributes) {
        try {
            citaService.actualizarCita(id, dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cita actualizada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/citas/editar/" + id;
        }
        return "redirect:/admin/citas/listar";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            citaService.eliminarCita(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cita eliminada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/citas/listar";
    }
}