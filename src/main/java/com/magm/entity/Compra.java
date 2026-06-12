package com.magm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCompra;

    @Column(nullable = false)
    private LocalDateTime fechaTransaccion;

    private String tipoComprobante;
    private String nroComprobante;
    private String nroGuia;
    private String tipoPago;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal montoCompra;

    @Column(nullable = false)
    private Integer estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleCompra> detalleCompras = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaTransaccion == null) {
            fechaTransaccion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = 1;
        }
    }
}
