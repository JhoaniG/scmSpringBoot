package com.scm.scm.controller;

import com.scm.scm.dto.DiagnosticoDuenoDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.service.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DuenoMascotaController {
    private  final MascotaService mascotaService;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    public DuenoMascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @GetMapping("/dueno/index")
    public String duenoIndex(Model model, Authentication authentication) {

        // Obtener usuario en sesión
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Pasar mascotas del usuario al modelo
        model.addAttribute("mascotas", mascotaService.getAllMascotas()
                .stream()
                .filter(m -> m.getUsuarioId().equals(usuario.getIdUsuario()))
                .toList());

        // Pasar todos los veterinarios al modelo
        List<Usuario> veterinarios = usuarioRepositorio.findByRolIdRol(3L); // Suponiendo que el ID del rol veterinario es 3
        model.addAttribute("veterinarios", veterinarios);

        model.addAttribute("usuario", usuario);
        model.addAttribute("logueado", true);
        model.addAttribute("diagnosticoDTO", new DiagnosticoDuenoDTO());
        return "duenoMascota/index";
    }
}
