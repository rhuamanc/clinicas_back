package com.magm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "zonas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Zona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idZona;

    @Column(nullable = false)
    private String nombreZona;

    private String direccion;
    private String ruc;
    private String razonSocial;
}
