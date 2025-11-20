package com.scm.scm.dto;

import lombok.Data;
import java.time.LocalDate; // <--- Importante: No olvides importar esto

@Data
public class DietaVistaDTO {

    private Long idDieta;
    private String tipoDieta;
    private String descripcion;
    private String foto;
    private String nombreMascota;
    private String nombreVeterinario;

    // --- AGREGA ESTOS DOS CAMPOS ---
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}