package com.magm.entity.clinica;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "laboratorio_resultados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaboratorioResultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idResultadoLaboratorio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_orden_laboratorio", nullable = false)
    private LaboratorioOrden orden;

    @Column(nullable = false, length = 4000)
    private String resultado;

    private String archivoAdjunto;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaResultado = LocalDateTime.now();
}
