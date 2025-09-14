// En package com.scm.scm.controller;
package com.scm.scm.controller;

import com.scm.scm.dto.VeterinarioDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.service.VeterinarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/veterinarios")
public class AdminVeterinarioController {

    @Autowired
    private VeterinarioService veterinarioService;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio; // Para obtener info del admin logueado

    @GetMapping("/listar")
    public String listarVeterinarios(Model model, @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "6") int size, Authentication authentication) {
        // Obtener info del usuario logueado para el menú
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        // Lógica de paginación
        Pageable pageable = PageRequest.of(page, size);
        Page<VeterinarioDTO> paginaVeterinarios = veterinarioService.getAllVeterinariosPaginados(pageable);

        model.addAttribute("veterinarios", paginaVeterinarios.getContent());
        model.addAttribute("currentPage", paginaVeterinarios.getNumber());
        model.addAttribute("totalPages", paginaVeterinarios.getTotalPages());
        model.addAttribute("totalItems", paginaVeterinarios.getTotalElements());

        return "admin/veterinarios/listar";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarVeterinario(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            veterinarioService.eliminarVeterinario(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Veterinario eliminado exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/veterinarios/listar";
    }

    // --- MOSTRAR FORMULARIO DE CREACIÓN (GET) ---
    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model, Authentication authentication) {
        // Info del admin para la barra lateral
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        // Objeto DTO vacío para el formulario
        model.addAttribute("veterinarioDTO", new VeterinarioDTO());

        // Lista de usuarios con rol "Veterinario" que aún no tienen un perfil de veterinario
        List<Usuario> usuariosVeterinarios = usuarioRepositorio.findUsuariosVeterinariosDisponibles();
        model.addAttribute("listaUsuarios", usuariosVeterinarios);

        return "admin/veterinarios/crear";
    }

    // --- PROCESAR FORMULARIO DE CREACIÓN (POST) ---
    @PostMapping("/crear")
    public String crearVeterinario(@ModelAttribute VeterinarioDTO veterinarioDTO, RedirectAttributes redirectAttributes) {
        try {
            // Validar que se seleccionó un usuario
            if (veterinarioDTO.getUsuarioId() == null) {
                throw new RuntimeException("Debes seleccionar un usuario para crear el perfil de veterinario.");
            }
            veterinarioService.crearVeterinario(veterinarioDTO);
            redirectAttributes.addFlashAttribute("mensajeExito", "Perfil de veterinario creado exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            // Si hay un error, redirigimos de vuelta al formulario de creación
            return "redirect:/admin/veterinarios/crear";
        }

        return "redirect:/admin/veterinarios/listar";
    }

    // --- MOSTRAR FORMULARIO DE EDICIÓN (GET) ---
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Long id, Model model, Authentication authentication) {
        // Info del admin para la barra lateral
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        try {
            // 1. Obtenemos el DTO del veterinario que se va a editar
            VeterinarioDTO veterinarioDTO = veterinarioService.obtenerVeterinarioPorId(id);
            model.addAttribute("veterinarioDTO", veterinarioDTO);
        } catch (RuntimeException e) {
            // Si no se encuentra el ID, redirigimos con un error
            // (Esto es por si alguien modifica la URL manualmente)
            RedirectAttributes redirectAttributes = org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap.class.cast(model);
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/veterinarios/listar";
        }

        return "admin/veterinarios/editar";
    }


    // --- PROCESAR FORMULARIO DE EDICIÓN (POST) ---
    @PostMapping("/editar/{id}")
    public String actualizarVeterinario(@PathVariable("id") Long id,
                                        @ModelAttribute VeterinarioDTO veterinarioDTO,
                                        RedirectAttributes redirectAttributes) {
        try {
            veterinarioService.actualizarVeterinario(id, veterinarioDTO);
            redirectAttributes.addFlashAttribute("mensajeExito", "Perfil de veterinario actualizado exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            // Si hay un error, redirigimos de vuelta al formulario de edición
            return "redirect:/admin/veterinarios/editar/" + id;
        }

        return "redirect:/admin/veterinarios/listar";
    }
}