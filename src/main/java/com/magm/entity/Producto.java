package com.magm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "productos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProducto;

    @Column(nullable = false)
    private String nombreProducto;

    private Integer nroFraccion;
    private Integer unidades;
    private Integer fraccion;
    private String lote;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal precio;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Column(precision = 10, scale = 2)
    private BigDecimal porcentajeGanancia;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioVentaCaja;

    private Integer stock;
    private Integer stockMinimo;
    private String unidad;
    private String presentacion;
    private String nroBlister;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioBlister;

    private String ubicacion;
    private String codigoBarras;
    private String codigoDigemid;
    private LocalDate fechaAdquisicion;
    private LocalDate fechaVencimiento;
    private String descripcion;

    @Column(nullable = false)
    private Integer estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_laboratorio")
    private Laboratorio laboratorio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_zona", nullable = false)
    private Zona zona;
}
