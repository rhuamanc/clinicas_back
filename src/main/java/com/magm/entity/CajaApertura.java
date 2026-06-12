package com.magm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "caja_aperturas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CajaApertura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCaja;

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    private LocalDateTime fechaCierre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoInicial;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoFinal;

    @Column(nullable = false)
    private String estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @PrePersist
    public void prePersist() {
        if (fechaApertura == null) {
            fechaApertura = LocalDateTime.now();
        }
        if (montoFinal == null) {
            montoFinal = montoInicial == null ? BigDecimal.ZERO : montoInicial;
        }
        if (estado == null || estado.isBlank()) {
            estado = "ABIERTA";
        }
    }
}
