package com.magm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovimiento;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private String tipoMovimiento; // CARGO | DESCARGO

    @Column
    private String tipoDescargo; // MOD_STOCK | VENTA

    @Column(nullable = false)
    private Integer cantidad;

    @Column
    private Integer nroFraccion;

    @Column
    private Integer stockAntes;

    @Column
    private Integer stockDespues;

    @Column(nullable = false)
    private Integer idZona;

    @Column
    private Integer idReferencia;

    @Column
    private String usuario;

    @Column
    private String producto;

    @Column
    private String motivo;

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
