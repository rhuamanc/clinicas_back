package com.magm.controller;

import com.magm.dto.CajaDTO;
import com.magm.entity.CajaApertura;
import com.magm.entity.Usuario;
import com.magm.repository.CajaAperturaRepository;
import com.magm.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
public class CajaController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CajaController.class);
    private final CajaAperturaRepository cajaAperturaRepository;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<CajaDTO>> listar(@RequestParam Integer idZona) {
        log.info("Listando cajas para zona: {}", idZona);
        return ResponseEntity.ok(cajaAperturaRepository.findByUsuarioClienteZonaIdZonaOrderByFechaAperturaDesc(idZona)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping("/apertura")
    public ResponseEntity<CajaDTO> abrir(@RequestBody CajaDTO dto,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Apertura de caja por usuario: {}", userDetails.getUsername());
        Usuario usuario = usuarioRepository.findByNombre(userDetails.getUsername())
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}", userDetails.getUsername());
                    return new RuntimeException("Usuario no encontrado");
                });

        CajaApertura caja = new CajaApertura();
        caja.setMontoInicial(dto.getMontoInicial() == null ? BigDecimal.ZERO : dto.getMontoInicial());
        caja.setMontoFinal(dto.getMontoInicial() == null ? BigDecimal.ZERO : dto.getMontoInicial());
        caja.setEstado("ABIERTA");
        caja.setUsuario(usuario);

        log.info("Caja abierta para usuario: {} con monto inicial: {}", usuario.getNombre(), caja.getMontoInicial());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(cajaAperturaRepository.save(caja)));
    }

    @PutMapping("/{id}/cierre")
    public ResponseEntity<CajaDTO> cerrar(@PathVariable Integer id, @RequestBody CajaDTO dto) {
        log.info("Cierre de caja con id: {}", id);
        CajaApertura caja = cajaAperturaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Caja no encontrada: {}", id);
                    return new RuntimeException("Caja no encontrada");
                });
        if (!"ABIERTA".equalsIgnoreCase(caja.getEstado())) {
            log.warn("Intento de cerrar una caja ya cerrada: {}", id);
            throw new RuntimeException("La caja ya esta cerrada");
        }
        caja.setEstado("CERRADA");
        caja.setFechaCierre(LocalDateTime.now());
        caja.setMontoFinal(dto.getMontoFinal() == null ? caja.getMontoFinal() : dto.getMontoFinal());
        log.info("Caja cerrada con id: {} y monto final: {}", id, caja.getMontoFinal());
        return ResponseEntity.ok(toDTO(cajaAperturaRepository.save(caja)));
    }

    private CajaDTO toDTO(CajaApertura c) {
        CajaDTO dto = new CajaDTO();
        dto.setIdCaja(c.getIdCaja());
        dto.setFechaApertura(c.getFechaApertura());
        dto.setFechaCierre(c.getFechaCierre());
        dto.setMontoInicial(c.getMontoInicial());
        dto.setMontoFinal(c.getMontoFinal());
        dto.setEstado(c.getEstado());
        return dto;
    }
}
