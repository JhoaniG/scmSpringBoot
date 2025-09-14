package com.scm.scm.dto;

import com.scm.scm.model.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class VeterinarioDTO {

    private Long idVeterinario;


    private String especialidad;


    private String veterinaria;


    private Long usuarioId;

    // Campos del usuario asociado para mostrar en la vista

    private String nombreUsuario;
    private String emailUsuario;
    private String fotoUsuario;
}
