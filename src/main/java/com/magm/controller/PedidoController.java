package com.magm.controller;

import com.magm.dto.ItemCantidadDTO;
import com.magm.dto.PedidoDTO;
import com.magm.entity.DetallePedido;
import com.magm.entity.Pedido;
import com.magm.entity.Producto;
import com.magm.entity.Usuario;
import com.magm.repository.PedidoRepository;
import com.magm.repository.ProductoRepository;
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
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<PedidoDTO> crear(@RequestBody PedidoDTO dto,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByNombre(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Pedido pedido = new Pedido();
        pedido.setEstadoPedido(dto.getEstadoPedido());
        pedido.setObservacion(dto.getObservacion());
        pedido.setUsuario(usuario);

        for (ItemCantidadDTO item : dto.getItems()) {
            Producto p = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            DetallePedido dp = new DetallePedido();
            dp.setPedido(pedido);
            dp.setProducto(p);
            dp.setCantidad(item.getCantidad());
            pedido.getDetallePedidos().add(dp);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(pedidoRepository.save(pedido)));
    }

    @PutMapping("/{id}/confirmar")
    @Transactional
    public ResponseEntity<PedidoDTO> confirmar(@PathVariable Integer id) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (!"PENDIENTE".equalsIgnoreCase(pedido.getEstadoPedido())) {
            throw new RuntimeException("Solo se pueden confirmar pedidos pendientes");
        }

        for (DetallePedido d : pedido.getDetallePedidos()) {
            Producto p = d.getProducto();
            if (p.getStock() < d.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para " + p.getNombreProducto());
            }
            p.setStock(p.getStock() - d.getCantidad());
            productoRepository.save(p);
        }

        pedido.setEstadoPedido("CONFIRMADO");
        return ResponseEntity.ok(toDTO(pedidoRepository.save(pedido)));
    }

    @GetMapping("/rango")
    public ResponseEntity<List<PedidoDTO>> listarPorRango(@RequestParam Integer idZona,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaIni,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(pedidoRepository.findByZonaAndFecha(idZona, fechaIni.atStartOfDay(), fechaFin.atTime(LocalTime.MAX))
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    private PedidoDTO toDTO(Pedido p) {
        PedidoDTO dto = new PedidoDTO();
        dto.setIdPedido(p.getIdPedido());
        dto.setFechaRegistro(p.getFechaRegistro());
        dto.setEstadoPedido(p.getEstadoPedido());
        dto.setObservacion(p.getObservacion());
        dto.setItems(p.getDetallePedidos().stream().map(d -> {
            ItemCantidadDTO i = new ItemCantidadDTO();
            i.setIdProducto(d.getProducto().getIdProducto());
            i.setNombreProducto(d.getProducto().getNombreProducto());
            i.setCantidad(d.getCantidad());
            return i;
        }).collect(Collectors.toList()));
        return dto;
    }
}
