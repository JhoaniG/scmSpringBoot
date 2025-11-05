package com.scm.scm.controller;

import com.scm.scm.model.PasswordResetToken;
import com.scm.scm.model.Usuario;
import com.scm.scm.service.EmailService;
import com.scm.scm.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmailService emailService;

    // --- Muestra el formulario para pedir el email ---
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password-form"; // Nombre del archivo HTML
    }

    // --- Procesa la solicitud de recuperación ---
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String userEmail, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Optional<Usuario> userOptional = usuarioService.finUserByEmail(userEmail);

        if (!userOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "No existe una cuenta con ese correo electrónico.");
            return "redirect:/forgot-password";
        }

        Usuario user = userOptional.get();
        String token = UUID.randomUUID().toString();
        usuarioService.CreatePasswordResetTokenForUser(user, token);

        // Construye la URL completa para el enlace de reseteo
        String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        String resetLink = appUrl + "/reset-password?token=" + token;

        // Envía el correo
        String subject = "Recuperación de Contraseña - SCM";
        String text = "Para restablecer tu contraseña, haz clic en el siguiente enlace:\n" + resetLink + "\n\nSi no solicitaste esto, ignora este correo.";
        emailService.enviarMensajeSimple(user.getEmail(), subject, text);

        redirectAttributes.addFlashAttribute("success", "Se ha enviado un enlace de recuperación a tu correo electrónico.");
        return "redirect:/forgot-password";
    }

    // --- Muestra el formulario para ingresar la nueva contraseña ---
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        PasswordResetToken resetToken = usuarioService.getPasswordResetToken(token);
        if (resetToken == null || resetToken.isExpired()) {
            redirectAttributes.addFlashAttribute("error", "El enlace de recuperación es inválido o ha expirado.");
            return "redirect:/login"; // O a forgot-password
        }
        model.addAttribute("token", token);
        return "reset-password-form"; // Nombre del archivo HTML
    }

    // --- Procesa la nueva contraseña ---
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       RedirectAttributes redirectAttributes) {

        PasswordResetToken resetToken = usuarioService.getPasswordResetToken(token);
        if (resetToken == null || resetToken.isExpired()) {
            redirectAttributes.addFlashAttribute("error", "El enlace de recuperación es inválido o ha expirado.");
            return "redirect:/login";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            // Volvemos al formulario, pero mantenemos el token en la URL
            return "redirect:/reset-password?token=" + token;
        }

        Usuario user = resetToken.getUsuario();
        usuarioService.ChangeUserPassword(user, password);

        redirectAttributes.addFlashAttribute("success", "Tu contraseña ha sido actualizada exitosamente. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }
}