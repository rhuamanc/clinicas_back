package com.magm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "salidas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Salida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSalida;

    @Column(nullable = false)
    private LocalDateTime fechaSalida;

    @Column(nullable = false)
    private String motivo;

    @Column(nullable = false)
    private Integer estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "salida", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleSalida> detalleSalidas = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaSalida == null) {
            fechaSalida = LocalDateTime.now();
        }
        if (estado == null) {
            estado = 1;
        }
    }
}
