package com.magm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "El usuario es requerido")
    private String nombre;

    @NotBlank(message = "La contraseña es requerida")
    private String password;

    @NotNull(message = "La sucursal es requerida")
    private Integer idZona;
}
