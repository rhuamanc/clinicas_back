package com.magm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "genericos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Generico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idGenerico;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private Integer estado;
}
