package com.scm.scm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "mascotas")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mascota", nullable = false, unique = true)
    private Long idMascota;
    @Column(name = "foto", nullable = false)
    private String foto;
    @Column(name = "nombre", nullable = false)
    private  String nombre;
    @Column(name = "genero", nullable = false)
    private String genero;
    @Column(name = "fechaNacimiento", nullable = false)
    private LocalDate fechaNacimiento;
    @Column(name = "raza", nullable = false)
    private String raza;
    //Relacion IdUsuario
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    @Column(name = "especie", nullable = false)
    private String especie;

    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Dieta> dietas;
    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ActividadFisica> actividadesFisicas;




}
