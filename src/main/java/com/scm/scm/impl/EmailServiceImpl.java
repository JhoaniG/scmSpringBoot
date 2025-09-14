package com.scm.scm.impl;

import com.scm.scm.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Override
    public void enviarMensajeSimple(String para, String asunto, String texto) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("jhoanigallo@gmail.com"); // Puedes poner un remitente genérico
        message.setTo(para);
        message.setSubject(asunto);
        message.setText(texto);
        emailSender.send(message);
    }
}