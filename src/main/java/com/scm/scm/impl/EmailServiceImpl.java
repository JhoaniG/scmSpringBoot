package com.scm.scm.impl;

import com.scm.scm.service.EmailService;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    // Leemos la API Key desde las variables
    @Value("${resend.api.key}")
    private String resendApiKey;

    private final String remitente = "onboarding@resend.dev";

    @Async
    @Override
    public void enviarMensajeSimple(String para, String asunto, String texto) {
        try {
            Resend resend = new Resend(resendApiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(remitente)
                    .to(para)
                    .subject(asunto)
                    .text(texto) // Usamos .text para correos simples
                    .build();

            resend.emails().send(params);
            System.out.println("Correo simple enviado vía API a: " + para);
        } catch (Exception e) {
            System.err.println("Error API Resend Simple: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void enviarCorreoAprobacion(String destinatario, String nombreUsuario) {
        try {
            Resend resend = new Resend(resendApiKey);

            // Tu HTML original intacto
            String htmlTemplate = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 15px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
                        .header { 
                            background: linear-gradient(135deg, #6f42c1 0%, #0d6efd 100%);
                            color: white; padding: 30px; text-align: center; 
                        }
                        .header h1 { margin: 0; font-size: 32px; letter-spacing: 2px; }
                        .content { padding: 40px 30px; color: #333333; line-height: 1.6; }
                        .h-user { color: #0d6efd; font-weight: bold; font-size: 20px; }
                        .btn { 
                            display: inline-block; background-color: #0d6efd; color: #ffffff !important; 
                            text-decoration: none; padding: 12px 30px; border-radius: 50px; 
                            font-weight: bold; margin-top: 20px; font-size: 16px;
                        }
                        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #888; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>SCM</h1>
                            <p>Sistema de Control de Mascotas</p>
                        </div>
                        <div class="content">
                            <p>Hola, <span class="h-user">{NOMBRE_USUARIO}</span> 👋</p>
                            <p>¡Nos alegra informarte que tu solicitud para unirte como <strong>Veterinario</strong> ha sido <strong>APROBADA</strong>!</p>
                            <p>A partir de este momento, tu cuenta tiene acceso completo al panel profesional.</p>
                            <div style="background-color: #e3f2fd; border-left: 5px solid #0d6efd; padding: 15px; margin: 20px 0; border-radius: 5px;">
                                <strong>Acceso:</strong> Usa el mismo correo y contraseña con los que te registraste.
                            </div>
                            <div style="text-align: center;">
                                <a href="https://heroic-magic-production.up.railway.app/login" class="btn">Ir a mi Panel</a>
                            </div>
                            <p style="margin-top: 30px;">Gracias por querer cuidar a las mascotas con nosotros.</p>
                            <p>Atentamente,<br><strong>El equipo de Administración SCM</strong></p>
                        </div>
                        <div class="footer">
                            &copy; 2025 SCM. Todos los derechos reservados.<br>
                            Este es un mensaje automático, por favor no responder.
                        </div>
                    </div>
                </body>
                </html>
                """;

            String htmlMsg = htmlTemplate.replace("{NOMBRE_USUARIO}", nombreUsuario);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(remitente)
                    .to(destinatario)
                    .subject("¡Bienvenido al Equipo SCM! 🐾")
                    .html(htmlMsg) // Aquí pasamos el HTML
                    .build();

            resend.emails().send(params);
            System.out.println("Correo Aprobación enviado vía API a: " + destinatario);

        } catch (Exception e) {
            System.err.println("Error API Resend Aprobación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void enviarCorreoCitaAsignada(String destinatario, String nombreDueno, String nombreMascota,
                                         String fecha, String hora, String motivo,
                                         String clinica, String direccion) {
        try {
            Resend resend = new Resend(resendApiKey);

            String htmlMsg = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', sans-serif; background-color: #f4f6f9; padding: 20px; color: #333; }
                        .card { background: white; border-radius: 15px; padding: 30px; max-width: 600px; margin: 0 auto; box-shadow: 0 5px 15px rgba(0,0,0,0.05); border-top: 5px solid #0d6efd; }
                        .header { text-align: center; margin-bottom: 25px; }
                        h2 { color: #0d6efd; margin: 0 0 5px 0; }
                        .subtitle { color: #6c757d; font-size: 14px; }
                        .info-box { background-color: #e3f2fd; padding: 20px; border-radius: 10px; margin: 20px 0; border-left: 4px solid #0dcaf0; }
                        .location-box { background-color: #f8f9fa; padding: 15px; border-radius: 10px; margin-top: 15px; border: 1px dashed #ccc; }
                        .label { font-weight: bold; color: #555; margin-right: 5px; }
                        .row { margin-bottom: 8px; }
                        .footer { text-align: center; font-size: 12px; color: #999; margin-top: 30px; border-top: 1px solid #eee; padding-top: 15px; }
                        .btn { display: inline-block; background-color: #0d6efd; color: white; padding: 10px 20px; text-decoration: none; border-radius: 50px; font-weight: bold; margin-top: 15px; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="header">
                            <h2>Cita Programada</h2>
                            <span class="subtitle">Sistema de Control de Mascotas (SCM)</span>
                        </div>
                        <p>Hola <strong>%s</strong>,</p>
                        <p>Te confirmamos que se ha agendado una nueva cita para tu mascota <strong>%s</strong>.</p>
                        <div class="info-box">
                            <div class="row"><span class="label">📅 Fecha:</span> %s</div>
                            <div class="row"><span class="label">⏰ Hora:</span> %s</div>
                            <div class="row"><span class="label">📋 Motivo:</span> %s</div>
                        </div>
                        <div class="location-box">
                            <div class="row"><span class="label">🏥 Clínica / Lugar:</span> <strong>%s</strong></div>
                            <div class="row"><span class="label">📍 Dirección:</span> %s</div>
                        </div>
                        <p style="text-align: center; margin-top: 20px;">Por favor, llega 10 minutos antes.</p>
                        <div class="footer">
                            &copy; 2025 SCM. Todos los derechos reservados.<br>Este es un correo automático.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(nombreDueno, nombreMascota, fecha, hora, motivo, clinica, direccion);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(remitente)
                    .to(destinatario)
                    .subject("📅 Nueva Cita Médica: " + nombreMascota)
                    .html(htmlMsg)
                    .build();

            resend.emails().send(params);
            System.out.println("Correo Cita enviado vía API a: " + destinatario);

        } catch (Exception e) {
            System.err.println("Error API Resend Cita: " + e.getMessage());
            e.printStackTrace();
        }
    }
}