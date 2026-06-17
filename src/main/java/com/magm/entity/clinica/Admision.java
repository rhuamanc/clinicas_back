package com.magm.entity.clinica;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admisiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAdmision;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cita")
    private Cita cita;

    @Column(nullable = false)
    private LocalDateTime fechaLlegada;

    @Column(nullable = false)
    private String tipoIngreso;

    @Column(nullable = false)
    @Builder.Default
    private String estado = "REGISTRADA";

    @Column(nullable = false)
    @Builder.Default
    private String derivacion = "TRIAJE";
}
