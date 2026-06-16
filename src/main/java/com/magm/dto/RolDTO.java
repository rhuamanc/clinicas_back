package com.magm.dto;

import lombok.Data;

import java.util.List;

@Data
public class RolDTO {
    private Integer idRol;
    private String nombre;
    private String descripcion;
    private Integer estado;
    private List<String> recursos;
}
