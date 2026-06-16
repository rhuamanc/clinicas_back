package com.magm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioAdminDTO {
    private Integer id;

    @NotBlank(message = "El usuario es obligatorio")
    private String nombre;
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;
    private Integer idRol;
    private Integer estado;

    @NotNull(message = "La zona es obligatoria")
    private Integer idZona;
    private Integer idCliente;
    private String nombreCliente;
    private Boolean cuentaHabilitada;
}
