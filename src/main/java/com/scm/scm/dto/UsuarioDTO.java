package com.scm.scm.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {

    private Long idUsuario;


    private String foto;


    private String nombre;


    private String apellido;


    private String email;


    private String contrasena;


    private String telefono;


    private String direccion;


    private LocalDate fechaNacimiento;


    private Long rolId;
}
