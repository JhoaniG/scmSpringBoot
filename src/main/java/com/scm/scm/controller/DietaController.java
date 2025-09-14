package com.scm.scm.controller;

import com.scm.scm.dto.*;
import com.scm.scm.model.Usuario;
import com.scm.scm.service.DietaService;
import com.scm.scm.service.MascotaService;
import com.scm.scm.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Controller
@RequestMapping("/dieta")
public class DietaController {

    private final DietaService dietaService;
    private final UsuarioService usuarioService;
    private final MascotaService mascotaService;
    private  final com.scm.scm.repository.UsuarioRepositorio usuarioRepositorio;

    public DietaController(DietaService dietaService, UsuarioService usuarioService, MascotaService mascotaService, com.scm.scm.repository.UsuarioRepositorio usuarioRepositorio) {
        this.dietaService = dietaService;
        this.usuarioService = usuarioService;
        this.mascotaService = mascotaService;
        this.usuarioRepositorio = usuarioRepositorio;
    }
    @ModelAttribute
    public void agregarAtributosGlobales(Model model, Authentication auth) {
        // Siempre agregamos un objeto vacío para que el fragmento no falle
        model.addAttribute("diagnosticoDTO", new DiagnosticoDuenoDTO());

        // Si hay usuario autenticado
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
            model.addAttribute("usuario", usuario);
            model.addAttribute("logueado", true);
        } else {
            model.addAttribute("logueado", false);
        }

        // Inicializamos listas vacías por defecto
        model.addAttribute("mascotas", List.of());
        model.addAttribute("veterinarios", List.of());
    }
    @GetMapping("/seleccionar-dueno")
    public String seleccionarDueno(Model model) {
        List<UsuarioDTO> listaDuenos = usuarioService.obtenerDuenosDeMascota();
        model.addAttribute("listaDuenos", listaDuenos);
        return "veterinarios/seleccionarDueno";
    }

    @GetMapping("/crear/seleccionar-mascota")
    public String seleccionarMascota(@RequestParam("duenoId") Long duenoId, Model model, HttpSession session) {
        Long idVeterinario = (Long) session.getAttribute("idVeterinario");

        // Agrega esta línea para ver si el ID es nulo
        System.out.println("ID del Veterinario en la sesion: " + idVeterinario);

        List<MascotaDTO> listaMascotas = mascotaService.obtenerMascotasPorDuenoId(duenoId);
        model.addAttribute("listaMascotas", listaMascotas);
        model.addAttribute("idVeterinario", idVeterinario);
        return "veterinarios/crearDieta";
    }
    @PostMapping("/crear")
    public String crearDieta(@ModelAttribute DietaDTO dietaDTO,
                             @RequestParam(value = "archivoFoto", required = false) MultipartFile archivoFoto) {
        try {
            // Asigna el archivo al DTO antes de llamar al servicio
            dietaDTO.setArchivoFoto(archivoFoto);
            dietaService.crearDieta(dietaDTO);
        } catch (Exception e) {
            return "redirect:/dieta/seleccionar-dueno?error=true";
        }
        return "redirect:/veterinario/index";
    }



}