package com.magm.controller;

import com.magm.dto.VentaDTO;
import com.magm.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @PostMapping
    public ResponseEntity<VentaDTO> guardar(
            @Valid @RequestBody VentaDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ventaService.guardarVenta(dto, userDetails.getUsername()));
    }

    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<VentaDTO>> listarPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam Integer idZona) {
        return ResponseEntity.ok(ventaService.listarPorFecha(fecha, idZona));
    }

    @GetMapping("/rango")
    public ResponseEntity<List<VentaDTO>> listarPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaIni,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam Integer idZona) {
        return ResponseEntity.ok(ventaService.listarPorRango(fechaIni, fechaFin, idZona));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.buscarPorId(id));
    }
}
