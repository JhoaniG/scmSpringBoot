package com.scm.scm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "diagnosticoDuenos")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Diagnosticodueno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diagnostico", nullable = false, unique = true)
    private Long idDiagnosticoDueno;
    @Column(name = "fechaDiagnostico", nullable = false)
    private String fechaDiagnostico;

    @Column(name = "observaciones", nullable = false)
    private String observaciones;
    // necesita idM y idV
    //relacion de muchos a uno con mastoca
    @ManyToOne
    @JoinColumn(name = "id_mascota", nullable = false)
    private Mascota mascota;
    //relacion de muchos a uno con veterinario
    @ManyToOne
    @JoinColumn(name="id_veterinario", nullable = false )
    private  Veterinario veterinario;



}
