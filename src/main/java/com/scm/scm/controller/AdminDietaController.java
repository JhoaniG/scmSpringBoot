package com.scm.scm.controller;

import com.scm.scm.dto.DietaDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.DietaService;
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
@RequestMapping("/admin/dietas")
public class AdminDietaController {

    @Autowired
    private DietaService dietaService;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private MascotaRepositorio mascotaRepositorio;
    @Autowired
    private VeterinarioRepositorio veterinarioRepositorio;

    @GetMapping("/listar")
    public String listarDietas(Model model, @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "6") int size, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        Pageable pageable = PageRequest.of(page, size);
        Page<DietaDTO> paginaDietas = dietaService.getAllDietasPaginadas(pageable);
        model.addAttribute("dietas", paginaDietas.getContent());
        model.addAttribute("currentPage", paginaDietas.getNumber());
        model.addAttribute("totalPages", paginaDietas.getTotalPages());
        return "admin/dietas/listar";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        model.addAttribute("dietaDTO", new DietaDTO());
        model.addAttribute("listaMascotas", mascotaRepositorio.findAll());
        model.addAttribute("listaVeterinarios", veterinarioRepositorio.findAll());
        return "admin/dietas/crear";
    }

    @PostMapping("/crear")
    public String crearDieta(@ModelAttribute DietaDTO dietaDTO, RedirectAttributes redirectAttributes) {
        try {
            dietaService.crearDieta(dietaDTO);
            redirectAttributes.addFlashAttribute("mensajeExito", "Dieta registrada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/dietas/crear";
        }
        return "redirect:/admin/dietas/listar";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);
        model.addAttribute("dietaDTO", dietaService.obtenerDietaPorId(id));
        return "admin/dietas/editar";
    }

    @PostMapping("/editar/{id}")
    public String actualizarDieta(@PathVariable Long id, @ModelAttribute DietaDTO dietaDTO, RedirectAttributes redirectAttributes) {
        try {
            dietaService.actualizarDieta(id, dietaDTO);
            redirectAttributes.addFlashAttribute("mensajeExito", "Dieta actualizada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/dietas/editar/" + id;
        }
        return "redirect:/admin/dietas/listar";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarDieta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dietaService.eliminarDieta(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Dieta eliminada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/dietas/listar";
    }
}