package com.scm.scm.controller;

import com.scm.scm.dto.DiagnosticoDuenoDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.DiagnosticoDuenoService;
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
@RequestMapping("/admin/diagnosticos")
public class AdminDiagnosticoController {

    @Autowired
    private DiagnosticoDuenoService diagnosticoService;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private MascotaRepositorio mascotaRepositorio;
    @Autowired
    private VeterinarioRepositorio veterinarioRepositorio;

    @GetMapping("/listar")
    public String listarDiagnosticos(Model model, @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaDiagnostico").descending());
        Page<DiagnosticoDuenoDTO> paginaDiagnosticos = diagnosticoService.getAllDiagnosticosPaginados(pageable);
        model.addAttribute("diagnosticos", paginaDiagnosticos.getContent());
        model.addAttribute("currentPage", paginaDiagnosticos.getNumber());
        model.addAttribute("totalPages", paginaDiagnosticos.getTotalPages());
        return "admin/diagnosticos/listar";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        model.addAttribute("diagnosticoDTO", new DiagnosticoDuenoDTO());
        model.addAttribute("listaMascotas", mascotaRepositorio.findAll());
        model.addAttribute("listaVeterinarios", veterinarioRepositorio.findAll());
        return "admin/diagnosticos/crear";
    }

    @PostMapping("/crear")
    public String crearDiagnostico(@ModelAttribute DiagnosticoDuenoDTO dto, RedirectAttributes redirectAttributes) {
        try {
            diagnosticoService.crearDiagnosticoDueno(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Diagnóstico creado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/diagnosticos/crear";
        }
        return "redirect:/admin/diagnosticos/listar";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);
        model.addAttribute("diagnosticoDTO", diagnosticoService.obtenerDiagnosticoDuenoPorId(id));
        return "admin/diagnosticos/editar";
    }

    @PostMapping("/editar/{id}")
    public String actualizarDiagnostico(@PathVariable Long id, @ModelAttribute DiagnosticoDuenoDTO dto, RedirectAttributes redirectAttributes) {
        try {
            diagnosticoService.actualizarDiagnosticoDueno(id, dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Diagnóstico actualizado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/diagnosticos/editar/" + id;
        }
        return "redirect:/admin/diagnosticos/listar";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarDiagnostico(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            diagnosticoService.eliminarDiagnosticoDueno(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Diagnóstico eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/diagnosticos/listar";
    }
}