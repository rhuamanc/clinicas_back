package com.magm.entity.clinica;

import com.magm.entity.Producto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "receta_detalles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecetaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRecetaDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_receta", nullable = false)
    private Receta receta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(nullable = false)
    private String medicamento;

    @Column(nullable = false)
    private Integer cantidad;

    private String dosis;
    private String frecuencia;
    private String duracion;

    @Column(length = 1000)
    private String indicaciones;
}
