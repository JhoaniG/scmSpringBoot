package com.scm.scm.dto;

import com.scm.scm.model.Diagnosticodueno;
import com.scm.scm.model.Mascota;
import com.scm.scm.model.Veterinario;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CitaDTO {
    private  Long idCita;
    private LocalDate fechaCita;
    private String motivoCita;
    private String estadoCita;

    private Long mascotaId;

    private Long veterinarioId;


    private Long diagnosticoduenoId;



    private String nombreMascota;
    private String nombreVeterinario;
    private  String nombreDueno;

}
