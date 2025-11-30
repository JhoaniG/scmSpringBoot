package com.scm.scm.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class SolicitudVeterinarioDTO {

    private Long idSolicitud;

    @NotBlank(message = "La especialidad es obligatoria")
    private String especialidad;

    @NotBlank(message = "El nombre de la veterinaria/clínica es obligatorio")
    private String veterinaria;

    @NotBlank(message = "Cuéntanos sobre tu vocación")
    private String perfilProfesional;

    // El archivo PDF que subirá
    private MultipartFile archivoHojaVida;

    // Datos para mostrar en la tabla del admin
    private String nombreSolicitante;
    private String fechaSolicitud;
}