package com.magm.config;

import com.magm.entity.Cliente;
import com.magm.entity.Laboratorio;
import com.magm.entity.Producto;
import com.magm.entity.Proveedor;
import com.magm.entity.Rol;
import com.magm.entity.Usuario;
import com.magm.entity.Zona;
import com.magm.repository.ClienteRepository;
import com.magm.repository.LaboratorioRepository;
import com.magm.repository.ProductoRepository;
import com.magm.repository.ProveedorRepository;
import com.magm.repository.RolRepository;
import com.magm.repository.UsuarioRepository;
import com.magm.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private static final List<String> ADMIN_RESOURCES = List.of(
            "laboratorios",
            "genericos",
            "productos",
            "proveedores",
            "ventas",
            "compras",
            "salidas",
            "incentivos",
            "usuarios",
            "roles",
            "digemid_codigos",
            "cargos",
            "reportes",
            "pedidos",
            "caja"
    );

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final ZonaRepository zonaRepository;
    private final ClienteRepository clienteRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final ProveedorRepository proveedorRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            Rol rolAdmin = upsertRol("ADMIN", "Administrador del sistema", ADMIN_RESOURCES);
            upsertRol("VENDEDOR", "Vendedor de medicamentos", List.of("ventas", "productos"));
            upsertRol("GERENTE", "Gerente de farmacia", List.of(
                    "productos",
                    "ventas",
                    "compras",
                    "proveedores",
                    "reportes",
                    "caja",
                    "pedidos"
            ));
            upsertRol("ALMACENERO", "Encargado de almacen", List.of(
                    "productos",
                    "compras",
                    "salidas",
                    "pedidos",
                    "laboratorios",
                    "genericos",
                    "proveedores"
            ));

            Zona zonaPrincipal = asegurarZonaPrincipal();
            asegurarUsuarioPrivilegiado("superadmin", "superadmin123", "Super Administrador", zonaPrincipal, rolAdmin);
            asegurarUsuarioPrivilegiado("admin", "admin123", "Administrador General", zonaPrincipal, rolAdmin);

            if (usuarioRepository.count() > 2) {
                return;
            }

            Laboratorio laboratorio = laboratorioRepository.findById(1).orElseGet(() ->
                    laboratorioRepository.save(Laboratorio.builder()
                            .idLaboratorio(1)
                            .nombreLaboratorio("Genfar")
                            .abreviatura("GEN")
                            .estado(1)
                            .build())
            );

            if (proveedorRepository.count() == 0) {
                proveedorRepository.save(Proveedor.builder()
                        .nombreProveedor("Distribuidora Medisur")
                        .ruc("20445566771")
                        .direccion("Av. Salud 123")
                        .telefono("999888777")
                        .estado(1)
                        .build());
            }

            if (productoRepository.count() == 0) {
                productoRepository.save(Producto.builder()
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
                        .zona(zonaPrincipal)
                        .build());

                productoRepository.save(Producto.builder()
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
                        .zona(zonaPrincipal)
                        .build());
            }
        };
    }

    private Rol upsertRol(String nombre, String descripcion, List<String> recursos) {
        Rol rol = rolRepository.findByNombre(nombre).orElseGet(Rol::new);
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        rol.setEstado(1);
        rol.setRecursos(recursos);
        return rolRepository.save(rol);
    }

    private Zona asegurarZonaPrincipal() {
        return zonaRepository.findById(1).orElseGet(() ->
                zonaRepository.save(Zona.builder()
                        .idZona(1)
                        .nombreZona("Principal")
                        .direccion("Lima")
                        .ruc("20123456789")
                        .razonSocial("Farmacia Demo SAC")
                        .build())
        );
    }

    private void asegurarUsuarioPrivilegiado(String username, String password, String nombreCliente, Zona zona, Rol rolAdmin) {
        Usuario usuario = usuarioRepository.findByNombre(username).orElseGet(Usuario::new);
        Cliente cliente = usuario.getCliente();

        if (cliente == null) {
            cliente = Cliente.builder()
                    .nombreCliente(nombreCliente)
                    .cuentaHabilitada(true)
                    .zona(zona)
                    .build();
        } else {
            cliente.setNombreCliente(nombreCliente);
            cliente.setCuentaHabilitada(true);
            cliente.setZona(zona);
        }

        cliente = clienteRepository.save(cliente);

        usuario.setNombre(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setAdmin(true);
        usuario.setRol("ADMIN");
        usuario.setRolEntity(rolAdmin);
        usuario.setEstado(1);
        usuario.setCliente(cliente);

        usuarioRepository.save(usuario);
    }
}
