package com.scm.scm.service;

public interface EmailService {
    // Tu método existente
    void enviarMensajeSimple(String para, String asunto, String texto);

    // --- AÑADE ESTE NUEVO ---
    void enviarCorreoAprobacion(String destinatario, String nombreUsuario);
    void enviarCorreoCitaAsignada(String destinatario, String nombreDueno, String nombreMascota,
                                  String fecha, String hora, String motivo,
                                  String clinica, String direccion);
}