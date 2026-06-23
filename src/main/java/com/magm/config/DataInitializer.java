package com.magm.config;

import com.magm.entity.Cliente;
import com.magm.entity.Laboratorio;
import com.magm.entity.Producto;
import com.magm.entity.Proveedor;
import com.magm.entity.Rol;
import com.magm.entity.Usuario;
import com.magm.entity.Zona;
import com.magm.entity.clinica.ExamenLaboratorio;
import com.magm.entity.clinica.Especialidad;
import com.magm.entity.clinica.Medico;
import com.magm.entity.clinica.Paciente;
import com.magm.repository.ClienteRepository;
import com.magm.repository.LaboratorioRepository;
import com.magm.repository.ProductoRepository;
import com.magm.repository.ProveedorRepository;
import com.magm.repository.RolRepository;
import com.magm.repository.UsuarioRepository;
import com.magm.repository.ZonaRepository;
import com.magm.repository.clinica.ExamenLaboratorioRepository;
import com.magm.repository.clinica.EspecialidadRepository;
import com.magm.repository.clinica.MedicoRepository;
import com.magm.repository.clinica.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
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
            "caja",
            "pacientes",
            "medicos",
            "especialidades",
            "citas",
            "admision",
            "triaje",
            "consulta_medica",
            "diagnosticos",
            "recetas_medicas",
            "farmacia_integrada",
            "laboratorio_clinico",
            "procedimientos",
            "historia_clinica",
            "configuracion"
    );

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final ZonaRepository zonaRepository;
    private final ClienteRepository clienteRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final ProveedorRepository proveedorRepository;
    private final RolRepository rolRepository;
    private final EspecialidadRepository especialidadRepository;
        private final MedicoRepository medicoRepository;
        private final PacienteRepository pacienteRepository;
        private final ExamenLaboratorioRepository examenLaboratorioRepository;
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
            upsertRol("MEDICO", "Medico del policlinico", List.of(
                    "pacientes",
                    "medicos",
                    "especialidades",
                    "citas",
                    "admision",
                    "triaje",
                    "consulta_medica",
                    "diagnosticos",
                    "recetas_medicas",
                    "laboratorio_clinico",
                    "procedimientos",
                    "historia_clinica"
            ));
            upsertRol("ADMISION", "Admision del policlinico", List.of(
                    "pacientes",
                    "citas",
                    "admision",
                    "triaje",
                    "historia_clinica"
            ));
            upsertRol("LABORATORIO", "Laboratorio clinico", List.of(
                    "pacientes",
                    "laboratorio_clinico",
                    "historia_clinica"
            ));
            upsertRol("FARMACIA", "Farmacia integrada", List.of(
                    "productos",
                    "ventas",
                    "recetas_medicas",
                    "farmacia_integrada",
                    "historia_clinica"
            ));

            asegurarEspecialidadesBase();
            asegurarSemillasClinica();

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

    private void asegurarEspecialidadesBase() {
        if (especialidadRepository.count() > 0) {
            return;
        }

        List<String> especialidades = List.of(
                "Medicina General",
                "Pediatria",
                "Ginecologia",
                "Odontologia",
                "Nutricion",
                "Dermatologia"
        );

        for (String nombre : especialidades) {
            especialidadRepository.save(Especialidad.builder()
                    .nombre(nombre)
                    .descripcion("Especialidad medica: " + nombre)
                    .activa(true)
                    .build());
        }
    }

    private void asegurarSemillasClinica() {
        Especialidad medicinaGeneral = especialidadRepository.findByActivaTrueOrderByNombreAsc().stream()
                .findFirst()
                .orElseGet(() -> especialidadRepository.save(Especialidad.builder()
                        .nombre("Medicina General")
                        .descripcion("Especialidad medica: Medicina General")
                        .activa(true)
                        .build()));

        Medico medico = medicoRepository.findByCmp("CMP12345").orElseGet(() -> Medico.builder()
                .nombres("Juan")
                .apellidos("Perez")
                .cmp("CMP12345")
                .telefono("999111222")
                .email("juan.perez@policlinico.local")
                .consultorio("Consultorio 1")
                .horarioInicio(LocalTime.of(8, 0))
                .horarioFin(LocalTime.of(14, 0))
                .activo(true)
                .especialidades(new HashSet<>())
                .build());
        medico.getEspecialidades().add(medicinaGeneral);
        medicoRepository.save(medico);

        pacienteRepository.findByDni("44556677").orElseGet(() -> pacienteRepository.save(Paciente.builder()
                .nombres("Maria")
                .apellidos("Lopez")
                .dni("44556677")
                .telefono("988777666")
                .direccion("Av. Principal 123")
                .fechaNacimiento(LocalDate.of(1995, 5, 10))
                .sexo("F")
                .alergias("Ninguna")
                .antecedentes("Sin antecedentes relevantes")
                .contactoEmergenciaNombre("Carlos Lopez")
                .contactoEmergenciaTelefono("977666555")
                .estado(1)
                .build()));

        ExamenLaboratorio examen = examenLaboratorioRepository.findByCodigoIgnoreCase("LAB-GLU-001")
                .orElseGet(() -> ExamenLaboratorio.builder()
                        .codigo("LAB-GLU-001")
                        .nombre("Glucosa en sangre")
                        .descripcion("Medicion de glucosa en ayunas")
                        .areaLaboratorio("BIOQUIMICA")
                        .precio(new BigDecimal("18.00"))
                        .tiempoEntrega("24 horas")
                        .requiereAyuno(true)
                        .requiereMuestraEspecial(false)
                        .indicacionesPaciente("Ayuno de 8 horas")
                        .activo(true)
                        .build());
        examen.setActivo(true);
        examenLaboratorioRepository.save(examen);
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
