package com.magm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IncentivoDTO {
    private Integer idIncentivo;
    private LocalDateTime fecha;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    private String descripcion;

    @NotNull(message = "Debe seleccionar un producto")
    private Integer idProducto;

    private String nombreProducto;
}
