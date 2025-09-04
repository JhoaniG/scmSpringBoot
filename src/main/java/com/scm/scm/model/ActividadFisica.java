package com.scm.scm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "actividadFisica")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ActividadFisica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividadFisica", nullable = false, unique = true)
    private  Long idActividadFisica;
    @Column(name = "descripcion", nullable = false)
    private String descripcion;
    @Column(name = "tipoActividad", nullable = false)
    private  String tipoActividad;
    @Column(name = "foto", nullable = false)
    private String foto;
    // idM idV
@ManyToOne
    @JoinColumn(name = "id_mascota", nullable = false)
    private Mascota mascota;
//relacion muchos a uno con veterinario
    @ManyToOne
    @JoinColumn(name = "id_veterinario", nullable = false)
    private Veterinario veterinario;

}
