package com.magm.entity.clinica;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diagnosticos_atencion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosticoAtencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDiagnosticoAtencion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_atencion", nullable = false)
    private AtencionMedica atencion;

    @Column(length = 20)
    private String codigoCie10;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private String tipo = "PRESUNTIVO";
}
