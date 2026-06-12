package com.magm.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoDTO {
    private Integer idPedido;
    private LocalDateTime fechaRegistro;
    private String estadoPedido;
    private String observacion;
    private List<ItemCantidadDTO> items;
}
