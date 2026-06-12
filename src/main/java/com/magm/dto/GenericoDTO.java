package com.magm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenericoDTO {
    private Integer idGenerico;

    @NotBlank(message = "El nombre del generico es obligatorio")
    private String nombre;
    private String descripcion;
    private Integer estado;
}
