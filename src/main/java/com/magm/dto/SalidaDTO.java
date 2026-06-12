package com.magm.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SalidaDTO {
    private Integer idSalida;
    private LocalDateTime fechaSalida;
    private String motivo;
    private Integer estado;
    private List<ItemCantidadDTO> items;
}
