package com.scm.scm.dto;

import com.scm.scm.model.Mascota;
import com.scm.scm.model.Veterinario;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DiagnosticoDuenoDTO {

    private Long idDiagnosticoDueno;

    private String fechaDiagnostico;
    private String nombreM;       // Nombre de la mascota
    private String nombreDueno;


    private String observaciones;

    private Long mascotaId;

    private Long veterinarioId;
}
