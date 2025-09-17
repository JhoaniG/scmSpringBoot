package com.scm.scm.controller;

import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.service.PdfGenerationService;
import com.scm.scm.service.UsuarioService;
import com.scm.scm.repository.RolRepositorio;
import com.scm.scm.exceptions.CustomExeception;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;
    private final RolRepositorio rolRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PdfGenerationService  pdfGenerationService;

    @Autowired
    public AdminUsuarioController(UsuarioService usuarioService, RolRepositorio rolRepositorio, UsuarioRepositorio usuarioRepositorio, PdfGenerationService pdfGenerationService) {
        this.usuarioService = usuarioService;
        this.rolRepositorio = rolRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.pdfGenerationService = pdfGenerationService;
    }

    // --- LISTAR USUARIOS CON PAGINACIÓN ---
    @GetMapping("/listar")
    public String listarUsuarios(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "6") int size,
                                 @RequestParam(required = false) String nombre, // <-- Nuevo
                                 @RequestParam(required = false) Long rolId,   // <-- Nuevo
                                 Authentication authentication) {

        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        Pageable pageable = PageRequest.of(page, size);
        // Llama al servicio con los filtros
        Page<UsuarioDTO> paginaUsuarios = usuarioService.findAllUsersPaginated(nombre, rolId, pageable);

        // Devuelve los filtros a la vista para los campos de búsqueda y la paginación
        model.addAttribute("nombreFiltro", nombre);
        model.addAttribute("rolFiltro", rolId);

        // Carga la lista de roles para el dropdown del filtro
        model.addAttribute("roles", rolRepositorio.findAll());

        model.addAttribute("usuarios", paginaUsuarios.getContent());
        model.addAttribute("currentPage", paginaUsuarios.getNumber());
        model.addAttribute("totalPages", paginaUsuarios.getTotalPages());

        return "admin/usuarios/listar";
    }
    // --- CREAR USUARIO (Formulario) ---
    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        model.addAttribute("usuarioDTO", new UsuarioDTO());
        model.addAttribute("roles", rolRepositorio.findAll());
        return "admin/usuarios/crear";
    }

    // --- CREAR USUARIO (Procesar) ---
    @PostMapping("/crear")
    public String crearUsuario(@ModelAttribute UsuarioDTO usuarioDTO, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.createUser(usuarioDTO);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario creado exitosamente.");
        } catch (CustomExeception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/usuarios/crear";
        }
        return "redirect:/admin/usuarios/listar";
    }

    // --- ELIMINAR USUARIO ---
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.deleteUser(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario eliminado exitosamente.");
        } catch (RuntimeException e) { // <-- Cambiamos la excepción que atrapamos
            // Obtenemos el mensaje que definimos en el servicio y lo pasamos a la vista
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/usuarios/listar";
    }
    // Muestra la página para subir el archivo
    @GetMapping("/cargar")
    public String mostrarFormularioCarga(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);
        return "admin/usuarios/cargar";
    }

    // Procesa el archivo subido
    @PostMapping("/cargar")
    public String cargarArchivo(@RequestParam("archivo") MultipartFile archivo, RedirectAttributes redirectAttributes) {
        if (archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Por favor, selecciona un archivo para subir.");
            return "redirect:/admin/usuarios/cargar";
        }
        try {
            usuarioService.cargarUsuariosDesdeExcel(archivo);
            redirectAttributes.addFlashAttribute("mensajeExito", "El archivo se está procesando. Se enviará un reporte por correo electrónico al finalizar.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Ocurrió un error al procesar el archivo: " + e.getMessage());
        }
        return "redirect:/admin/usuarios/listar";
    }

//PEDEF
    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarUsuariosPdf() {
        // 1. Obtenemos la lista de todos los usuarios
        List<UsuarioDTO> usuarios = usuarioService.findAllUsers();

        // 2. Preparamos los datos para la plantilla
        Map<String, Object> datos = new HashMap<>();
        datos.put("usuarios", usuarios);

        // 3. Generamos el PDF usando nuestro servicio reutilizable
        byte[] pdfBytes = pdfGenerationService.generarPdfDesdeHtml("reports/admin-usuarios-template", datos);

        // 4. Preparamos la respuesta para que se descargue
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte_usuarios.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

}