package com.scm.scm.dto;

import com.scm.scm.model.Mascota;
import com.scm.scm.model.Veterinario;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DietaDTO {

    private Long idDieta;

    private  String descripcion;

    private  String tipoDieta;

    private  String foto;

    private Long mascotaId;

    private Long veterinarioId;
}
