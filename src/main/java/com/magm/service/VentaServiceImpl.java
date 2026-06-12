package com.magm.service;

import com.magm.dto.DetalleVentaDTO;
import com.magm.dto.VentaDTO;
import com.magm.entity.DetalleVenta;
import com.magm.entity.MovimientoStock;
import com.magm.entity.Producto;
import com.magm.entity.Usuario;
import com.magm.entity.Venta;
import com.magm.entity.Incentivo;
import com.magm.entity.IncentivoProducto;
import com.magm.repository.IncentivoProductoRepository;
import com.magm.repository.IncentivoRepository;
import com.magm.repository.MovimientoStockRepository;
import com.magm.repository.ProductoRepository;
import com.magm.repository.UsuarioRepository;
import com.magm.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final IncentivoRepository incentivoRepository;
    private final IncentivoProductoRepository incentivoProductoRepository;

    @Override
    @Transactional
    public VentaDTO guardarVenta(VentaDTO dto, String username) {
        Usuario usuario = usuarioRepository.findByNombre(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Venta venta = Venta.builder()
                .montoVenta(dto.getMontoVenta())
                .tipoPago(dto.getTipoPago())
                .usuario(usuario)
                .build();

        for (DetalleVentaDTO detalleDTO : dto.getDetalleVentas()) {
            Producto producto = productoRepository.findById(detalleDTO.getIdProducto())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado: " + detalleDTO.getIdProducto()));

            if (producto.getStock() < detalleDTO.getCantidad()) {
                throw new RuntimeException(
                        "Stock insuficiente para: " + producto.getNombreProducto()
                        + " (disponible: " + producto.getStock() + ")");
            }

            int stockAntes = producto.getStock() == null ? 0 : producto.getStock();
            int cantidad = detalleDTO.getCantidad() == null ? 0 : detalleDTO.getCantidad();
            int stockDespues = stockAntes - cantidad;

            producto.setStock(stockDespues);
            productoRepository.save(producto);

            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(detalleDTO.getPrecioUnitario())
                    .subtotal(detalleDTO.getSubtotal())
                    .build();
            venta.getDetalleVentas().add(detalle);

                    IncentivoProducto incentivoConfig = incentivoProductoRepository
                        .findByProductoIdProductoAndEstadoOrderByFechaRegistroDesc(producto.getIdProducto(), 1)
                        .stream()
                        .findFirst()
                        .orElse(null);
                if (incentivoConfig != null && incentivoConfig.getMonto() != null
                    && incentivoConfig.getMonto().compareTo(BigDecimal.ZERO) > 0
                    && cantidad > 0) {
                String descripcionConfig = incentivoConfig.getDescripcion() == null ? "" : incentivoConfig.getDescripcion().trim();
                String descripcion = descripcionConfig.isEmpty()
                    ? String.format("Incentivo por venta de %s x%d", producto.getNombreProducto(), cantidad)
                    : String.format("%s | Producto: %s x%d", descripcionConfig, producto.getNombreProducto(), cantidad);
                incentivoRepository.save(Incentivo.builder()
                    .usuario(usuario)
                    .monto(incentivoConfig.getMonto().multiply(BigDecimal.valueOf(cantidad)))
                    .descripcion(descripcion)
                    .build());
                }
        }

        Venta ventaGuardada = ventaRepository.save(venta);

        for (DetalleVenta detalle : ventaGuardada.getDetalleVentas()) {
            movimientoStockRepository.save(MovimientoStock.builder()
                    .tipoMovimiento("DESCARGO")
                    .tipoDescargo("VENTA")
                    .cantidad(detalle.getCantidad())
                    .nroFraccion(detalle.getProducto().getNroFraccion())
                    .stockAntes(detalle.getProducto().getStock() + detalle.getCantidad())
                    .stockDespues(detalle.getProducto().getStock())
                    .idZona(usuario.getCliente().getZona().getIdZona())
                    .idReferencia(ventaGuardada.getIdVenta())
                    .usuario(usuario.getNombre())
                    .producto(detalle.getProducto().getNombreProducto())
                    .motivo("VENTA")
                    .build());
        }

        return toDTO(ventaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> listarPorFecha(LocalDate fecha, Integer idZona) {
        return ventaRepository
                .findByZonaAndFecha(idZona, fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> listarPorRango(LocalDate fechaIni, LocalDate fechaFin, Integer idZona) {
        return ventaRepository
                .findByZonaAndFecha(idZona, fechaIni.atStartOfDay(), fechaFin.atTime(LocalTime.MAX))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VentaDTO buscarPorId(Integer id) {
        return ventaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + id));
    }

    private VentaDTO toDTO(Venta venta) {
        VentaDTO dto = new VentaDTO();
        dto.setIdVenta(venta.getIdVenta());
        dto.setFechaTransaccion(venta.getFechaTransaccion());
        dto.setMontoVenta(venta.getMontoVenta());
        dto.setTipoPago(venta.getTipoPago());
        dto.setEstado(venta.getEstado());
        dto.setDetalleVentas(venta.getDetalleVentas().stream().map(d -> {
            DetalleVentaDTO dDTO = new DetalleVentaDTO();
            dDTO.setIdProducto(d.getProducto().getIdProducto());
            dDTO.setNombreProducto(d.getProducto().getNombreProducto());
            dDTO.setCantidad(d.getCantidad());
            dDTO.setPrecioUnitario(d.getPrecioUnitario());
            dDTO.setSubtotal(d.getSubtotal());
            return dDTO;
        }).collect(Collectors.toList()));
        return dto;
    }
}
