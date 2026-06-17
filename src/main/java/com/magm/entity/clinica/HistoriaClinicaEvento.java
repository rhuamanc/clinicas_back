package com.magm.entity.clinica;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historia_clinica_eventos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriaClinicaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHistoriaEvento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(nullable = false)
    private String tipoEvento;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaEvento = LocalDateTime.now();
}
