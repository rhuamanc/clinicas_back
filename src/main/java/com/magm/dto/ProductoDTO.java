package com.magm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoDTO {
    private Integer idProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombreProducto;

    private Integer nroFraccion;
    private Integer unidades;
    private Integer fraccion;
    private String lote;

    @NotNull(message = "El precio de venta es obligatorio")
    @Positive(message = "El precio de venta debe ser mayor a 0")
    private BigDecimal precio;

    @NotNull(message = "El precio de costo es obligatorio")
    @Positive(message = "El precio de costo debe ser mayor a 0")
    private BigDecimal precioCompra;
    private BigDecimal porcentajeGanancia;
    private BigDecimal precioVentaCaja;
    private Integer stock;

    @NotNull(message = "El stock minimo es obligatorio")
    @Min(value = 0, message = "El stock minimo no puede ser negativo")
    private Integer stockMinimo;

    @NotBlank(message = "La unidad es obligatoria")
    private String unidad;
    private String presentacion;
    private String nroBlister;
    private BigDecimal precioBlister;

    @NotBlank(message = "La ubicacion es obligatoria")
    private String ubicacion;
    private String codigoBarras;
    private String codigoDigemid;
    private String fechaAdquisicion;

    @NotBlank(message = "La fecha de vencimiento es obligatoria")
    private String fechaVencimiento;
    private String descripcion;
    private Integer estado;
    private Integer idLaboratorio;
    private String nombreLaboratorio;

    @NotNull(message = "La zona es obligatoria")
    private Integer idZona;
}
