package com.scm.scm.controller;

import com.scm.scm.dto.ActividadFisicaDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.ActividadFisicaService;
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
@RequestMapping("/admin/actividades")
public class AdminActividadController {

    @Autowired
    private ActividadFisicaService actividadService;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private MascotaRepositorio mascotaRepositorio;
    @Autowired
    private VeterinarioRepositorio veterinarioRepositorio;

    @GetMapping("/listar")
    public String listarActividades(Model model, @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "6") int size, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        Pageable pageable = PageRequest.of(page, size);
        Page<ActividadFisicaDTO> paginaActividades = actividadService.getAllActividadesPaginadas(pageable);
        model.addAttribute("actividades", paginaActividades.getContent());
        model.addAttribute("currentPage", paginaActividades.getNumber());
        model.addAttribute("totalPages", paginaActividades.getTotalPages());
        return "admin/actividades/listar";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        model.addAttribute("actividadDTO", new ActividadFisicaDTO());
        model.addAttribute("listaMascotas", mascotaRepositorio.findAll());
        model.addAttribute("listaVeterinarios", veterinarioRepositorio.findAll());
        return "admin/actividades/crear";
    }

    @PostMapping("/crear")
    public String crearActividad(@ModelAttribute ActividadFisicaDTO dto, RedirectAttributes redirectAttributes) {
        try {
            actividadService.crearActividadFisica(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Actividad registrada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/actividades/crear";
        }
        return "redirect:/admin/actividades/listar";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);
        model.addAttribute("actividadDTO", actividadService.obtenerActividadFisicaPorId(id));
        return "admin/actividades/editar";
    }

    @PostMapping("/editar/{id}")
    public String actualizarActividad(@PathVariable Long id, @ModelAttribute ActividadFisicaDTO dto, RedirectAttributes redirectAttributes) {
        try {
            actividadService.actualizarActividadFisica(id, dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Actividad actualizada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/actividades/editar/" + id;
        }
        return "redirect:/admin/actividades/listar";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarActividad(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            actividadService.eliminarActividadFisica(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Actividad eliminada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/actividades/listar";
    }
}