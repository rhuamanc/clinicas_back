package com.magm.entity.clinica;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "triajes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Triaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTriaje;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_admision", nullable = false)
    private Admision admision;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(precision = 10, scale = 2)
    private BigDecimal pesoKg;

    @Column(precision = 10, scale = 2)
    private BigDecimal tallaM;

    private String presionArterial;

    @Column(precision = 10, scale = 2)
    private BigDecimal temperatura;

    private Integer frecuenciaCardiaca;
    private Integer saturacionOxigeno;

    @Column(precision = 10, scale = 2)
    private BigDecimal imc;

    @Column(length = 1000)
    private String motivoConsulta;
}
