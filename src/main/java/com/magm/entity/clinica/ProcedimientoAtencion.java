package com.magm.entity.clinica;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "procedimientos_atencion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedimientoAtencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProcedimientoAtencion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_atencion", nullable = false)
    private AtencionMedica atencion;

    @Column(nullable = false)
    private String procedimiento;

    @Column(length = 2000)
    private String detalle;

    @Column(precision = 10, scale = 2)
    private BigDecimal tarifa;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private String estado = "REALIZADO";
}
