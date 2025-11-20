package com.scm.scm.dto;

import lombok.Data;
import java.time.LocalDate; // <--- IMPORTANTE

@Data
public class ActividadVistaDTO {

    private Long idActividadFisica;
    private String descripcion;
    private String tipoActividad;
    private String foto;
    private String nombreMascota;
    private String nombreVeterinario;

    // --- AGREGAR ESTOS CAMPOS ---
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}