package com.magm.controller;

import com.magm.dto.ProductoDTO;
import com.magm.entity.Laboratorio;
import com.magm.entity.MovimientoStock;
import com.magm.entity.Producto;
import com.magm.entity.Zona;
import com.magm.repository.LaboratorioRepository;
import com.magm.repository.MovimientoStockRepository;
import com.magm.repository.ProductoRepository;
import com.magm.repository.ZonaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final ZonaRepository zonaRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listar(@RequestParam Integer idZona) {
        log.info("Listando productos activos para idZona={}", idZona);
        List<ProductoDTO> productos = productoRepository.findByZonaIdZonaAndEstadoOrderByNombreProductoAsc(idZona, 1)
            .stream().map(this::toDTO).collect(Collectors.toList());
        log.info("Listado completado idZona={} total={}", idZona, productos.size());
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoDTO>> buscar(
            @RequestParam String nombre,
            @RequestParam Integer idZona) {
        log.info("Buscando productos por nombre='{}' en idZona={}", nombre, idZona);
        List<ProductoDTO> productos = productoRepository
            .findByNombreProductoContainingIgnoreCaseAndZonaIdZonaAndEstado(nombre, idZona, 1)
            .stream().map(this::toDTO).collect(Collectors.toList());
        log.info("Busqueda completada nombre='{}' idZona={} total={}", nombre, idZona, productos.size());
        return ResponseEntity.ok(productos);
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> crear(@Valid @RequestBody ProductoDTO dto,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creando producto nombre='{}' idZona={} idLaboratorio={}",
            dto.getNombreProducto(), dto.getIdZona(), dto.getIdLaboratorio());

        Zona zona = zonaRepository.findById(dto.getIdZona())
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        Laboratorio laboratorio = null;
        if (dto.getIdLaboratorio() != null) {
            laboratorio = laboratorioRepository.findById(dto.getIdLaboratorio())
                    .orElseThrow(() -> new RuntimeException("Laboratorio no encontrado"));
        }

        Producto producto = new Producto();
        producto.setNombreProducto(dto.getNombreProducto());
        producto.setNroFraccion(dto.getNroFraccion());
        producto.setUnidades(dto.getUnidades());
        producto.setFraccion(dto.getFraccion());
        producto.setLote(dto.getLote());
        producto.setPrecio(dto.getPrecio());
        producto.setPrecioCompra(dto.getPrecioCompra());
        producto.setPorcentajeGanancia(dto.getPorcentajeGanancia());
        producto.setPrecioVentaCaja(dto.getPrecioVentaCaja());
        producto.setStock(dto.getStock());
        producto.setStockMinimo(dto.getStockMinimo());
        producto.setUnidad(dto.getUnidad());
        producto.setPresentacion(dto.getPresentacion());
        producto.setNroBlister(dto.getNroBlister());
        producto.setPrecioBlister(dto.getPrecioBlister());
        producto.setUbicacion(dto.getUbicacion());
        producto.setCodigoBarras(dto.getCodigoBarras());
        producto.setCodigoDigemid(dto.getCodigoDigemid());
        producto.setFechaAdquisicion(parseDate(dto.getFechaAdquisicion()));
        producto.setFechaVencimiento(parseDate(dto.getFechaVencimiento()));
        producto.setDescripcion(dto.getDescripcion());

        normalizarPresentacion(producto);
        int totalFracciones = calcularTotalFracciones(producto.getNroFraccion(), producto.getUnidades(), producto.getFraccion());
        producto.setStock(totalFracciones);

        producto.setEstado(1);
        producto.setLaboratorio(laboratorio);
        producto.setZona(zona);
        Producto guardado = productoRepository.save(producto);

        // Registro de cargo por ingreso inicial de stock
        if (totalFracciones > 0) {
            movimientoStockRepository.save(MovimientoStock.builder()
                    .tipoMovimiento("CARGO")
                .cantidad(totalFracciones)
                    .nroFraccion(guardado.getNroFraccion())
                    .stockAntes(0)
                    .stockDespues(totalFracciones)
                    .idZona(zona.getIdZona())
                    .idReferencia(guardado.getIdProducto())
                    .usuario(userDetails != null ? userDetails.getUsername() : null)
                    .producto(guardado.getNombreProducto())
                    .motivo("CREACION_PRODUCTO")
                    .build());
        }

        log.info("Producto creado idProducto={} nombre='{}' idZona={}",
            guardado.getIdProducto(), guardado.getNombreProducto(), dto.getIdZona());

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizar(@PathVariable Integer id,
                                                  @RequestParam Integer idZona,
                                                  @Valid @RequestBody ProductoDTO dto,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        log.info("=== INICIO PUT PRODUCTO === idProducto={} idZona={}", id, idZona);
        
        try {
            log.info("Buscando producto en BD...");
            Producto producto = productoRepository.findByIdProductoAndZonaIdZona(id, idZona)
                    .orElseThrow(() -> {
                        log.error("Producto no encontrado: idProducto={} idZona={}", id, idZona);
                        return new RuntimeException("Producto no encontrado");
                    });
            log.info("Producto encontrado. Zona cargada: {} Laboratorio: {}", 
                producto.getZona() != null ? producto.getZona().getIdZona() : "NULL", 
                producto.getLaboratorio() != null ? producto.getLaboratorio().getIdLaboratorio() : "NULL");

            Laboratorio laboratorio = null;
            if (dto.getIdLaboratorio() != null) {
                log.info("Buscando laboratorio idLaboratorio={}", dto.getIdLaboratorio());
                laboratorio = laboratorioRepository.findById(dto.getIdLaboratorio())
                        .orElseThrow(() -> {
                            log.error("Laboratorio no encontrado: idLaboratorio={}", dto.getIdLaboratorio());
                            return new RuntimeException("Laboratorio no encontrado");
                        });
                log.info("Laboratorio encontrado: {}", laboratorio.getNombreLaboratorio());
            }

            log.info("Actualizando campos del producto...");
            int stockAnterior = calcularTotalFracciones(producto.getNroFraccion(), producto.getUnidades(), producto.getFraccion());
            if (stockAnterior == 0 && producto.getStock() != null) {
                stockAnterior = Math.max(0, producto.getStock());
            }
            producto.setNombreProducto(dto.getNombreProducto());
            producto.setNroFraccion(dto.getNroFraccion());
            producto.setUnidades(dto.getUnidades());
            producto.setFraccion(dto.getFraccion());
            producto.setLote(dto.getLote());
            producto.setPrecio(dto.getPrecio());
            producto.setPrecioCompra(dto.getPrecioCompra());
            producto.setPorcentajeGanancia(dto.getPorcentajeGanancia());
            producto.setPrecioVentaCaja(dto.getPrecioVentaCaja());
            producto.setStock(dto.getStock());
            producto.setStockMinimo(dto.getStockMinimo());
            producto.setUnidad(dto.getUnidad());
            producto.setPresentacion(dto.getPresentacion());
            producto.setNroBlister(dto.getNroBlister());
            producto.setPrecioBlister(dto.getPrecioBlister());
            producto.setUbicacion(dto.getUbicacion());
            producto.setCodigoBarras(dto.getCodigoBarras());
            producto.setCodigoDigemid(dto.getCodigoDigemid());
            producto.setFechaAdquisicion(parseDate(dto.getFechaAdquisicion()));
            producto.setFechaVencimiento(parseDate(dto.getFechaVencimiento()));
            producto.setDescripcion(dto.getDescripcion());
            producto.setLaboratorio(laboratorio);
            producto.setEstado(dto.getEstado() == null ? producto.getEstado() : dto.getEstado());

            normalizarPresentacion(producto);
            int stockNuevo = calcularTotalFracciones(producto.getNroFraccion(), producto.getUnidades(), producto.getFraccion());
            producto.setStock(stockNuevo);
            
            log.info("Guardando producto en BD...");
            Producto actualizado = productoRepository.save(producto);
            log.info("Producto guardado. Recargando desde BD con EntityGraph...");
            
            // Recargar el producto con EntityGraph para asegurar que zona y laboratorio están cargados
            Producto recargado = productoRepository.findByIdProductoAndZonaIdZona(actualizado.getIdProducto(), idZona)
                    .orElseThrow(() -> {
                        log.error("Error recargando producto guardado: idProducto={}", actualizado.getIdProducto());
                        return new RuntimeException("Error recargando producto");
                    });

            int delta = stockNuevo - stockAnterior;
            if (delta != 0) {
                movimientoStockRepository.save(MovimientoStock.builder()
                        .tipoMovimiento(delta > 0 ? "CARGO" : "DESCARGO")
                        .tipoDescargo(delta < 0 ? "MOD_STOCK" : null)
                        .cantidad(Math.abs(delta))
                        .nroFraccion(recargado.getNroFraccion())
                        .stockAntes(stockAnterior)
                        .stockDespues(stockNuevo)
                        .idZona(idZona)
                        .idReferencia(recargado.getIdProducto())
                        .usuario(userDetails != null ? userDetails.getUsername() : null)
                        .producto(recargado.getNombreProducto())
                        .motivo("MODIFICACION_STOCK")
                        .build());
            }
            
            log.info("Producto recargado. Convirtiendo a DTO...");
            ProductoDTO resultado = toDTO(recargado);
            log.info("=== FIN PUT PRODUCTO === idProducto={} exitoso", id);
            return ResponseEntity.ok(resultado);
            
        } catch (Exception ex) {
            log.error("ERROR en PUT PRODUCTO idProducto={} idZona={}: {}", id, idZona, ex.getMessage(), ex);
            throw ex;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id,
                                         @RequestParam Integer idZona,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        log.info("=== INICIO DELETE PRODUCTO === idProducto={} idZona={}", id, idZona);
        
        try {
            log.info("Buscando producto para eliminar...");
            Producto producto = productoRepository.findByIdProductoAndZonaIdZona(id, idZona)
                    .orElseThrow(() -> {
                        log.error("Producto no encontrado para eliminar: idProducto={} idZona={}", id, idZona);
                        return new RuntimeException("Producto no encontrado");
                    });
            
            log.info("Producto encontrado. Marcando como estado=0...");
            int stockActual = producto.getStock() == null ? 0 : producto.getStock();
            int totalActual = calcularTotalFracciones(producto.getNroFraccion(), producto.getUnidades(), producto.getFraccion());
            if (totalActual == 0) {
                totalActual = Math.max(0, stockActual);
            }
            if (totalActual > 0) {
                movimientoStockRepository.save(MovimientoStock.builder()
                        .tipoMovimiento("DESCARGO")
                        .tipoDescargo("MOD_STOCK")
                        .cantidad(totalActual)
                        .nroFraccion(producto.getNroFraccion())
                        .stockAntes(totalActual)
                        .stockDespues(0)
                        .idZona(idZona)
                        .idReferencia(producto.getIdProducto())
                    .usuario(userDetails != null ? userDetails.getUsername() : null)
                        .producto(producto.getNombreProducto())
                        .motivo("ELIMINACION_PRODUCTO")
                        .build());
            }
            producto.setUnidades(0);
            producto.setFraccion(0);
            producto.setStock(0);
            producto.setEstado(0);
            productoRepository.save(producto);
            log.info("=== FIN DELETE PRODUCTO === idProducto={} eliminado logicamente", id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            log.error("ERROR en DELETE PRODUCTO idProducto={} idZona={}: {}", id, idZona, ex.getMessage(), ex);
            throw ex;
        }
    }

    private ProductoDTO toDTO(Producto p) {
        ProductoDTO dto = new ProductoDTO();
        dto.setIdProducto(p.getIdProducto());
        dto.setNombreProducto(p.getNombreProducto());
        dto.setNroFraccion(p.getNroFraccion());
        dto.setUnidades(p.getUnidades());
        dto.setFraccion(p.getFraccion());
        dto.setLote(p.getLote());
        dto.setPrecio(p.getPrecio());
        dto.setPrecioCompra(p.getPrecioCompra());
        dto.setPorcentajeGanancia(p.getPorcentajeGanancia());
        dto.setPrecioVentaCaja(p.getPrecioVentaCaja());
        dto.setStock(p.getStock());
        dto.setStockMinimo(p.getStockMinimo());
        dto.setUnidad(p.getUnidad());
        dto.setPresentacion(p.getPresentacion());
        dto.setNroBlister(p.getNroBlister());
        dto.setPrecioBlister(p.getPrecioBlister());
        dto.setUbicacion(p.getUbicacion());
        dto.setCodigoBarras(p.getCodigoBarras());
        dto.setCodigoDigemid(p.getCodigoDigemid());
        dto.setFechaAdquisicion(p.getFechaAdquisicion() != null ? p.getFechaAdquisicion().toString() : null);
        dto.setFechaVencimiento(p.getFechaVencimiento() != null ? p.getFechaVencimiento().toString() : null);
        dto.setDescripcion(p.getDescripcion());
        dto.setEstado(p.getEstado());
        if (p.getLaboratorio() != null) {
            dto.setIdLaboratorio(p.getLaboratorio().getIdLaboratorio());
            dto.setNombreLaboratorio(p.getLaboratorio().getNombreLaboratorio());
        }
        dto.setIdZona(p.getZona() != null ? p.getZona().getIdZona() : null);
        return dto;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private void normalizarPresentacion(Producto producto) {
        int nroFraccion = sanitizeNroFraccion(producto.getNroFraccion());
        int unidades = Math.max(0, safeInt(producto.getUnidades()));
        int fraccion = Math.max(0, safeInt(producto.getFraccion()));

        if (nroFraccion > 1) {
            unidades += fraccion / nroFraccion;
            fraccion = fraccion % nroFraccion;
        } else {
            unidades += fraccion;
            fraccion = 0;
        }

        producto.setNroFraccion(nroFraccion);
        producto.setUnidades(unidades);
        producto.setFraccion(fraccion);
    }

    private int calcularTotalFracciones(Integer nroFraccion, Integer unidades, Integer fraccion) {
        int nf = sanitizeNroFraccion(nroFraccion);
        int u = Math.max(0, safeInt(unidades));
        int f = Math.max(0, safeInt(fraccion));
        if (nf > 1) {
            return (u * nf) + f;
        }
        return u + f;
    }

    private int sanitizeNroFraccion(Integer nroFraccion) {
        int nf = safeInt(nroFraccion);
        return nf <= 0 ? 1 : nf;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
