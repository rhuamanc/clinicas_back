package com.magm.entity.clinica;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "examenes_laboratorio", uniqueConstraints = {
        @UniqueConstraint(name = "uk_examen_laboratorio_codigo", columnNames = "codigo"),
        @UniqueConstraint(name = "uk_examen_laboratorio_nombre", columnNames = "nombre")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamenLaboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idExamen;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String areaLaboratorio;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, length = 100)
    private String tiempoEntrega;

    @Column(nullable = false)
    @Builder.Default
    private Boolean requiereAyuno = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean requiereMuestraEspecial = false;

    @Column(length = 1500)
    private String indicacionesPaciente;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
