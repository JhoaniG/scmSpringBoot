package com.scm.scm.controller;

import com.scm.scm.model.Rol;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.RolRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin/correos")
public class AdminCorreoController {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private RolRepositorio rolRepositorio;
    @Autowired
    private EmailService emailService;

    @GetMapping("/crear")
    public String mostrarFormularioCorreo(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);

        // Pasamos la lista de roles para el selector
        model.addAttribute("roles", rolRepositorio.findAll());
        return "admin/correos/enviar-correo";
    }

    @PostMapping("/enviar")
    public String enviarCorreosMasivos(@RequestParam Long rolId,
                                       @RequestParam String asunto,
                                       @RequestParam String cuerpo,
                                       RedirectAttributes redirectAttributes) {
        try {
            List<Usuario> destinatarios;
            // Si rolId es 0, enviamos a todos. Si no, filtramos por rol.
            if (rolId == 0) {
                destinatarios = usuarioRepositorio.findAll();
            } else {
                Rol rol = rolRepositorio.findById(rolId).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                destinatarios = usuarioRepositorio.findByRol(rol);
            }

            // Enviamos el correo a cada destinatario
            for (Usuario destinatario : destinatarios) {
                emailService.enviarMensajeSimple(destinatario.getEmail(), asunto, cuerpo);
            }

            redirectAttributes.addFlashAttribute("mensajeExito", "El envío de correos ha comenzado. Se enviarán " + destinatarios.size() + " correos en segundo plano.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al iniciar el envío: " + e.getMessage());
        }

        return "redirect:/admin/correos/crear";
    }
}