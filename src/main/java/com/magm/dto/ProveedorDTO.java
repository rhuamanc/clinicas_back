package com.magm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProveedorDTO {
    private Integer idProveedor;

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    private String nombreProveedor;
    private String ruc;
    private String direccion;
    private String telefono;
    private Integer estado;
}
