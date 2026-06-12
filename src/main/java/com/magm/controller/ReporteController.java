package com.magm.controller;

import com.magm.repository.ProductoRepository;
import com.magm.repository.CompraRepository;
import com.magm.repository.IncentivoRepository;
import com.magm.repository.MovimientoStockRepository;
import com.magm.repository.SalidaRepository;
import com.magm.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteController {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
        private final CompraRepository compraRepository;
        private final IncentivoRepository incentivoRepository;
        private final SalidaRepository salidaRepository;
        private final MovimientoStockRepository movimientoStockRepository;

        private LocalDateTime startOfDay(String fechaInicio) {
                LocalDate day = fechaInicio == null ? LocalDate.now() : LocalDate.parse(fechaInicio);
                return day.atStartOfDay();
        }

        private LocalDateTime endOfDay(String fechaFin) {
                LocalDate day = fechaFin == null ? LocalDate.now() : LocalDate.parse(fechaFin);
                return day.atTime(LocalTime.MAX);
        }

    @GetMapping("/resumen-diario")
    public ResponseEntity<Map<String, Object>> resumenDiario(@RequestParam Integer idZona,
                                                             @RequestParam(required = false) String fecha) {
        LocalDate day = fecha == null ? LocalDate.now() : LocalDate.parse(fecha);
        LocalDateTime ini = day.atStartOfDay();
        LocalDateTime fin = day.atTime(LocalTime.MAX);

        var ventas = ventaRepository.findByZonaAndFecha(idZona, ini, fin);
        BigDecimal total = ventas.stream()
                .map(v -> v.getMontoVenta() == null ? BigDecimal.ZERO : v.getMontoVenta())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> payload = new HashMap<>();
        payload.put("fecha", day.toString());
        payload.put("cantidadVentas", ventas.size());
        payload.put("montoTotal", total);
        payload.put("productosBajoStock", productoRepository.findByZonaIdZonaAndEstadoOrderByNombreProductoAsc(idZona, 1)
                .stream().filter(p -> p.getStock() != null && p.getStockMinimo() != null && p.getStock() <= p.getStockMinimo()).count());

                var detalleVentas = ventas.stream().map(v -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("correlativoVenta", v.getIdVenta());
                        row.put("idVenta", v.getIdVenta());
                        row.put("fecha", v.getFechaTransaccion());
                        row.put("usuario", v.getUsuario() == null ? null : v.getUsuario().getNombre());
                        row.put("tipoPago", v.getTipoPago());
                        row.put("montoVenta", v.getMontoVenta());
                        row.put("estado", v.getEstado());
                        row.put("detalles", v.getDetalleVentas().stream().map(d -> {
                                Map<String, Object> det = new HashMap<>();
                                det.put("idProducto", d.getProducto() == null ? null : d.getProducto().getIdProducto());
                                det.put("nombreProducto", d.getProducto() == null ? null : d.getProducto().getNombreProducto());
                                det.put("cantidad", d.getCantidad());
                                det.put("precioUnitario", d.getPrecioUnitario());
                                det.put("subtotal", d.getSubtotal());
                                return det;
                        }).toList());
                        return row;
                }).toList();

                payload.put("ventas", detalleVentas);
        return ResponseEntity.ok(payload);
    }

        @GetMapping("/ventas")
        public ResponseEntity<Map<String, Object>> ventas(@RequestParam Integer idZona,
                                                                                                          @RequestParam(required = false) String fechaInicio,
                                                                                                          @RequestParam(required = false) String fechaFin) {
                LocalDateTime ini = startOfDay(fechaInicio);
                LocalDateTime fin = endOfDay(fechaFin);
                var ventas = ventaRepository.findByZonaAndFecha(idZona, ini, fin);

                BigDecimal total = ventas.stream()
                                .map(v -> v.getMontoVenta() == null ? BigDecimal.ZERO : v.getMontoVenta())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalEfectivo = ventas.stream()
                                .filter(v -> Integer.valueOf(1).equals(v.getTipoPago()))
                                .map(v -> v.getMontoVenta() == null ? BigDecimal.ZERO : v.getMontoVenta())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalTarjeta = ventas.stream()
                                .filter(v -> !Integer.valueOf(1).equals(v.getTipoPago()))
                                .map(v -> v.getMontoVenta() == null ? BigDecimal.ZERO : v.getMontoVenta())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal utilidad = ventas.stream().flatMap(v -> v.getDetalleVentas().stream())
                                .map(d -> {
                                        BigDecimal precioVenta = d.getPrecioUnitario() == null ? BigDecimal.ZERO : d.getPrecioUnitario();
                                        BigDecimal precioCosto = d.getProducto() != null && d.getProducto().getPrecioCompra() != null
                                                        ? d.getProducto().getPrecioCompra()
                                                        : BigDecimal.ZERO;
                                        int cantidad = d.getCantidad() == null ? 0 : d.getCantidad();
                                        return precioVenta.subtract(precioCosto).multiply(BigDecimal.valueOf(cantidad));
                                })
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                var items = ventas.stream().map(v -> {
                        Map<String, Object> item = new HashMap<>();
                        int cantidadItems = v.getDetalleVentas() == null ? 0 : v.getDetalleVentas().size();
                        int unidadesVendidas = v.getDetalleVentas() == null ? 0 : v.getDetalleVentas().stream()
                                        .map(d -> d.getCantidad() == null ? 0 : d.getCantidad())
                                        .reduce(0, Integer::sum);
                        BigDecimal utilidadVenta = v.getDetalleVentas() == null ? BigDecimal.ZERO : v.getDetalleVentas().stream()
                                        .map(d -> {
                                                BigDecimal pv = d.getPrecioUnitario() == null ? BigDecimal.ZERO : d.getPrecioUnitario();
                                                BigDecimal pc = d.getProducto() != null && d.getProducto().getPrecioCompra() != null
                                                                ? d.getProducto().getPrecioCompra()
                                                                : BigDecimal.ZERO;
                                                int cant = d.getCantidad() == null ? 0 : d.getCantidad();
                                                return pv.subtract(pc).multiply(BigDecimal.valueOf(cant));
                                        })
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        item.put("idVenta", v.getIdVenta());
                        item.put("fecha", v.getFechaTransaccion());
                        item.put("usuario", v.getUsuario() == null ? null : v.getUsuario().getNombre());
                        item.put("tipoPago", v.getTipoPago());
                        item.put("montoVenta", v.getMontoVenta());
                        item.put("estado", v.getEstado());
                        item.put("cantidadItems", cantidadItems);
                        item.put("unidadesVendidas", unidadesVendidas);
                        item.put("utilidad", utilidadVenta);
                        return item;
                }).toList();

                Map<String, Object> payload = new HashMap<>();
                payload.put("items", items);
                payload.put("totalVentas", ventas.size());
                payload.put("montoTotal", total);
                payload.put("montoEfectivo", totalEfectivo);
                payload.put("montoTarjeta", totalTarjeta);
                payload.put("utilidadTotal", utilidad);
                return ResponseEntity.ok(payload);
        }

        @GetMapping("/vendedores")
        public ResponseEntity<List<Map<String, Object>>> vendedores(@RequestParam Integer idZona,
                                                                                                                                @RequestParam(required = false) String fechaInicio,
                                                                                                                                @RequestParam(required = false) String fechaFin) {
                LocalDateTime ini = startOfDay(fechaInicio);
                LocalDateTime fin = endOfDay(fechaFin);
                var ventas = ventaRepository.findByZonaAndFecha(idZona, ini, fin);

                Map<Integer, Map<String, Object>> agregados = new LinkedHashMap<>();
                for (var venta : ventas) {
                        if (venta.getUsuario() == null) {
                                continue;
                        }
                        Integer idUsuario = venta.getUsuario().getId();
                        agregados.putIfAbsent(idUsuario, new HashMap<>());
                        Map<String, Object> row = agregados.get(idUsuario);
                        row.put("idUsuario", idUsuario);
                        row.put("usuario", venta.getUsuario().getNombre());

                        Integer cantidadVentas = (Integer) row.getOrDefault("cantidadVentas", 0);
                        BigDecimal montoTotal = (BigDecimal) row.getOrDefault("montoTotal", BigDecimal.ZERO);
                        BigDecimal utilidad = (BigDecimal) row.getOrDefault("utilidadTotal", BigDecimal.ZERO);

                        BigDecimal montoVenta = venta.getMontoVenta() == null ? BigDecimal.ZERO : venta.getMontoVenta();
                        BigDecimal utilidadVenta = venta.getDetalleVentas().stream().map(d -> {
                                BigDecimal pv = d.getPrecioUnitario() == null ? BigDecimal.ZERO : d.getPrecioUnitario();
                                BigDecimal pc = d.getProducto() != null && d.getProducto().getPrecioCompra() != null
                                                ? d.getProducto().getPrecioCompra()
                                                : BigDecimal.ZERO;
                                int cant = d.getCantidad() == null ? 0 : d.getCantidad();
                                return pv.subtract(pc).multiply(BigDecimal.valueOf(cant));
                        }).reduce(BigDecimal.ZERO, BigDecimal::add);

                        row.put("cantidadVentas", cantidadVentas + 1);
                        row.put("montoTotal", montoTotal.add(montoVenta));
                        row.put("utilidadTotal", utilidad.add(utilidadVenta));
                }

                var result = agregados.values().stream()
                                .sorted((a, b) -> ((BigDecimal) b.getOrDefault("montoTotal", BigDecimal.ZERO))
                                                .compareTo((BigDecimal) a.getOrDefault("montoTotal", BigDecimal.ZERO)))
                                .toList();

                return ResponseEntity.ok(result);
        }

        @GetMapping("/inventario")
        public ResponseEntity<Map<String, Object>> inventario(@RequestParam Integer idZona) {
                var productos = productoRepository.findByZonaIdZonaAndEstadoOrderByNombreProductoAsc(idZona, 1);

                BigDecimal totalCapital = BigDecimal.ZERO;
                BigDecimal totalVenta = BigDecimal.ZERO;
                List<Map<String, Object>> items = productos.stream().map(p -> {
                        int stock = p.getStock() == null ? 0 : p.getStock();
                        BigDecimal precioCompra = p.getPrecioCompra() == null ? BigDecimal.ZERO : p.getPrecioCompra();
                        BigDecimal precioVenta = p.getPrecio() == null ? BigDecimal.ZERO : p.getPrecio();
                        BigDecimal capital = precioCompra.multiply(BigDecimal.valueOf(stock));
                        BigDecimal venta = precioVenta.multiply(BigDecimal.valueOf(stock));

                        Map<String, Object> row = new HashMap<>();
                        row.put("idProducto", p.getIdProducto());
                        row.put("nombreProducto", p.getNombreProducto());
                        row.put("laboratorio", p.getLaboratorio() == null ? null : p.getLaboratorio().getNombreLaboratorio());
                        row.put("unidades", p.getUnidades());
                        row.put("fraccion", p.getFraccion());
                        row.put("stock", stock);
                        row.put("precioCompra", precioCompra);
                        row.put("precioVenta", precioVenta);
                        row.put("capital", capital);
                        row.put("venta", venta);
                        row.put("ubicacion", p.getUbicacion());
                        row.put("fechaVencimiento", p.getFechaVencimiento());
                        row.put("stockMinimo", p.getStockMinimo());
                        return row;
                }).toList();

                for (var row : items) {
                        totalCapital = totalCapital.add((BigDecimal) row.get("capital"));
                        totalVenta = totalVenta.add((BigDecimal) row.get("venta"));
                }

                Map<String, Object> payload = new HashMap<>();
                payload.put("items", items);
                payload.put("totalCapital", totalCapital);
                payload.put("totalVenta", totalVenta);
                payload.put("totalUtilidad", totalVenta.subtract(totalCapital));
                return ResponseEntity.ok(payload);
        }

        @GetMapping("/productos-por-vencer")
        public ResponseEntity<List<Map<String, Object>>> productosPorVencer(@RequestParam Integer idZona,
                                                                                                                                                @RequestParam(defaultValue = "30") Integer dias,
                                                                                                                                                @RequestParam(defaultValue = "100") Integer limite) {
                LocalDate hoy = LocalDate.now();
                LocalDate tope = hoy.plusDays(Math.max(0, dias));

                var rows = productoRepository.findByZonaIdZonaAndEstadoOrderByNombreProductoAsc(idZona, 1)
                                .stream()
                                .filter(p -> p.getFechaVencimiento() != null)
                                .filter(p -> !p.getFechaVencimiento().isAfter(tope))
                                .sorted(Comparator.comparing(p -> p.getFechaVencimiento()))
                                .limit(Math.max(1, limite))
                                .map(p -> {
                                        Map<String, Object> item = new HashMap<>();
                                        item.put("idProducto", p.getIdProducto());
                                        item.put("nombreProducto", p.getNombreProducto());
                                        item.put("stock", p.getStock());
                                        item.put("fraccion", p.getFraccion());
                                        item.put("fechaVencimiento", p.getFechaVencimiento());
                                        item.put("diasRestantes", ChronoUnit.DAYS.between(hoy, p.getFechaVencimiento()));
                                        return item;
                                })
                                .toList();

                return ResponseEntity.ok(rows);
        }

        @GetMapping("/compras-por-proveedor")
        public ResponseEntity<Map<String, Object>> comprasPorProveedor(@RequestParam Integer idZona,
                                                                                                                                   @RequestParam(required = false) Integer idProveedor,
                                                                                                                                   @RequestParam(required = false) String fechaInicio,
                                                                                                                                   @RequestParam(required = false) String fechaFin) {
                LocalDateTime ini = startOfDay(fechaInicio);
                LocalDateTime fin = endOfDay(fechaFin);

                var compras = compraRepository.findByZonaAndFecha(idZona, ini, fin).stream()
                                .filter(c -> idProveedor == null || (c.getProveedor() != null && Objects.equals(c.getProveedor().getIdProveedor(), idProveedor)))
                                .toList();

                var items = compras.stream().map(c -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("idCompra", c.getIdCompra());
                        row.put("fecha", c.getFechaTransaccion());
                        row.put("proveedor", c.getProveedor() == null ? null : c.getProveedor().getNombreProveedor());
                        row.put("tipoComprobante", c.getTipoComprobante());
                        row.put("nroComprobante", c.getNroComprobante());
                        row.put("nroGuia", c.getNroGuia());
                        row.put("tipoPago", c.getTipoPago());
                        row.put("monto", c.getMontoCompra());
                        return row;
                }).toList();

                BigDecimal total = compras.stream()
                                .map(c -> c.getMontoCompra() == null ? BigDecimal.ZERO : c.getMontoCompra())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> payload = new HashMap<>();
                payload.put("items", items);
                payload.put("cantidadCompras", compras.size());
                payload.put("montoTotal", total);
                return ResponseEntity.ok(payload);
        }

        @GetMapping("/incentivos")
        public ResponseEntity<Map<String, Object>> incentivos(@RequestParam Integer idZona,
                                                                                                                  @RequestParam(required = false) String fechaInicio,
                                                                                                                  @RequestParam(required = false) String fechaFin) {
                LocalDateTime ini = startOfDay(fechaInicio);
                LocalDateTime fin = endOfDay(fechaFin);

                var incentivos = incentivoRepository.findByUsuarioClienteZonaIdZonaOrderByFechaDesc(idZona).stream()
                                .filter(i -> i.getFecha() != null && !i.getFecha().isBefore(ini) && !i.getFecha().isAfter(fin))
                                .toList();

                BigDecimal total = incentivos.stream()
                                .map(i -> i.getMonto() == null ? BigDecimal.ZERO : i.getMonto())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                var items = incentivos.stream().map(i -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("idIncentivo", i.getIdIncentivo());
                        row.put("fecha", i.getFecha());
                        row.put("usuario", i.getUsuario() == null ? null : i.getUsuario().getNombre());
                        row.put("monto", i.getMonto());
                        row.put("descripcion", i.getDescripcion());
                        return row;
                }).toList();

                Map<String, Object> payload = new HashMap<>();
                payload.put("items", items);
                payload.put("montoTotal", total);
                return ResponseEntity.ok(payload);
        }

    @GetMapping("/cargos-descargos")
    public ResponseEntity<Map<String, Object>> cargosDescargos(@RequestParam Integer idZona,
                                                                @RequestParam(required = false) String fechaInicio,
                                                                @RequestParam(required = false) String fechaFin) {
        LocalDateTime ini = startOfDay(fechaInicio);
        LocalDateTime fin = endOfDay(fechaFin);

        var ventas = ventaRepository.findByZonaAndFecha(idZona, ini, fin);
        var movimientos = movimientoStockRepository.findByIdZonaAndFechaBetweenOrderByFechaDesc(idZona, ini, fin);
        var compras = compraRepository.findByZonaAndFecha(idZona, ini, fin);
        var salidas = salidaRepository.findByZonaAndFecha(idZona, ini, fin);

                // Cargos por ingresos o ajustes positivos de stock
                var cargos = movimientos.stream()
                                .filter(m -> "CARGO".equalsIgnoreCase(m.getTipoMovimiento()))
                                .map(m -> {
            Map<String, Object> row = new HashMap<>();
                        row.put("tipoMovimiento", m.getTipoMovimiento());
                        row.put("idReferencia", m.getIdReferencia());
                        row.put("fecha", m.getFecha());
                        row.put("usuario", m.getUsuario());
                        row.put("producto", m.getProducto());
                        row.put("cantidad", m.getCantidad());
                        row.put("motivo", m.getMotivo());
                                                int nf = m.getNroFraccion() == null ? 1 : Math.max(1, m.getNroFraccion());
                                                int cantidad = m.getCantidad() == null ? 0 : m.getCantidad();
                                                int stockAntes = m.getStockAntes() == null ? 0 : m.getStockAntes();
                                                int stockDespues = m.getStockDespues() == null ? 0 : m.getStockDespues();
                                                row.put("nroFraccion", nf);
                                                row.put("cajas", nf > 1 ? cantidad / nf : cantidad);
                                                row.put("fracciones", nf > 1 ? cantidad % nf : 0);
                                                row.put("stockAntes", stockAntes);
                                                row.put("stockDespues", stockDespues);
                                                row.put("stockAntesCajas", nf > 1 ? stockAntes / nf : stockAntes);
                                                row.put("stockAntesFracciones", nf > 1 ? stockAntes % nf : 0);
                                                row.put("stockDespuesCajas", nf > 1 ? stockDespues / nf : stockDespues);
                                                row.put("stockDespuesFracciones", nf > 1 ? stockDespues % nf : 0);
            return row;
                }).toList();

                // Compatibilidad para datos historicos sin tabla de movimientos
                if (cargos.isEmpty()) {
                        cargos = compras.stream().flatMap(c -> c.getDetalleCompras().stream().map(d -> {
                                Map<String, Object> row = new HashMap<>();
                                row.put("tipoMovimiento", "CARGO");
                                row.put("idReferencia", c.getIdCompra());
                                row.put("fecha", c.getFechaTransaccion());
                                row.put("usuario", c.getUsuario() == null ? null : c.getUsuario().getNombre());
                                row.put("producto", d.getProducto() == null ? null : d.getProducto().getNombreProducto());
                                row.put("cantidad", d.getCantidad());
                                row.put("motivo", "COMPRA");
                                int nf = d.getProducto() != null && d.getProducto().getNroFraccion() != null ? Math.max(1, d.getProducto().getNroFraccion()) : 1;
                                int cantidad = d.getCantidad() == null ? 0 : d.getCantidad();
                                row.put("nroFraccion", nf);
                                row.put("cajas", nf > 1 ? cantidad / nf : cantidad);
                                row.put("fracciones", nf > 1 ? cantidad % nf : 0);
                                row.put("stockAntes", null);
                                row.put("stockDespues", null);
                                row.put("stockAntesCajas", null);
                                row.put("stockAntesFracciones", null);
                                row.put("stockDespuesCajas", null);
                                row.put("stockDespuesFracciones", null);
                                return row;
                        })).toList();
                }

                // Descargos por modificacion de stock: ajustes negativos y eliminaciones
                var descargosModStock = movimientos.stream()
                                .filter(m -> "DESCARGO".equalsIgnoreCase(m.getTipoMovimiento()))
                                .filter(m -> "MOD_STOCK".equalsIgnoreCase(m.getTipoDescargo()))
                                .map(m -> {
            Map<String, Object> row = new HashMap<>();
                        row.put("tipoDescargo", "MOD_STOCK");
                        row.put("idReferencia", m.getIdReferencia());
                        row.put("fecha", m.getFecha());
                        row.put("usuario", m.getUsuario());
                        row.put("producto", m.getProducto());
                        row.put("cantidad", m.getCantidad());
                        row.put("motivo", m.getMotivo());
                                                int nf = m.getNroFraccion() == null ? 1 : Math.max(1, m.getNroFraccion());
                                                int cantidad = m.getCantidad() == null ? 0 : m.getCantidad();
                                                int stockAntes = m.getStockAntes() == null ? 0 : m.getStockAntes();
                                                int stockDespues = m.getStockDespues() == null ? 0 : m.getStockDespues();
                                                row.put("nroFraccion", nf);
                                                row.put("cajas", nf > 1 ? cantidad / nf : cantidad);
                                                row.put("fracciones", nf > 1 ? cantidad % nf : 0);
                                                row.put("stockAntes", stockAntes);
                                                row.put("stockDespues", stockDespues);
                                                row.put("stockAntesCajas", nf > 1 ? stockAntes / nf : stockAntes);
                                                row.put("stockAntesFracciones", nf > 1 ? stockAntes % nf : 0);
                                                row.put("stockDespuesCajas", nf > 1 ? stockDespues / nf : stockDespues);
                                                row.put("stockDespuesFracciones", nf > 1 ? stockDespues % nf : 0);
            return row;
                }).toList();

                if (descargosModStock.isEmpty()) {
                        descargosModStock = salidas.stream().flatMap(s -> s.getDetalleSalidas().stream().map(d -> {
                                Map<String, Object> row = new HashMap<>();
                                row.put("tipoDescargo", "MOD_STOCK");
                                row.put("idReferencia", s.getIdSalida());
                                row.put("fecha", s.getFechaSalida());
                                row.put("usuario", s.getUsuario() == null ? null : s.getUsuario().getNombre());
                                row.put("producto", d.getProducto() == null ? null : d.getProducto().getNombreProducto());
                                row.put("cantidad", d.getCantidad());
                                row.put("motivo", s.getMotivo());
                                int nf = d.getProducto() != null && d.getProducto().getNroFraccion() != null ? Math.max(1, d.getProducto().getNroFraccion()) : 1;
                                int cantidad = d.getCantidad() == null ? 0 : d.getCantidad();
                                row.put("nroFraccion", nf);
                                row.put("cajas", nf > 1 ? cantidad / nf : cantidad);
                                row.put("fracciones", nf > 1 ? cantidad % nf : 0);
                                                                row.put("stockAntes", null);
                                                                row.put("stockDespues", null);
                                                                row.put("stockAntesCajas", null);
                                                                row.put("stockAntesFracciones", null);
                                                                row.put("stockDespuesCajas", null);
                                                                row.put("stockDespuesFracciones", null);
                                return row;
                        })).toList();
                }

                // Descargos por venta desde movimientos registrados
                var descargosVenta = movimientos.stream()
                                .filter(m -> "DESCARGO".equalsIgnoreCase(m.getTipoMovimiento()))
                                .filter(m -> "VENTA".equalsIgnoreCase(m.getTipoDescargo()))
                                .map(m -> {
            Map<String, Object> row = new HashMap<>();
                        row.put("tipoDescargo", "VENTA");
                        row.put("idReferencia", m.getIdReferencia());
                        row.put("fecha", m.getFecha());
                        row.put("usuario", m.getUsuario());
                        row.put("producto", m.getProducto());
                        row.put("cantidad", m.getCantidad());
            row.put("motivo", "VENTA");
                        int nf = m.getNroFraccion() == null ? 1 : Math.max(1, m.getNroFraccion());
                        int cantidad = m.getCantidad() == null ? 0 : m.getCantidad();
                        int stockAntes = m.getStockAntes() == null ? 0 : m.getStockAntes();
                        int stockDespues = m.getStockDespues() == null ? 0 : m.getStockDespues();
                        row.put("nroFraccion", nf);
                        row.put("cajas", nf > 1 ? cantidad / nf : cantidad);
                        row.put("fracciones", nf > 1 ? cantidad % nf : 0);
                        row.put("stockAntes", stockAntes);
                        row.put("stockDespues", stockDespues);
                        row.put("stockAntesCajas", nf > 1 ? stockAntes / nf : stockAntes);
                        row.put("stockAntesFracciones", nf > 1 ? stockAntes % nf : 0);
                        row.put("stockDespuesCajas", nf > 1 ? stockDespues / nf : stockDespues);
                        row.put("stockDespuesFracciones", nf > 1 ? stockDespues % nf : 0);
            return row;
                }).toList();

                if (descargosVenta.isEmpty()) {
                        descargosVenta = ventas.stream().flatMap(v -> v.getDetalleVentas().stream().map(d -> {
                                Map<String, Object> row = new HashMap<>();
                                row.put("tipoDescargo", "VENTA");
                                row.put("idReferencia", v.getIdVenta());
                                row.put("fecha", v.getFechaTransaccion());
                                row.put("usuario", v.getUsuario() == null ? null : v.getUsuario().getNombre());
                                row.put("producto", d.getProducto() == null ? null : d.getProducto().getNombreProducto());
                                row.put("cantidad", d.getCantidad());
                                row.put("motivo", "VENTA");
                                int nf = d.getProducto() != null && d.getProducto().getNroFraccion() != null ? Math.max(1, d.getProducto().getNroFraccion()) : 1;
                                int cantidad = d.getCantidad() == null ? 0 : d.getCantidad();
                                row.put("nroFraccion", nf);
                                row.put("cajas", nf > 1 ? cantidad / nf : cantidad);
                                row.put("fracciones", nf > 1 ? cantidad % nf : 0);
                                row.put("stockAntes", null);
                                row.put("stockDespues", null);
                                row.put("stockAntesCajas", null);
                                row.put("stockAntesFracciones", null);
                                row.put("stockDespuesCajas", null);
                                row.put("stockDespuesFracciones", null);
                                return row;
                        })).toList();
                }

                var descargos = new java.util.ArrayList<Map<String, Object>>();
                descargos.addAll(descargosModStock);
                descargos.addAll(descargosVenta);

                int totalCargos = cargos.stream()
                .map(m -> (Integer) m.getOrDefault("cantidad", 0))
                .reduce(0, Integer::sum);
        int totalDescargosModStock = descargosModStock.stream()
                .map(m -> (Integer) m.getOrDefault("cantidad", 0))
                .reduce(0, Integer::sum);
        int totalDescargosVenta = descargosVenta.stream()
                .map(m -> (Integer) m.getOrDefault("cantidad", 0))
                .reduce(0, Integer::sum);

        Map<String, Object> payload = new HashMap<>();
                payload.put("cargos", cargos);
                payload.put("descargos", descargos);
                payload.put("totalCargos", totalCargos);
        payload.put("totalDescargosModStock", totalDescargosModStock);
        payload.put("totalDescargosVenta", totalDescargosVenta);
        payload.put("totalUnidadesDescargadas", totalDescargosModStock + totalDescargosVenta);
        return ResponseEntity.ok(payload);
    }
}
