package com.magm.entity.clinica;

import com.magm.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "atenciones_medicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAtencion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_admision", nullable = false)
    private Admision admision;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_medico", nullable = false)
    private Medico medico;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario_medico")
    private Usuario usuarioMedico;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaAtencion = LocalDateTime.now();

    @Column(length = 2000)
    private String sintomas;

    @Column(length = 2000)
    private String examenFisico;

    @Column(length = 2000)
    private String tratamiento;

    @Column(length = 2000)
    private String evolucion;

    @Column(length = 2000)
    private String observaciones;

    @Column(nullable = false)
    @Builder.Default
    private String estado = "ABIERTA";
}
