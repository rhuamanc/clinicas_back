package com.magm.service;

import com.magm.dto.VentaDTO;

import java.time.LocalDate;
import java.util.List;

public interface VentaService {
    VentaDTO guardarVenta(VentaDTO dto, String username);
    List<VentaDTO> listarPorFecha(LocalDate fecha, Integer idZona);
    List<VentaDTO> listarPorRango(LocalDate fechaIni, LocalDate fechaFin, Integer idZona);
    VentaDTO buscarPorId(Integer id);
}
