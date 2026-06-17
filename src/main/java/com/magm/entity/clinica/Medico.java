package com.magm.entity.clinica;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "medicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMedico;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false, unique = true)
    private String cmp;

    private String telefono;
    private String email;
    private String consultorio;
    private LocalTime horarioInicio;
    private LocalTime horarioFin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "medico_especialidad",
            joinColumns = @JoinColumn(name = "id_medico"),
            inverseJoinColumns = @JoinColumn(name = "id_especialidad")
    )
    @Builder.Default
    private Set<Especialidad> especialidades = new HashSet<>();
}
