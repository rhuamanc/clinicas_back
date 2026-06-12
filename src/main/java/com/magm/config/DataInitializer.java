package com.magm.config;

import com.magm.entity.*;
import com.magm.repository.ClienteRepository;
import com.magm.repository.LaboratorioRepository;
import com.magm.repository.ProductoRepository;
import com.magm.repository.ProveedorRepository;
import com.magm.repository.UsuarioRepository;
import com.magm.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final ZonaRepository zonaRepository;
    private final ClienteRepository clienteRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final ProveedorRepository proveedorRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            if (usuarioRepository.count() > 0) {
                return;
            }

            Zona zona = Zona.builder()
                    .idZona(1)
                    .nombreZona("Principal")
                    .direccion("Lima")
                    .ruc("20123456789")
                    .razonSocial("Farmacia Demo SAC")
                    .build();

            Cliente cliente = Cliente.builder()
                    .idCliente(1)
                    .nombreCliente("Administrador General")
                    .cuentaHabilitada(true)
                    .zona(zona)
                    .build();

            Usuario admin = Usuario.builder()
                    .nombre("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .admin(true)
                    .rol("ADMIN")
                    .estado(1)
                    .cliente(cliente)
                    .build();

            Laboratorio laboratorio = Laboratorio.builder()
                    .idLaboratorio(1)
                    .nombreLaboratorio("Genfar")
                    .abreviatura("GEN")
                    .estado(1)
                    .build();

            Producto p1 = Producto.builder()
                    .nombreProducto("Paracetamol 500mg")
                    .nroFraccion(10)
                    .unidades(12)
                    .fraccion(0)
                    .lote("PARA-001")
                    .precio(new BigDecimal("1.50"))
                    .precioCompra(new BigDecimal("0.90"))
                    .porcentajeGanancia(new BigDecimal("66.67"))
                    .precioVentaCaja(new BigDecimal("15.00"))
                    .stock(120)
                    .stockMinimo(20)
                    .unidad("Tableta")
                    .presentacion("Blister")
                    .ubicacion("A1")
                    .codigoBarras("750100000001")
                    .codigoDigemid("DIG-001")
                    .fechaAdquisicion(LocalDate.now().minusDays(20))
                    .fechaVencimiento(LocalDate.now().plusMonths(8))
                    .estado(1)
                    .laboratorio(laboratorio)
                    .zona(zona)
                    .build();

            Producto p2 = Producto.builder()
                    .nombreProducto("Ibuprofeno 400mg")
                    .nroFraccion(10)
                    .unidades(8)
                    .fraccion(0)
                    .lote("IBU-002")
                    .precio(new BigDecimal("2.20"))
                    .precioCompra(new BigDecimal("1.40"))
                    .porcentajeGanancia(new BigDecimal("57.14"))
                    .precioVentaCaja(new BigDecimal("22.00"))
                    .stock(80)
                    .stockMinimo(15)
                    .unidad("Tableta")
                    .presentacion("Caja")
                    .ubicacion("B2")
                    .codigoBarras("750100000002")
                    .codigoDigemid("DIG-002")
                    .fechaAdquisicion(LocalDate.now().minusDays(10))
                    .fechaVencimiento(LocalDate.now().plusMonths(10))
                    .estado(1)
                    .laboratorio(laboratorio)
                    .zona(zona)
                    .build();

            Proveedor proveedor = Proveedor.builder()
                    .nombreProveedor("Distribuidora Medisur")
                    .ruc("20445566771")
                    .direccion("Av. Salud 123")
                    .telefono("999888777")
                    .estado(1)
                    .build();

            zonaRepository.save(zona);
            clienteRepository.save(cliente);
            laboratorioRepository.save(laboratorio);
            proveedorRepository.save(proveedor);
            usuarioRepository.save(admin);
            productoRepository.save(p1);
            productoRepository.save(p2);
        };
    }
}
