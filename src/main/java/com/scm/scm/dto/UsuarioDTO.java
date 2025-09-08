package com.scm.scm.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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


    //Solo si es vet
    private String especialidad;
    private String veterinaria;
    private MultipartFile archivoFoto;

}
