package com.scm.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor // Constructor simple
public class CalendarioEventoDTO {
    private String title; // El "motivo" de la cita
    private LocalDateTime start; // Fecha y hora de inicio
    private String backgroundColor; // Para los colores
    private String borderColor; // Para los colores
}