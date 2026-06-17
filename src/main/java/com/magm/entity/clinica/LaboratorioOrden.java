package com.magm.entity.clinica;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "laboratorio_ordenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaboratorioOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idOrdenLaboratorio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_atencion", nullable = false)
    private AtencionMedica atencion;

    @Column(nullable = false)
    private String examen;

    @Column(nullable = false)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaOrden = LocalDateTime.now();

    private LocalDateTime fechaMuestra;
    private LocalDateTime fechaEntrega;

    @Column(length = 1000)
    private String observaciones;
}
