package com.magm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProveedor;

    @Column(nullable = false)
    private String nombreProveedor;

    private String ruc;
    private String direccion;
    private String telefono;

    @Column(nullable = false)
    private Integer estado;
}
