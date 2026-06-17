package com.magm.entity.clinica;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPaciente;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    private String telefono;
    private String direccion;
    private LocalDate fechaNacimiento;
    private String sexo;

    @Column(length = 1000)
    private String alergias;

    @Column(length = 2000)
    private String antecedentes;

    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;

    @Column(nullable = false)
    @Builder.Default
    private Integer estado = 1;
}
