package com.magm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleCompraDTO {

    @NotNull
    private Integer idProducto;

    private String nombreProducto;

    @NotNull @Min(1)
    private Integer cantidad;

    @NotNull @Positive
    private BigDecimal precioUnitario;

    @NotNull @Positive
    private BigDecimal subtotal;
}
