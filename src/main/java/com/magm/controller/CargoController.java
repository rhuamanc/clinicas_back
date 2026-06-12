package com.magm.controller;

import com.magm.dto.CargoDTO;
import com.magm.entity.Cargo;
import com.magm.entity.Usuario;
import com.magm.repository.CargoRepository;
import com.magm.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cargos")
@RequiredArgsConstructor
public class CargoController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CargoController.class);
    private final CargoRepository cargoRepository;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<CargoDTO> crear(@Valid @RequestBody CargoDTO dto,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creando cargo por usuario: {}", userDetails.getUsername());
        Usuario usuario = usuarioRepository.findByNombre(userDetails.getUsername())
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}", userDetails.getUsername());
                    return new RuntimeException("Usuario no encontrado");
                });

        Cargo c = new Cargo();
        c.setTipo(dto.getTipo());
        c.setMonto(dto.getMonto());
        c.setDescripcion(dto.getDescripcion());
        c.setUsuario(usuario);
        log.info("Cargo creado para usuario: {} con monto: {}", usuario.getNombre(), c.getMonto());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(cargoRepository.save(c)));
    }

    @GetMapping("/rango")
    public ResponseEntity<List<CargoDTO>> listarPorRango(@RequestParam Integer idZona,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaIni,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        log.info("Listando cargos para zona: {} entre {} y {}", idZona, fechaIni, fechaFin);
        return ResponseEntity.ok(cargoRepository.findByZonaAndFecha(idZona, fechaIni.atStartOfDay(), fechaFin.atTime(LocalTime.MAX))
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    private CargoDTO toDTO(Cargo c) {
        CargoDTO dto = new CargoDTO();
        dto.setIdCargo(c.getIdCargo());
        dto.setFecha(c.getFecha());
        dto.setTipo(c.getTipo());
        dto.setMonto(c.getMonto());
        dto.setDescripcion(c.getDescripcion());
        return dto;
    }
}
