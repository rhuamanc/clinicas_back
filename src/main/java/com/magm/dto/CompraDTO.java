package com.magm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraDTO {

    private Integer idCompra;
    private LocalDateTime fechaTransaccion;

    @NotBlank(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante;

    @NotBlank(message = "El numero de comprobante es obligatorio")
    private String nroComprobante;

    @NotBlank(message = "El numero de guia es obligatorio")
    private String nroGuia;

    @NotBlank(message = "El tipo de pago es obligatorio")
    private String tipoPago;

    @NotNull @Positive
    private BigDecimal montoCompra;

    private Integer estado;

    @NotNull
    private Integer idProveedor;

    private String nombreProveedor;

    @NotEmpty(message = "La compra debe tener al menos un producto")
    @Valid
    private List<DetalleCompraDTO> detalleCompras;
}
