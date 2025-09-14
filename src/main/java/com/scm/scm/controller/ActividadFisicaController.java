package com.scm.scm.controller;

import com.scm.scm.dto.ActividadFisicaDTO;
import com.scm.scm.dto.MascotaDTO;
import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.service.ActividadFisicaService;
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
@RequestMapping("/actividad")
public class ActividadFisicaController {
    private final com.scm.scm.repository.UsuarioRepositorio usuarioRepositorio;

    private final ActividadFisicaService actividadFisicaService;
    private final UsuarioService usuarioService;
    private final MascotaService mascotaService;

    public ActividadFisicaController(com.scm.scm.repository.UsuarioRepositorio usuarioRepositorio, ActividadFisicaService actividadFisicaService, UsuarioService usuarioService, MascotaService mascotaService) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.actividadFisicaService = actividadFisicaService;
        this.usuarioService = usuarioService;
        this.mascotaService = mascotaService;
    }

    @GetMapping("/seleccionar-dueno")
    public String seleccionarDueno(Model model,HttpSession session, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

        model.addAttribute("usuario", usuario);
        session.setAttribute("usuario", usuario);

        List<UsuarioDTO> listaDuenos = usuarioService.obtenerDuenosDeMascota();
        model.addAttribute("listaDuenos", listaDuenos);
        return "veterinarios/seleccionarDuenoActividad";
    }

    @GetMapping("/crear/seleccionar-mascota")
    public String seleccionarMascota(@RequestParam("duenoId") Long duenoId, Model model, HttpSession session, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

        model.addAttribute("usuario", usuario);
        session.setAttribute("usuario", usuario);
        Long idVeterinario = (Long) session.getAttribute("idVeterinario");

        List<MascotaDTO> listaMascotas = mascotaService.obtenerMascotasPorDuenoId(duenoId);

        model.addAttribute("listaMascotas", listaMascotas);
        model.addAttribute("idVeterinario", idVeterinario);
        return "veterinarios/crearActividadFisica";
    }

    @PostMapping("/crear")
    public String crearActividad(@ModelAttribute ActividadFisicaDTO actividadFisicaDTO,
                                 @RequestParam(value = "archivoFoto", required = false) MultipartFile archivoFoto) {
        try {
            actividadFisicaDTO.setArchivoFoto(archivoFoto);
            actividadFisicaService.crearActividadFisica(actividadFisicaDTO);
        } catch (Exception e) {
            return "redirect:/actividad/seleccionar-dueno?error=true";
        }
        return "redirect:/veterinario/index";
    }
}