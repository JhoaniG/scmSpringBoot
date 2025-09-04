package com.scm.scm.dto;

import com.scm.scm.model.Usuario;
import lombok.*;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class MascotaDTO {

    private Long idMascota;

    private String foto;

    private  String nombre;

    private String genero;

    private LocalDate fechaNacimiento;

    private String raza;

    private Long usuarioId;
}
