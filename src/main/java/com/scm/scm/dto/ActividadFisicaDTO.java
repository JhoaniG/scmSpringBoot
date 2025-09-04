package com.scm.scm.dto;

import com.scm.scm.model.Mascota;
import com.scm.scm.model.Veterinario;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ActividadFisicaDTO {

    private  Long idActividadFisica;

    private String descripcion;

    private  String tipoActividad;

    private String foto;

    private Long mascotaId;

    private Long veterinarioId;
}
