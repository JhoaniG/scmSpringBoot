package com.scm.scm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "solicitudes_veterinario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudVeterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSolicitud;

    // El usuario que se postula (Dueño)
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String especialidad;

    @Column(nullable = false)
    private String veterinaria; // Nombre de la clínica o consultorio

    @Column(nullable = false, length = 1000)
    private String perfilProfesional; // Descripción de vocación

    @Column(name = "hoja_vida_pdf")
    private String hojaVidaPdf; // Ruta del archivo

    @Column(nullable = false)
    private LocalDate fechaSolicitud;

    @Column(nullable = false)
    private String estado; // "PENDIENTE", "APROBADA", "RECHAZADA"
}