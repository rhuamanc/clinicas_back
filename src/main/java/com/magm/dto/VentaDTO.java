package com.magm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaDTO {

    private Integer idVenta;
    private LocalDateTime fechaTransaccion;

    @NotNull @Positive
    private BigDecimal montoVenta;

    @NotNull
    private Integer tipoPago;

    private BigDecimal montoCobrado;
    private BigDecimal vuelto;
    private String documentoTipo;
    private String documentoNumero;
    private String documentoNombre;

    private Integer estado;

    @NotEmpty(message = "La venta debe tener al menos un producto")
    @Valid
    private List<DetalleVentaDTO> detalleVentas;
}
