package com.magm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LaboratorioDTO {
    private Integer idLaboratorio;

    @NotBlank(message = "El nombre del laboratorio es obligatorio")
    private String nombreLaboratorio;

    @NotBlank(message = "La abreviatura es obligatoria")
    private String abreviatura;
    private String ruc;
    private String direccion;
    private Integer estado;
}
