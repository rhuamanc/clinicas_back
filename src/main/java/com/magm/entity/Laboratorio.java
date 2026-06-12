package com.magm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "laboratorios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Laboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idLaboratorio;

    @Column(nullable = false)
    private String nombreLaboratorio;

    private String abreviatura;

    private String ruc;
    private String direccion;
    private Integer estado;
}
