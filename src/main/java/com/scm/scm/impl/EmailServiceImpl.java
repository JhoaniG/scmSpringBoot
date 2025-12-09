package com.scm.scm.impl;

import com.scm.scm.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    // Aquí ponemos TU CORREO EXACTO, el que validaste en Brevo
    private final String remitente = "seguimientoycotroldemascotassc@gmail.com";

    @Async
    @Override
    public void enviarMensajeSimple(String para, String asunto, String texto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remitente);
            message.setTo(para);
            message.setSubject(asunto);
            message.setText(texto);
            emailSender.send(message);
            System.out.println("Correo simple enviado a: " + para);
        } catch (Exception e) {
            System.err.println("Error enviando correo simple: " + e.getMessage());
        }
    }

    @Async
    @Override
    public void enviarCorreoAprobacion(String destinatario, String nombreUsuario) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            // Importante: codificación UTF-8 para tildes y ñ
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("¡Bienvenido al Equipo SCM! 🐾");

            String htmlTemplate = """
                <!DOCTYPE html>
                <html>
                <body>
                    <div style="font-family: sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                        <h1 style="color: #0d6efd; text-align: center;">Bienvenido a SCM</h1>
                        <p>Hola <strong>{NOMBRE_USUARIO}</strong>,</p>
                        <p>Tu cuenta de Veterinario ha sido <strong>APROBADA</strong>.</p>
                        <p>Ya puedes ingresar al sistema con tu correo y contraseña.</p>
                        <div style="text-align: center; margin-top: 20px;">
                            <a href="https://heroic-magic-production.up.railway.app/login" style="background-color: #0d6efd; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Ir al Login</a>
                        </div>
                        <p style="color: #888; font-size: 12px; text-align: center; margin-top: 30px;">Equipo SCM</p>
                    </div>
                </body>
                </html>
                """;

            String htmlMsg = htmlTemplate.replace("{NOMBRE_USUARIO}", nombreUsuario);
            helper.setText(htmlMsg, true);

            emailSender.send(message);
            System.out.println("Correo Aprobación enviado a: " + destinatario);

        } catch (Exception e) {
            System.err.println("Error enviando correo HTML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void enviarCorreoCitaAsignada(String destinatario, String nombreDueno, String nombreMascota,
                                         String fecha, String hora, String motivo,
                                         String clinica, String direccion) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("📅 Nueva Cita Médica: " + nombreMascota);

            String htmlMsg = """
                <!DOCTYPE html>
                <html>
                <body>
                    <div style="font-family: sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                        <h2 style="color: #0d6efd; text-align: center;">Cita Confirmada</h2>
                        <p>Hola <strong>%s</strong>,</p>
                        <p>Cita agendada para: <strong>%s</strong></p>
                        <ul>
                            <li><strong>Fecha:</strong> %s</li>
                            <li><strong>Hora:</strong> %s</li>
                            <li><strong>Motivo:</strong> %s</li>
                            <li><strong>Clínica:</strong> %s</li>
                            <li><strong>Dirección:</strong> %s</li>
                        </ul>
                        <p style="text-align: center; font-size: 12px; color: #888;">Por favor llega 10 minutos antes.</p>
                    </div>
                </body>
                </html>
                """.formatted(nombreDueno, nombreMascota, fecha, hora, motivo, clinica, direccion);

            helper.setText(htmlMsg, true);
            emailSender.send(message);
            System.out.println("Correo Cita enviado a: " + destinatario);

        } catch (Exception e) {
            System.err.println("Error enviando correo cita: " + e.getMessage());
        }
    }
}