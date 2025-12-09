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
import java.util.Properties;

@Service
public class EmailServiceImpl implements EmailService {

    // Esta variable lee tu clave de Railway (MAIL_PASSWORD)
    @Value("${brevo.api.key}")
    private String brevoApiKey;

    // Tu correo verificado en Brevo
    private final String remitenteEmail = "seguimientoycotroldemascotassc@gmail.com";
    private final String remitenteNombre = "SCM Admin";

    private TransactionalEmailsApi initApi() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        // Configura la clave API
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
            e.printStackTrace();
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
            sendSmtpEmail.setSubject("¡Bienvenido al Equipo SCM! 🐾");

            // HTML MANUAL
            String htmlContent = "<html><body>" +
                    "<h1>Hola " + nombreUsuario + "</h1>" +
                    "<p>Tu cuenta ha sido <strong>APROBADA</strong>.</p>" +
                    "<p>Ya puedes acceder al sistema.</p>" +
                    "<a href='https://heroic-magic-production.up.railway.app/login'>Ir al Login</a>" +
                    "</body></html>";

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
            sendSmtpEmail.setSubject("📅 Nueva Cita: " + nombreMascota);

            String htmlContent = "<html><body>" +
                    "<h1>Cita Confirmada</h1>" +
                    "<p>Mascota: <strong>" + nombreMascota + "</strong></p>" +
                    "<p>Fecha: " + fecha + " a las " + hora + "</p>" +
                    "<p>Lugar: " + clinica + " - " + direccion + "</p>" +
                    "</body></html>";

            sendSmtpEmail.setHtmlContent(htmlContent);

            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("Brevo API: Correo Cita enviado a " + destinatario);

        } catch (Exception e) {
            System.err.println("Error Brevo API Cita: " + e.getMessage());
            e.printStackTrace();
        }
    }
}