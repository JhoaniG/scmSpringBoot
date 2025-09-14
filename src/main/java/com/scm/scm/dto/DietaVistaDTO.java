package com.scm.scm.dto;

import lombok.Data;

@Data
public class DietaVistaDTO {

    private Long idDieta;
    private String tipoDieta;
    private String descripcion;
    private String foto;
    private String nombreMascota;
    private String nombreVeterinario;
}