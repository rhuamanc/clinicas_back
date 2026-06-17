package com.magm.entity.clinica;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recetas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReceta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_atencion", nullable = false)
    private AtencionMedica atencion;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaReceta = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private String estado = "PENDIENTE";

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<RecetaDetalle> detalles = new ArrayList<>();

    @Column(length = 1000)
    private String observaciones;
}
