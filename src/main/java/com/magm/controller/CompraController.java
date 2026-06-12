package com.magm.controller;

import com.magm.dto.CompraDTO;
import com.magm.dto.DetalleCompraDTO;
import com.magm.entity.Compra;
import com.magm.entity.DetalleCompra;
import com.magm.entity.MovimientoStock;
import com.magm.entity.Producto;
import com.magm.entity.Proveedor;
import com.magm.entity.Usuario;
import com.magm.repository.CompraRepository;
import com.magm.repository.MovimientoStockRepository;
import com.magm.repository.ProductoRepository;
import com.magm.repository.ProveedorRepository;
import com.magm.repository.UsuarioRepository;
import jakarta.validation.Valid;
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
@RequestMapping("/api/compras")
@RequiredArgsConstructor

public class CompraController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompraController.class);
    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<CompraDTO> crear(@Valid @RequestBody CompraDTO dto,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creando compra por usuario: {} para proveedor: {}", userDetails.getUsername(), dto.getIdProveedor());
        Usuario usuario = usuarioRepository.findByNombre(userDetails.getUsername())
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}", userDetails.getUsername());
                    return new RuntimeException("Usuario no encontrado");
                });

        Proveedor proveedor = proveedorRepository.findById(dto.getIdProveedor())
                .orElseThrow(() -> {
                    log.error("Proveedor no encontrado: {}", dto.getIdProveedor());
                    return new RuntimeException("Proveedor no encontrado");
                });

        Compra compra = new Compra();
        compra.setTipoComprobante(dto.getTipoComprobante());
        compra.setNroComprobante(dto.getNroComprobante());
        compra.setNroGuia(dto.getNroGuia());
        compra.setTipoPago(dto.getTipoPago());
        compra.setFechaTransaccion(dto.getFechaTransaccion());
        compra.setMontoCompra(dto.getMontoCompra());
        compra.setEstado(1);
        compra.setUsuario(usuario);
        compra.setProveedor(proveedor);

        for (DetalleCompraDTO item : dto.getDetalleCompras()) {
            Producto producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> {
                        log.error("Producto no encontrado: {}", item.getIdProducto());
                        return new RuntimeException("Producto no encontrado: " + item.getIdProducto());
                    });

            int stockAntes = producto.getStock() == null ? 0 : producto.getStock();
            int cantidad = item.getCantidad() == null ? 0 : item.getCantidad();
            int stockDespues = stockAntes + cantidad;

            producto.setStock(stockDespues);
            producto.setPrecioCompra(item.getPrecioUnitario());
            productoRepository.save(producto);

            movimientoStockRepository.save(MovimientoStock.builder()
                .tipoMovimiento("CARGO")
                .cantidad(cantidad)
                .nroFraccion(producto.getNroFraccion())
                .stockAntes(stockAntes)
                .stockDespues(stockDespues)
                .idZona(usuario.getCliente().getZona().getIdZona())
                .idReferencia(compra.getIdCompra())
                .usuario(usuario.getNombre())
                .producto(producto.getNombreProducto())
                .motivo("COMPRA")
                .build());

            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compra);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detalle.setSubtotal(item.getSubtotal());
            compra.getDetalleCompras().add(detalle);
        }

        log.info("Compra creada correctamente para usuario: {}", usuario.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(compraRepository.save(compra)));
    }

    @GetMapping("/fecha/{fecha}")
        public ResponseEntity<List<CompraDTO>> listarPorFecha(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                   @RequestParam Integer idZona) {
        log.info("Listando compras para zona: {} en fecha: {}", idZona, fecha);
        return ResponseEntity.ok(compraRepository.findByZonaAndFecha(idZona, fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX))
            .stream().map(this::toDTO).collect(Collectors.toList()));
        }

    @GetMapping("/rango")
        public ResponseEntity<List<CompraDTO>> listarPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaIni,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam Integer idZona) {
        log.info("Listando compras para zona: {} entre {} y {}", idZona, fechaIni, fechaFin);
        return ResponseEntity.ok(compraRepository.findByZonaAndFecha(idZona, fechaIni.atStartOfDay(), fechaFin.atTime(LocalTime.MAX))
            .stream().map(this::toDTO).collect(Collectors.toList()));
        }

    private CompraDTO toDTO(Compra compra) {
        CompraDTO dto = new CompraDTO();
        dto.setIdCompra(compra.getIdCompra());
        dto.setFechaTransaccion(compra.getFechaTransaccion());
        dto.setTipoComprobante(compra.getTipoComprobante());
        dto.setNroComprobante(compra.getNroComprobante());
        dto.setNroGuia(compra.getNroGuia());
        dto.setTipoPago(compra.getTipoPago());
        dto.setMontoCompra(compra.getMontoCompra());
        dto.setEstado(compra.getEstado());
        dto.setIdProveedor(compra.getProveedor().getIdProveedor());
        dto.setNombreProveedor(compra.getProveedor().getNombreProveedor());
        dto.setDetalleCompras(compra.getDetalleCompras().stream().map(d -> {
            DetalleCompraDTO it = new DetalleCompraDTO();
            it.setIdProducto(d.getProducto().getIdProducto());
            it.setNombreProducto(d.getProducto().getNombreProducto());
            it.setCantidad(d.getCantidad());
            it.setPrecioUnitario(d.getPrecioUnitario());
            it.setSubtotal(d.getSubtotal());
            return it;
        }).collect(Collectors.toList()));
        return dto;
    }
}
