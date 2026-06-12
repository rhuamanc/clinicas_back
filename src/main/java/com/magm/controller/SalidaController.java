package com.magm.controller;

import com.magm.dto.ItemCantidadDTO;
import com.magm.dto.SalidaDTO;
import com.magm.entity.DetalleSalida;
import com.magm.entity.MovimientoStock;
import com.magm.entity.Producto;
import com.magm.entity.Salida;
import com.magm.entity.Usuario;
import com.magm.repository.MovimientoStockRepository;
import com.magm.repository.ProductoRepository;
import com.magm.repository.SalidaRepository;
import com.magm.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/salidas")
@RequiredArgsConstructor
public class SalidaController {

    private final SalidaRepository salidaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<SalidaDTO> crear(@RequestBody SalidaDTO dto,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByNombre(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Salida salida = new Salida();
        salida.setMotivo(dto.getMotivo());
        salida.setEstado(1);
        salida.setUsuario(usuario);

        for (ItemCantidadDTO item : dto.getItems()) {
            Producto p = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            if (p.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para " + p.getNombreProducto());
            }
            int stockAntes = p.getStock() == null ? 0 : p.getStock();
            int cantidad = item.getCantidad() == null ? 0 : item.getCantidad();
            int stockDespues = stockAntes - cantidad;

            p.setStock(stockDespues);
            productoRepository.save(p);

            movimientoStockRepository.save(MovimientoStock.builder()
                    .tipoMovimiento("DESCARGO")
                    .tipoDescargo("MOD_STOCK")
                    .cantidad(cantidad)
                    .nroFraccion(p.getNroFraccion())
                    .stockAntes(stockAntes)
                    .stockDespues(stockDespues)
                    .idZona(usuario.getCliente().getZona().getIdZona())
                    .idReferencia(salida.getIdSalida())
                    .usuario(usuario.getNombre())
                    .producto(p.getNombreProducto())
                    .motivo(dto.getMotivo())
                    .build());

            DetalleSalida ds = new DetalleSalida();
            ds.setSalida(salida);
            ds.setProducto(p);
            ds.setCantidad(item.getCantidad());
            salida.getDetalleSalidas().add(ds);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(salidaRepository.save(salida)));
    }

    @GetMapping("/rango")
    public ResponseEntity<List<SalidaDTO>> listarPorRango(@RequestParam Integer idZona,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaIni,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(salidaRepository.findByZonaAndFecha(idZona, fechaIni.atStartOfDay(), fechaFin.atTime(LocalTime.MAX))
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    private SalidaDTO toDTO(Salida s) {
        SalidaDTO dto = new SalidaDTO();
        dto.setIdSalida(s.getIdSalida());
        dto.setFechaSalida(s.getFechaSalida());
        dto.setMotivo(s.getMotivo());
        dto.setEstado(s.getEstado());
        dto.setItems(s.getDetalleSalidas().stream().map(d -> {
            ItemCantidadDTO i = new ItemCantidadDTO();
            i.setIdProducto(d.getProducto().getIdProducto());
            i.setNombreProducto(d.getProducto().getNombreProducto());
            i.setCantidad(d.getCantidad());
            return i;
        }).collect(Collectors.toList()));
        return dto;
    }
}
