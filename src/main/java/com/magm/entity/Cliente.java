package com.magm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCliente;

    private String nombreCliente;

    private boolean cuentaHabilitada;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_zona", nullable = false)
    private Zona zona;
}
