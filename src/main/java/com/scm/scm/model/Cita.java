package com.scm.scm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "citas")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita", nullable = false, updatable = false)
    private  Long idCita;
    @Column(name = "fechaCita", nullable = false)
    private LocalDate fechaCita;
    @Column(name = "motivoCita", nullable = false, length = 100)
    private String motivoCita;
    @Column(name = "estadoCita", nullable = false, length = 200)
    private String estadoCita;
    //idV idM idD
    //relacion de muchos a uno con mascota
    @ManyToOne
    @JoinColumn(name = "id_mascota", nullable = false)
    private Mascota mascota;
    //relacion de muchos a uno con veterinario
    @ManyToOne
    @JoinColumn(name = "id_veterinario")
    private Veterinario veterinario;
    //relacion de muchos a uno con diagnostico
    @ManyToOne
    @JoinColumn(name = "id_diagnostico", nullable = false)
    private Diagnosticodueno diagnosticodueno;




}
