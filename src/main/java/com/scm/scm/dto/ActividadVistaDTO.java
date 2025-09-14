package com.scm.scm.dto;

import lombok.Data;

@Data
public class ActividadVistaDTO {

    private Long idActividadFisica;
    private String descripcion;
    private String tipoActividad;
    private String foto;
    private String nombreMascota;
    private String nombreVeterinario;
}