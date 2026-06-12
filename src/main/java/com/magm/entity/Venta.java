package com.magm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVenta;

    @Column(nullable = false)
    private LocalDateTime fechaTransaccion;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal montoVenta;

    @Column(nullable = false)
    private Integer tipoPago;

    @Column(nullable = false)
    private Integer estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<DetalleVenta> detalleVentas = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaTransaccion == null) {
            fechaTransaccion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = 1;
        }
    }
}
