package com.scm.scm.impl;

import com.scm.scm.service.EmailService;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    private final String remitenteEmail = "seguimientoycotroldemascotassc@gmail.com";
    private final String remitenteNombre = "Sistema SCM";

    // URL de tu aplicación en Railway (para los botones)
    private final String baseUrl = "https://heroic-magic-production.up.railway.app";

    private TransactionalEmailsApi initApi() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(brevoApiKey);
        return new TransactionalEmailsApi();
    }

    @Async
    @Override
    public void enviarMensajeSimple(String para, String asunto, String texto) {
        try {
            TransactionalEmailsApi apiInstance = initApi();
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(remitenteEmail);
            sender.setName(remitenteNombre);

            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(para);

            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Collections.singletonList(to));
            sendSmtpEmail.setSubject(asunto);
            sendSmtpEmail.setTextContent(texto);

            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("Brevo API: Correo simple enviado a " + para);
        } catch (Exception e) {
            System.err.println("Error Brevo API Simple: " + e.getMessage());
        }
    }

    @Async
    @Override
    public void enviarCorreoAprobacion(String destinatario, String nombreUsuario) {
        try {
            TransactionalEmailsApi apiInstance = initApi();
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(remitenteEmail);
            sender.setName(remitenteNombre);

            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(destinatario);

            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Collections.singletonList(to));
            sendSmtpEmail.setSubject("✅ ¡Bienvenido al Equipo SCM!");

            // --- PLANTILLA HTML DE APROBACIÓN ---
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.05); }
                        .header { background: linear-gradient(135deg, #6366f1 0%, #3b82f6 100%); padding: 30px; text-align: center; color: white; }
                        .header h1 { margin: 0; font-size: 28px; letter-spacing: 1px; }
                        .content { padding: 40px 30px; color: #334155; line-height: 1.6; }
                        .welcome-text { font-size: 18px; margin-bottom: 20px; }
                        .highlight { color: #4f46e5; font-weight: bold; }
                        .status-box { background-color: #ecfdf5; border-left: 5px solid #10b981; padding: 15px; margin: 25px 0; color: #065f46; font-weight: 500; }
                        .btn-container { text-align: center; margin-top: 35px; }
                        .btn { background-color: #4f46e5; color: #ffffff !important; text-decoration: none; padding: 14px 32px; border-radius: 50px; font-weight: bold; display: inline-block; box-shadow: 0 4px 6px rgba(79, 70, 229, 0.2); transition: background 0.3s; }
                        .footer { background-color: #f8fafc; padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>SCM</h1>
                            <span style="opacity: 0.9; font-size: 14px;">Control de Mascotas</span>
                        </div>
                        <div class="content">
                            <p class="welcome-text">Hola, <span class="highlight">%s</span> 👋</p>
                            
                            <p>Nos complace informarte que tu solicitud para unirte como <strong>Veterinario</strong> ha sido revisada.</p>
                            
                            <div class="status-box">
                                ✅ Tu cuenta ha sido APROBADA exitosamente.
                            </div>
                            
                            <p>A partir de este momento, tienes acceso completo al panel de gestión profesional. Puedes comenzar a administrar citas y pacientes.</p>
                            
                            <div class="btn-container">
                                <a href="%s/login" class="btn">Ingresar al Sistema</a>
                            </div>
                        </div>
                        <div class="footer">
                            &copy; 2025 SCM - Sistema de Control de Mascotas.<br>
                            Este es un mensaje automático, por favor no responder.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(nombreUsuario, baseUrl);

            sendSmtpEmail.setHtmlContent(htmlContent);

            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("Brevo API: Correo Aprobación enviado a " + destinatario);

        } catch (Exception e) {
            System.err.println("Error Brevo API HTML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void enviarCorreoCitaAsignada(String destinatario, String nombreDueno, String nombreMascota,
                                         String fecha, String hora, String motivo,
                                         String clinica, String direccion) {
        try {
            TransactionalEmailsApi apiInstance = initApi();
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(remitenteEmail);
            sender.setName(remitenteNombre);

            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(destinatario);

            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Collections.singletonList(to));
            sendSmtpEmail.setSubject("📅 Nueva Cita Agendada: " + nombreMascota);

            // --- PLANTILLA HTML DE CITA ---
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 0; }
                        .card { max-width: 600px; margin: 30px auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05); }
                        .header { background-color: #4f46e5; padding: 25px; text-align: center; color: white; }
                        .header h2 { margin: 0; font-size: 24px; }
                        .body { padding: 30px; color: #333; }
                        .greeting { font-size: 18px; color: #1e293b; margin-bottom: 20px; }
                        .pet-badge { background-color: #e0e7ff; color: #4338ca; padding: 4px 10px; border-radius: 6px; font-weight: bold; }
                        .info-grid { background-color: #f8fafc; border-radius: 12px; padding: 20px; border: 1px solid #e2e8f0; margin-top: 15px; }
                        .row { display: flex; justify-content: space-between; margin-bottom: 12px; border-bottom: 1px dashed #cbd5e1; padding-bottom: 12px; }
                        .row:last-child { border-bottom: none; margin-bottom: 0; padding-bottom: 0; }
                        .label { color: #64748b; font-size: 14px; font-weight: 600; }
                        .value { color: #0f172a; font-weight: 500; text-align: right; }
                        .location-box { background-color: #fff7ed; border-left: 4px solid #f97316; padding: 15px; margin-top: 25px; border-radius: 4px; }
                        .footer { text-align: center; font-size: 12px; color: #94a3b8; padding: 20px; background-color: #f1f5f9; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="header">
                            <h2>Cita Confirmada</h2>
                        </div>
                        <div class="body">
                            <p class="greeting">Hola <strong>%s</strong>,</p>
                            <p>Se ha programado una nueva visita médica para <span class="pet-badge">%s</span>.</p>
                            
                            <div class="info-grid">
                                <div class="row">
                                    <span class="label">📅 Fecha</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="row">
                                    <span class="label">⏰ Hora</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="row">
                                    <span class="label">📋 Motivo</span>
                                    <span class="value">%s</span>
                                </div>
                            </div>

                            <div class="location-box">
                                <div style="color: #c2410c; font-weight: bold; margin-bottom: 5px;">📍 Ubicación</div>
                                <div><strong>%s</strong></div>
                                <div style="font-size: 14px; color: #555;">%s</div>
                            </div>
                            
                            <p style="text-align: center; margin-top: 25px; font-size: 14px; color: #64748b;">
                                Recomendamos llegar 10 minutos antes de la hora programada.
                            </p>
                        </div>
                        <div class="footer">
                            &copy; 2025 SCM. Cuidando lo que más quieres.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(nombreDueno, nombreMascota, fecha, hora, motivo, clinica, direccion);

            sendSmtpEmail.setHtmlContent(htmlContent);

            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("Brevo API: Correo Cita enviado a " + destinatario);

        } catch (Exception e) {
            System.err.println("Error Brevo API Cita: " + e.getMessage());
            e.printStackTrace();
        }
    }
}