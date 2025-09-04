package com.scm.scm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dietas")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Dieta {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id_dieta", nullable = false, unique = true)
    private Long idDieta;
   @Column(name = "descripcion", nullable = false)
   private  String descripcion;
   @Column(name = "tipoDieta", nullable = false)
   private  String tipoDieta;
   @Column(name = "foto", nullable = false)
   private  String foto;
   // idM idV
   @ManyToOne
   @JoinColumn(name = "id_mascota", nullable = false)
   private Mascota mascota;
   //relacion muchos a uno con veterinario
   @ManyToOne
   @JoinColumn(name = "id_veterinario", nullable = false)
   private Veterinario veterinario;
}
