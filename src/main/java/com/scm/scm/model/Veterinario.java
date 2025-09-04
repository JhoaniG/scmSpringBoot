package com.scm.scm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "veterinarios")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_veterinario", nullable = false, unique = true)
    private Long idVeterinario;

    @Column(name = "especialidad", nullable = false)
    private String especialidad;

    @Column(name = "veterinaria", nullable = false)
    private String veterinaria;

    // Relación uno a uno con usuario
    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;


}
