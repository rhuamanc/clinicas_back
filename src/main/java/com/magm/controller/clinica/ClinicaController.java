package com.magm.controller.clinica;

import com.magm.dto.DetalleVentaDTO;
import com.magm.dto.VentaDTO;
import com.magm.entity.Producto;
import com.magm.entity.Usuario;
import com.magm.entity.clinica.*;
import com.magm.repository.ProductoRepository;
import com.magm.repository.UsuarioRepository;
import com.magm.repository.clinica.*;
import com.magm.service.VentaService;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clinica")
@RequiredArgsConstructor
public class ClinicaController {

    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final EspecialidadRepository especialidadRepository;
    private final CitaRepository citaRepository;
    private final AdmisionRepository admisionRepository;
    private final TriajeRepository triajeRepository;
    private final AtencionMedicaRepository atencionMedicaRepository;
    private final DiagnosticoAtencionRepository diagnosticoAtencionRepository;
    private final ExamenLaboratorioRepository examenLaboratorioRepository;
    private final RecetaRepository recetaRepository;
    private final RecetaDetalleRepository recetaDetalleRepository;
    private final LaboratorioOrdenRepository laboratorioOrdenRepository;
    private final LaboratorioResultadoRepository laboratorioResultadoRepository;
    private final ProcedimientoAtencionRepository procedimientoAtencionRepository;
    private final HistoriaClinicaEventoRepository historiaClinicaEventoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaService ventaService;

        private static final Set<String> AREAS_LABORATORIO_VALIDAS = Set.of(
            "HEMATOLOGIA",
            "BIOQUIMICA",
            "INMUNOLOGIA",
            "MICROBIOLOGIA",
            "PARASITOLOGIA",
            "HORMONAS",
            "UROANALISIS",
            "OTROS"
        );

    @GetMapping("/pacientes")
    public ResponseEntity<List<Paciente>> listarPacientes(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(pacienteRepository.findByEstadoOrderByApellidosAscNombresAsc(1));
        }
        return ResponseEntity.ok(
                pacienteRepository.findByEstadoAndNombresContainingIgnoreCaseOrEstadoAndApellidosContainingIgnoreCase(1, q, 1, q)
        );
    }

    @PostMapping("/pacientes")
    public ResponseEntity<Paciente> crearPaciente(@RequestBody Paciente paciente) {
        pacienteRepository.findByDni(paciente.getDni()).ifPresent(p -> {
            throw new RuntimeException("Ya existe un paciente con ese DNI");
        });
        paciente.setIdPaciente(null);
        paciente.setEstado(1);
        Paciente guardado = pacienteRepository.save(paciente);
        registrarEventoPaciente(guardado, "PACIENTE", "Registro de paciente");
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/pacientes/{id}")
    public ResponseEntity<Paciente> actualizarPaciente(@PathVariable Integer id, @RequestBody Paciente payload) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        paciente.setNombres(payload.getNombres());
        paciente.setApellidos(payload.getApellidos());
        paciente.setTelefono(payload.getTelefono());
        paciente.setDireccion(payload.getDireccion());
        paciente.setFechaNacimiento(payload.getFechaNacimiento());
        paciente.setSexo(payload.getSexo());
        paciente.setAlergias(payload.getAlergias());
        paciente.setAntecedentes(payload.getAntecedentes());
        paciente.setContactoEmergenciaNombre(payload.getContactoEmergenciaNombre());
        paciente.setContactoEmergenciaTelefono(payload.getContactoEmergenciaTelefono());
        Paciente actualizado = pacienteRepository.save(paciente);
        registrarEventoPaciente(actualizado, "PACIENTE", "Actualizacion de datos de paciente");
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/especialidades")
    public ResponseEntity<List<Especialidad>> listarEspecialidades() {
        return ResponseEntity.ok(especialidadRepository.findByActivaTrueOrderByNombreAsc());
    }

    @PostMapping("/especialidades")
    public ResponseEntity<Especialidad> crearEspecialidad(@RequestBody Especialidad especialidad) {
        especialidad.setIdEspecialidad(null);
        especialidad.setActiva(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(especialidadRepository.save(especialidad));
    }

    @GetMapping("/medicos")
    public ResponseEntity<List<Medico>> listarMedicos() {
        return ResponseEntity.ok(medicoRepository.findByActivoTrueOrderByApellidosAscNombresAsc());
    }

    @PostMapping("/medicos")
    public ResponseEntity<Medico> crearMedico(@RequestBody MedicoRequest request) {
        Set<Integer> idsEspecialidad = request.getIdsEspecialidad() == null ? Set.of() : request.getIdsEspecialidad();
        Set<Especialidad> especialidades = especialidadRepository.findAllById(idsEspecialidad)
                .stream().collect(Collectors.toSet());
        Medico medico = Medico.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .cmp(request.getCmp())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .consultorio(request.getConsultorio())
                .horarioInicio(request.getHorarioInicio())
                .horarioFin(request.getHorarioFin())
                .activo(request.getActivo() == null || request.getActivo())
                .especialidades(especialidades)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(medicoRepository.save(medico));
    }

    @PostMapping("/citas")
    public ResponseEntity<Cita> agendarCita(@RequestBody CitaRequest request) {
        if (citaRepository.existsByMedicoIdMedicoAndFechaHora(request.getIdMedico(), request.getFechaHora())) {
            throw new RuntimeException("El medico ya tiene una cita en ese horario");
        }

        Paciente paciente = pacienteRepository.findById(request.getIdPaciente())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Medico medico = medicoRepository.findById(request.getIdMedico())
                .orElseThrow(() -> new RuntimeException("Medico no encontrado"));
        Especialidad especialidad = especialidadRepository.findById(request.getIdEspecialidad())
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

        Cita cita = Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .especialidad(especialidad)
                .fechaHora(request.getFechaHora())
                .motivo(request.getMotivo())
                .observaciones(request.getObservaciones())
                .estado("AGENDADA")
                .build();
        Cita guardada = citaRepository.save(cita);
        registrarEventoPaciente(paciente, "CITA", "Cita agendada para " + guardada.getFechaHora());
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @PutMapping("/citas/{id}/estado")
    public ResponseEntity<Cita> cambiarEstadoCita(@PathVariable Integer id, @RequestBody CitaEstadoRequest request) {
        Cita cita = citaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setEstado(request.getEstado());
        if (request.getFechaHora() != null) {
            cita.setFechaHora(request.getFechaHora());
        }
        Cita actualizada = citaRepository.save(cita);
        registrarEventoPaciente(cita.getPaciente(), "CITA", "Cita en estado: " + actualizada.getEstado());
        return ResponseEntity.ok(actualizada);
    }

    @GetMapping("/citas")
    public ResponseEntity<List<Cita>> listarCitasPorDia(@RequestParam String fecha) {
        LocalDate dia = LocalDate.parse(fecha);
        return ResponseEntity.ok(citaRepository.findByFechaHoraBetweenOrderByFechaHoraAsc(dia.atStartOfDay(), dia.atTime(23, 59, 59)));
    }

    @PostMapping("/admisiones")
    public ResponseEntity<Admision> registrarAdmision(@RequestBody AdmisionRequest request) {
        Paciente paciente = pacienteRepository.findById(request.getIdPaciente())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Cita cita = null;
        if (request.getIdCita() != null) {
            cita = citaRepository.findById(request.getIdCita())
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        }

        Admision admision = Admision.builder()
                .paciente(paciente)
                .cita(cita)
                .fechaLlegada(LocalDateTime.now())
                .tipoIngreso(request.getTipoIngreso() == null ? "SIN_CITA" : request.getTipoIngreso())
                .estado("REGISTRADA")
                .derivacion(request.getDerivacion() == null ? "TRIAJE" : request.getDerivacion())
                .build();
        Admision guardada = admisionRepository.save(admision);
        registrarEventoPaciente(paciente, "ADMISION", "Ingreso registrado en admision");
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @GetMapping("/admisiones")
    public ResponseEntity<List<Admision>> listarAdmisiones(@RequestParam(required = false) String estado) {
        if (estado != null && !estado.isBlank()) {
            return ResponseEntity.ok(admisionRepository.findByEstadoOrderByFechaLlegadaDesc(estado));
        }
        return ResponseEntity.ok(admisionRepository.findAllByOrderByFechaLlegadaDesc());
    }

    @PostMapping("/triajes")
    public ResponseEntity<Triaje> registrarTriaje(@RequestBody TriajeRequest request) {
        Admision admision = admisionRepository.findById(request.getIdAdmision())
                .orElseThrow(() -> new RuntimeException("Admision no encontrada"));

        Triaje triaje = Triaje.builder()
                .admision(admision)
                .pesoKg(request.getPesoKg())
                .tallaM(request.getTallaM())
                .presionArterial(request.getPresionArterial())
                .temperatura(request.getTemperatura())
                .frecuenciaCardiaca(request.getFrecuenciaCardiaca())
                .saturacionOxigeno(request.getSaturacionOxigeno())
                .motivoConsulta(request.getMotivoConsulta())
                .build();

        if (request.getPesoKg() != null && request.getTallaM() != null && request.getTallaM().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal imc = request.getPesoKg().divide(request.getTallaM().multiply(request.getTallaM()), 2, RoundingMode.HALF_UP);
            triaje.setImc(imc);
        }

        Triaje guardado = triajeRepository.save(triaje);
        registrarEventoPaciente(admision.getPaciente(), "TRIAJE", "Triaje registrado");
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping("/triajes")
    public ResponseEntity<List<Triaje>> listarTriajes(@RequestParam(required = false) Integer idAdmision) {
        if (idAdmision != null) {
            return ResponseEntity.ok(triajeRepository.findByAdmisionIdAdmisionOrderByFechaRegistroDesc(idAdmision));
        }
        return ResponseEntity.ok(triajeRepository.findAll());
    }

    @PostMapping("/atenciones")
    public ResponseEntity<AtencionMedica> iniciarAtencion(@RequestBody AtencionRequest request) {
        Admision admision = admisionRepository.findById(request.getIdAdmision())
                .orElseThrow(() -> new RuntimeException("Admision no encontrada"));
        Medico medico = medicoRepository.findById(request.getIdMedico())
                .orElseThrow(() -> new RuntimeException("Medico no encontrado"));

        Usuario usuarioMedico = null;
        if (request.getIdUsuarioMedico() != null) {
            usuarioMedico = usuarioRepository.findById(request.getIdUsuarioMedico()).orElse(null);
        }

        AtencionMedica atencion = AtencionMedica.builder()
                .admision(admision)
                .medico(medico)
                .usuarioMedico(usuarioMedico)
                .sintomas(request.getSintomas())
                .examenFisico(request.getExamenFisico())
                .tratamiento(request.getTratamiento())
                .evolucion(request.getEvolucion())
                .observaciones(request.getObservaciones())
                .estado("ABIERTA")
                .build();

        AtencionMedica guardada = atencionMedicaRepository.save(atencion);
        registrarEventoPaciente(admision.getPaciente(), "CONSULTA", "Atencion medica iniciada");
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @GetMapping("/atenciones")
    public ResponseEntity<List<AtencionMedica>> listarAtenciones(@RequestParam(required = false) Integer idPaciente) {
        if (idPaciente != null) {
            return ResponseEntity.ok(atencionMedicaRepository.findByAdmisionPacienteIdPacienteOrderByFechaAtencionDesc(idPaciente));
        }
        return ResponseEntity.ok(atencionMedicaRepository.findAllByOrderByFechaAtencionDesc());
    }

        @GetMapping("/atenciones/{idAtencion}/resumen")
        public ResponseEntity<Map<String, Object>> resumenAtencion(@PathVariable Integer idAtencion) {
        AtencionMedica atencion = atencionMedicaRepository.findById(idAtencion)
            .orElseThrow(() -> new RuntimeException("Atencion no encontrada"));

        Triaje triaje = triajeRepository.findByAdmisionIdAdmisionOrderByFechaRegistroDesc(atencion.getAdmision().getIdAdmision())
            .stream()
            .findFirst()
            .orElse(null);

        List<LaboratorioOrden> ordenesLaboratorio = laboratorioOrdenRepository.findByAtencionIdAtencionOrderByFechaOrdenDesc(idAtencion);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("atencion", atencion);
        payload.put("triaje", triaje);
        payload.put("ordenesLaboratorio", ordenesLaboratorio);
        return ResponseEntity.ok(payload);
        }

        @GetMapping("/examenes-laboratorio")
        public ResponseEntity<Map<String, Object>> listarExamenesLaboratorio(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
        String areaNormalizada = normalizarArea(area);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<ExamenLaboratorio> data = examenLaboratorioRepository.buscar(activo, areaNormalizada, limpiarTexto(q), pageable);

        return ResponseEntity.ok(Map.of(
            "content", data.getContent(),
            "page", data.getNumber(),
            "size", data.getSize(),
            "totalElements", data.getTotalElements(),
            "totalPages", data.getTotalPages(),
            "first", data.isFirst(),
            "last", data.isLast()
        ));
        }

        @GetMapping("/examenes-laboratorio/{idExamen}")
        public ResponseEntity<ExamenLaboratorio> obtenerExamenLaboratorio(@PathVariable Integer idExamen) {
        ExamenLaboratorio examen = examenLaboratorioRepository.findById(idExamen)
            .orElseThrow(() -> new RuntimeException("Examen de laboratorio no encontrado"));
        return ResponseEntity.ok(examen);
        }

        @PostMapping("/examenes-laboratorio")
        public ResponseEntity<ExamenLaboratorio> crearExamenLaboratorio(@RequestBody ExamenLaboratorioRequest request) {
        validarExamenLaboratorioRequest(request, null);

        ExamenLaboratorio examen = ExamenLaboratorio.builder()
            .codigo(request.getCodigo().trim())
            .nombre(request.getNombre().trim())
            .descripcion(limpiarTexto(request.getDescripcion()))
            .areaLaboratorio(normalizarArea(request.getAreaLaboratorio()))
            .precio(request.getPrecio())
            .tiempoEntrega(request.getTiempoEntrega().trim())
            .requiereAyuno(request.getRequiereAyuno() != null && request.getRequiereAyuno())
            .requiereMuestraEspecial(request.getRequiereMuestraEspecial() != null && request.getRequiereMuestraEspecial())
            .indicacionesPaciente(limpiarTexto(request.getIndicacionesPaciente()))
            .activo(request.getActivo() == null || request.getActivo())
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(examenLaboratorioRepository.save(examen));
        }

        @PutMapping("/examenes-laboratorio/{idExamen}")
        public ResponseEntity<ExamenLaboratorio> actualizarExamenLaboratorio(@PathVariable Integer idExamen,
                                          @RequestBody ExamenLaboratorioRequest request) {
        ExamenLaboratorio examen = examenLaboratorioRepository.findById(idExamen)
            .orElseThrow(() -> new RuntimeException("Examen de laboratorio no encontrado"));

        validarExamenLaboratorioRequest(request, idExamen);

        examen.setCodigo(request.getCodigo().trim());
        examen.setNombre(request.getNombre().trim());
        examen.setDescripcion(limpiarTexto(request.getDescripcion()));
        examen.setAreaLaboratorio(normalizarArea(request.getAreaLaboratorio()));
        examen.setPrecio(request.getPrecio());
        examen.setTiempoEntrega(request.getTiempoEntrega().trim());
        examen.setRequiereAyuno(request.getRequiereAyuno() != null && request.getRequiereAyuno());
        examen.setRequiereMuestraEspecial(request.getRequiereMuestraEspecial() != null && request.getRequiereMuestraEspecial());
        examen.setIndicacionesPaciente(limpiarTexto(request.getIndicacionesPaciente()));
        examen.setActivo(request.getActivo() == null || request.getActivo());

        return ResponseEntity.ok(examenLaboratorioRepository.save(examen));
        }

        @PutMapping("/examenes-laboratorio/{idExamen}/activar")
        public ResponseEntity<ExamenLaboratorio> activarExamenLaboratorio(@PathVariable Integer idExamen) {
        ExamenLaboratorio examen = examenLaboratorioRepository.findById(idExamen)
            .orElseThrow(() -> new RuntimeException("Examen de laboratorio no encontrado"));
        examen.setActivo(true);
        return ResponseEntity.ok(examenLaboratorioRepository.save(examen));
        }

        @PutMapping("/examenes-laboratorio/{idExamen}/inactivar")
        public ResponseEntity<ExamenLaboratorio> inactivarExamenLaboratorio(@PathVariable Integer idExamen) {
        ExamenLaboratorio examen = examenLaboratorioRepository.findById(idExamen)
            .orElseThrow(() -> new RuntimeException("Examen de laboratorio no encontrado"));
            // Si fue utilizado en ordenes, solo se permite inactivar (no existe eliminacion fisica en este modulo).
        examen.setActivo(false);
        return ResponseEntity.ok(examenLaboratorioRepository.save(examen));
        }

    @PostMapping("/atenciones/{idAtencion}/diagnosticos")
    public ResponseEntity<DiagnosticoAtencion> registrarDiagnostico(@PathVariable Integer idAtencion,
                                                                    @RequestBody DiagnosticoRequest request) {
        AtencionMedica atencion = atencionMedicaRepository.findById(idAtencion)
                .orElseThrow(() -> new RuntimeException("Atencion no encontrada"));

        DiagnosticoAtencion diagnostico = DiagnosticoAtencion.builder()
                .atencion(atencion)
                .codigoCie10(request.getCodigoCie10())
                .descripcion(request.getDescripcion())
                .tipo(request.getTipo() == null ? "PRESUNTIVO" : request.getTipo())
                .build();

        DiagnosticoAtencion guardado = diagnosticoAtencionRepository.save(diagnostico);
        registrarEventoPaciente(atencion.getAdmision().getPaciente(), "DIAGNOSTICO", "Diagnostico registrado: " + guardado.getDescripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping("/atenciones/{idAtencion}/diagnosticos")
    public ResponseEntity<List<DiagnosticoAtencion>> listarDiagnosticosPorAtencion(@PathVariable Integer idAtencion) {
        return ResponseEntity.ok(diagnosticoAtencionRepository.findByAtencionIdAtencion(idAtencion));
    }

    @PostMapping("/atenciones/{idAtencion}/recetas")
    @Transactional
    public ResponseEntity<Receta> crearReceta(@PathVariable Integer idAtencion, @RequestBody RecetaRequest request) {
        AtencionMedica atencion = atencionMedicaRepository.findById(idAtencion)
                .orElseThrow(() -> new RuntimeException("Atencion no encontrada"));

        Receta receta = Receta.builder()
                .atencion(atencion)
                .estado("PENDIENTE")
                .observaciones(request.getObservaciones())
                .build();

        for (RecetaDetalleRequest detalleRequest : request.getDetalles()) {
            Producto producto = null;
            if (detalleRequest.getIdProducto() != null) {
                producto = productoRepository.findById(detalleRequest.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalleRequest.getIdProducto()));
            }
            RecetaDetalle detalle = RecetaDetalle.builder()
                    .receta(receta)
                    .producto(producto)
                    .medicamento(detalleRequest.getMedicamento())
                    .cantidad(detalleRequest.getCantidad())
                    .dosis(detalleRequest.getDosis())
                    .frecuencia(detalleRequest.getFrecuencia())
                    .duracion(detalleRequest.getDuracion())
                    .indicaciones(detalleRequest.getIndicaciones())
                    .build();
            receta.getDetalles().add(detalle);
        }

        Receta guardada = recetaRepository.save(receta);
        registrarEventoPaciente(atencion.getAdmision().getPaciente(), "RECETA", "Receta medica generada");
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @GetMapping("/recetas/pendientes")
    public ResponseEntity<List<Receta>> listarRecetasPendientes(@RequestParam(required = false) Integer idPaciente) {
        if (idPaciente != null) {
            return ResponseEntity.ok(recetaRepository.findByEstadoAndAtencionAdmisionPacienteIdPacienteOrderByFechaRecetaDesc("PENDIENTE", idPaciente));
        }
        return ResponseEntity.ok(recetaRepository.findByEstadoOrderByFechaRecetaDesc("PENDIENTE"));
    }

    @GetMapping("/recetas/{idReceta}/detalle")
    public ResponseEntity<Map<String, Object>> obtenerDetalleReceta(@PathVariable Integer idReceta) {
        Receta receta = recetaRepository.findById(idReceta)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        List<RecetaDetalle> detalles = recetaDetalleRepository.findByRecetaIdReceta(idReceta);
        Paciente paciente = receta.getAtencion().getAdmision().getPaciente();

        List<Map<String, Object>> detalleItems = detalles.stream().map(d -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idRecetaDetalle", d.getIdRecetaDetalle());
            item.put("idProducto", d.getProducto() != null ? d.getProducto().getIdProducto() : null);
            item.put("nombreProducto", d.getProducto() != null ? d.getProducto().getNombreProducto() : d.getMedicamento());
            item.put("medicamento", d.getMedicamento());
            item.put("cantidad", d.getCantidad());
            item.put("precioUnitario", d.getProducto() != null ? d.getProducto().getPrecio() : BigDecimal.ZERO);
            item.put("dosis", d.getDosis());
            item.put("frecuencia", d.getFrecuencia());
            item.put("duracion", d.getDuracion());
            item.put("indicaciones", d.getIndicaciones());
            return item;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "idReceta", receta.getIdReceta(),
                "estado", receta.getEstado(),
                "fechaReceta", receta.getFechaReceta(),
                "paciente", Map.of(
                        "idPaciente", paciente.getIdPaciente(),
                        "nombres", paciente.getNombres(),
                        "apellidos", paciente.getApellidos(),
                        "dni", paciente.getDni()
                ),
                "detalles", detalleItems
        ));
    }

    @PostMapping("/recetas/{idReceta}/dispensar")
    @Transactional
    public ResponseEntity<Map<String, Object>> dispensarReceta(@PathVariable Integer idReceta,
                                                               @AuthenticationPrincipal UserDetails userDetails,
                                                               @RequestParam(required = false) Integer tipoPago) {
        Receta receta = recetaRepository.findById(idReceta)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        if (!"PENDIENTE".equalsIgnoreCase(receta.getEstado())) {
            throw new RuntimeException("La receta ya fue dispensada o anulada");
        }

        List<RecetaDetalle> detalles = recetaDetalleRepository.findByRecetaIdReceta(idReceta);
        if (detalles.isEmpty()) {
            throw new RuntimeException("La receta no tiene medicamentos");
        }

        VentaDTO ventaDTO = new VentaDTO();
        ventaDTO.setTipoPago(tipoPago == null ? 1 : tipoPago);

        List<DetalleVentaDTO> ventaDetalles = detalles.stream().map(det -> {
            if (det.getProducto() == null) {
                throw new RuntimeException("El item " + det.getMedicamento() + " no esta vinculado a un producto de farmacia");
            }
            DetalleVentaDTO d = new DetalleVentaDTO();
            d.setIdProducto(det.getProducto().getIdProducto());
            d.setCantidad(det.getCantidad());
            d.setPrecioUnitario(det.getProducto().getPrecio());
            d.setSubtotal(det.getProducto().getPrecio().multiply(BigDecimal.valueOf(det.getCantidad())));
            return d;
        }).toList();

        BigDecimal total = ventaDetalles.stream().map(DetalleVentaDTO::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        ventaDTO.setMontoVenta(total);
        ventaDTO.setDetalleVentas(ventaDetalles);

        String username = userDetails != null ? userDetails.getUsername() : "admin";
        VentaDTO ventaGenerada = ventaService.guardarVenta(ventaDTO, username);

        receta.setEstado("DISPENSADA");
        recetaRepository.save(receta);

        registrarEventoPaciente(receta.getAtencion().getAdmision().getPaciente(), "FARMACIA", "Receta dispensada en farmacia");

        return ResponseEntity.ok(Map.of(
                "mensaje", "Receta dispensada correctamente",
                "idReceta", receta.getIdReceta(),
                "idVenta", ventaGenerada.getIdVenta(),
                "montoVenta", ventaGenerada.getMontoVenta()
        ));
    }

            @PostMapping("/recetas/{idReceta}/marcar-dispensada")
            @Transactional
            public ResponseEntity<Map<String, Object>> marcarRecetaDispensadaDesdeVenta(@PathVariable Integer idReceta,
                                                 @RequestParam(required = false) Integer idVenta) {
            Receta receta = recetaRepository.findById(idReceta)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

            if ("DISPENSADA".equalsIgnoreCase(receta.getEstado())) {
                return ResponseEntity.ok(Map.of(
                    "mensaje", "La receta ya estaba dispensada",
                    "idReceta", receta.getIdReceta(),
                    "estado", receta.getEstado()
                ));
            }

            if (!"PENDIENTE".equalsIgnoreCase(receta.getEstado())) {
                throw new RuntimeException("Solo se pueden marcar recetas pendientes como dispensadas");
            }

            receta.setEstado("DISPENSADA");
            recetaRepository.save(receta);

            String descripcion = idVenta != null
                ? "Receta dispensada desde modulo ventas. Venta ID: " + idVenta
                : "Receta dispensada desde modulo ventas";
            registrarEventoPaciente(receta.getAtencion().getAdmision().getPaciente(), "FARMACIA", descripcion);

            return ResponseEntity.ok(Map.of(
                "mensaje", "Receta marcada como dispensada",
                "idReceta", receta.getIdReceta(),
                "estado", receta.getEstado(),
                "idVenta", idVenta
            ));
            }

    @PostMapping("/atenciones/{idAtencion}/laboratorio-ordenes")
    public ResponseEntity<LaboratorioOrden> crearOrdenLaboratorio(@PathVariable Integer idAtencion,
                                                                   @RequestBody LaboratorioOrdenRequest request) {
        AtencionMedica atencion = atencionMedicaRepository.findById(idAtencion)
                .orElseThrow(() -> new RuntimeException("Atencion no encontrada"));

        ExamenLaboratorio examenCatalogo = null;
        String examenTexto = request.getExamen();
        BigDecimal precioExamen = null;

        if (request.getIdExamen() != null) {
            examenCatalogo = examenLaboratorioRepository.findById(request.getIdExamen())
                .orElseThrow(() -> new RuntimeException("Examen de laboratorio no encontrado"));
            if (!Boolean.TRUE.equals(examenCatalogo.getActivo())) {
            throw new RuntimeException("El examen de laboratorio seleccionado esta inactivo");
            }
            examenTexto = examenCatalogo.getNombre();
            precioExamen = examenCatalogo.getPrecio();
        }

        if (examenTexto == null || examenTexto.isBlank()) {
            throw new RuntimeException("Debe indicar el examen o seleccionar uno del catalogo");
        }

        LaboratorioOrden orden = LaboratorioOrden.builder()
                .atencion(atencion)
            .examenCatalogo(examenCatalogo)
            .examen(examenTexto.trim())
            .precioExamen(precioExamen)
                .estado("PENDIENTE")
            .observaciones(limpiarTexto(request.getObservaciones()))
                .build();

        LaboratorioOrden guardada = laboratorioOrdenRepository.save(orden);
        registrarEventoPaciente(atencion.getAdmision().getPaciente(), "LABORATORIO", "Orden de laboratorio generada");
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @GetMapping("/laboratorio-ordenes")
    public ResponseEntity<List<LaboratorioOrden>> listarOrdenesLaboratorio(@RequestParam(required = false) String estado,
                                                                            @RequestParam(required = false) Integer idPaciente) {
        if (idPaciente != null) {
            return ResponseEntity.ok(laboratorioOrdenRepository.findByAtencionAdmisionPacienteIdPacienteOrderByFechaOrdenDesc(idPaciente));
        }
        if (estado != null && !estado.isBlank()) {
            return ResponseEntity.ok(laboratorioOrdenRepository.findByEstadoOrderByFechaOrdenDesc(estado));
        }
        return ResponseEntity.ok(laboratorioOrdenRepository.findAll().stream()
                .sorted(Comparator.comparing(LaboratorioOrden::getFechaOrden).reversed())
                .toList());
    }

    @PostMapping("/laboratorio-ordenes/{idOrden}/resultados")
    public ResponseEntity<LaboratorioResultado> registrarResultadoLaboratorio(@PathVariable Integer idOrden,
                                                                              @RequestBody LaboratorioResultadoRequest request) {
        LaboratorioOrden orden = laboratorioOrdenRepository.findById(idOrden)
                .orElseThrow(() -> new RuntimeException("Orden de laboratorio no encontrada"));

        LaboratorioResultado resultado = LaboratorioResultado.builder()
                .orden(orden)
                .resultado(request.getResultado())
                .archivoAdjunto(request.getArchivoAdjunto())
                .build();
        LaboratorioResultado guardado = laboratorioResultadoRepository.save(resultado);

        orden.setEstado("ENTREGADO");
        orden.setFechaEntrega(LocalDateTime.now());
        laboratorioOrdenRepository.save(orden);

        registrarEventoPaciente(orden.getAtencion().getAdmision().getPaciente(), "LABORATORIO", "Resultado de laboratorio registrado");
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping("/laboratorio-ordenes/{idOrden}/resultados")
    public ResponseEntity<List<LaboratorioResultado>> listarResultadosLaboratorio(@PathVariable Integer idOrden) {
        return ResponseEntity.ok(laboratorioResultadoRepository.findByOrdenIdOrdenLaboratorio(idOrden));
    }

    @PostMapping("/atenciones/{idAtencion}/procedimientos")
    public ResponseEntity<ProcedimientoAtencion> registrarProcedimiento(@PathVariable Integer idAtencion,
                                                                        @RequestBody ProcedimientoRequest request) {
        AtencionMedica atencion = atencionMedicaRepository.findById(idAtencion)
                .orElseThrow(() -> new RuntimeException("Atencion no encontrada"));

        ProcedimientoAtencion procedimiento = ProcedimientoAtencion.builder()
                .atencion(atencion)
                .procedimiento(request.getProcedimiento())
                .detalle(request.getDetalle())
                .tarifa(request.getTarifa())
                .estado(request.getEstado() == null ? "REALIZADO" : request.getEstado())
                .build();
        ProcedimientoAtencion guardado = procedimientoAtencionRepository.save(procedimiento);

        registrarEventoPaciente(atencion.getAdmision().getPaciente(), "PROCEDIMIENTO", "Procedimiento registrado: " + guardado.getProcedimiento());
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping("/atenciones/{idAtencion}/procedimientos")
    public ResponseEntity<List<ProcedimientoAtencion>> listarProcedimientosPorAtencion(@PathVariable Integer idAtencion) {
        return ResponseEntity.ok(procedimientoAtencionRepository.findByAtencionIdAtencion(idAtencion));
    }

    @GetMapping("/pacientes/{idPaciente}/historia")
    public ResponseEntity<Map<String, Object>> historiaClinica(@PathVariable Integer idPaciente) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        List<AtencionMedica> atenciones = atencionMedicaRepository.findByAdmisionPacienteIdPacienteOrderByFechaAtencionDesc(idPaciente);
        List<Receta> recetas = recetaRepository.findByAtencionAdmisionPacienteIdPacienteOrderByFechaRecetaDesc(idPaciente);
        List<LaboratorioOrden> laboratorio = laboratorioOrdenRepository.findByAtencionAdmisionPacienteIdPacienteOrderByFechaOrdenDesc(idPaciente);
        List<HistoriaClinicaEvento> eventos = historiaClinicaEventoRepository.findByPacienteIdPacienteOrderByFechaEventoDesc(idPaciente);

        return ResponseEntity.ok(Map.of(
                "paciente", paciente,
                "atenciones", atenciones,
                "recetas", recetas,
                "laboratorio", laboratorio,
                "eventos", eventos
        ));
    }

    @GetMapping("/reportes/resumen")
    public ResponseEntity<Map<String, Object>> reporteResumen(@RequestParam String fecha) {
        LocalDate dia = LocalDate.parse(fecha);
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fin = dia.atTime(23, 59, 59);

        long citas = citaRepository.findByFechaHoraBetweenOrderByFechaHoraAsc(inicio, fin).size();
        long admisiones = admisionRepository.findAll().stream().filter(a ->
                a.getFechaLlegada() != null && !a.getFechaLlegada().isBefore(inicio) && !a.getFechaLlegada().isAfter(fin)
        ).count();
        long triajes = triajeRepository.findAll().stream().filter(t ->
                t.getFechaRegistro() != null && !t.getFechaRegistro().isBefore(inicio) && !t.getFechaRegistro().isAfter(fin)
        ).count();
        long atenciones = atencionMedicaRepository.findAll().stream().filter(a ->
                a.getFechaAtencion() != null && !a.getFechaAtencion().isBefore(inicio) && !a.getFechaAtencion().isAfter(fin)
        ).count();

        return ResponseEntity.ok(Map.of(
                "fecha", fecha,
                "pacientes", pacienteRepository.count(),
                "medicos", medicoRepository.count(),
                "citas", citas,
                "admisiones", admisiones,
                "triajes", triajes,
                "atenciones", atenciones,
                "recetasPendientes", recetaRepository.findByEstadoOrderByFechaRecetaDesc("PENDIENTE").size()
        ));
    }

            @GetMapping("/reportes/produccion-medica")
            public ResponseEntity<List<Map<String, Object>>> reporteProduccionMedica(@RequestParam String fecha) {
            LocalDate dia = LocalDate.parse(fecha);
            LocalDateTime inicio = dia.atStartOfDay();
            LocalDateTime fin = dia.atTime(23, 59, 59);

            Map<Integer, Long> conteo = atencionMedicaRepository.findAll().stream()
                .filter(a -> a.getFechaAtencion() != null && !a.getFechaAtencion().isBefore(inicio) && !a.getFechaAtencion().isAfter(fin))
                .collect(Collectors.groupingBy(a -> a.getMedico().getIdMedico(), Collectors.counting()));

            List<Map<String, Object>> data = medicoRepository.findAll().stream()
                .map(m -> Map.<String, Object>of(
                    "idMedico", m.getIdMedico(),
                    "medico", m.getApellidos() + ", " + m.getNombres(),
                    "cmp", m.getCmp(),
                    "atenciones", conteo.getOrDefault(m.getIdMedico(), 0L)
                ))
                .toList();

            return ResponseEntity.ok(data);
            }

            @GetMapping("/reportes/atenciones-especialidad")
            public ResponseEntity<List<Map<String, Object>>> reporteAtencionesEspecialidad(@RequestParam String fecha) {
            LocalDate dia = LocalDate.parse(fecha);
            LocalDateTime inicio = dia.atStartOfDay();
            LocalDateTime fin = dia.atTime(23, 59, 59);

            Map<String, Long> conteo = new HashMap<>();
            for (Cita cita : citaRepository.findByFechaHoraBetweenOrderByFechaHoraAsc(inicio, fin)) {
                String nombre = cita.getEspecialidad() != null ? cita.getEspecialidad().getNombre() : "Sin especialidad";
                conteo.put(nombre, conteo.getOrDefault(nombre, 0L) + 1L);
            }

            List<Map<String, Object>> data = conteo.entrySet().stream()
                .map(e -> Map.<String, Object>of(
                    "especialidad", e.getKey(),
                    "atenciones", e.getValue()
                ))
                .sorted((a, b) -> Long.compare((Long) b.get("atenciones"), (Long) a.get("atenciones")))
                .toList();

            return ResponseEntity.ok(data);
            }

            @GetMapping("/reportes/recetas-dispensadas")
            public ResponseEntity<Map<String, Object>> reporteRecetasDispensadas(@RequestParam String fecha) {
            LocalDate dia = LocalDate.parse(fecha);
            LocalDateTime inicio = dia.atStartOfDay();
            LocalDateTime fin = dia.atTime(23, 59, 59);

            List<Receta> dispensadas = recetaRepository.findAll().stream()
                .filter(r -> "DISPENSADA".equalsIgnoreCase(r.getEstado()))
                .filter(r -> r.getFechaReceta() != null && !r.getFechaReceta().isBefore(inicio) && !r.getFechaReceta().isAfter(fin))
                .toList();

            return ResponseEntity.ok(Map.of(
                "fecha", fecha,
                "recetasDispensadas", dispensadas.size(),
                "pacientesUnicos", dispensadas.stream()
                    .map(r -> r.getAtencion().getAdmision().getPaciente().getIdPaciente())
                    .distinct()
                    .count()
            ));
            }

    private void registrarEventoPaciente(Paciente paciente, String tipo, String descripcion) {
        HistoriaClinicaEvento evento = HistoriaClinicaEvento.builder()
                .paciente(paciente)
                .tipoEvento(tipo)
                .descripcion(descripcion)
                .build();
        historiaClinicaEventoRepository.save(evento);
    }

    private void validarExamenLaboratorioRequest(ExamenLaboratorioRequest request, Integer idExamenActual) {
        if (request == null) {
            throw new RuntimeException("Datos de examen de laboratorio requeridos");
        }
        if (request.getCodigo() == null || request.getCodigo().isBlank()) {
            throw new RuntimeException("El codigo del examen es obligatorio");
        }
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del examen es obligatorio");
        }
        if (request.getAreaLaboratorio() == null || request.getAreaLaboratorio().isBlank()) {
            throw new RuntimeException("El area de laboratorio es obligatoria");
        }
        if (request.getTiempoEntrega() == null || request.getTiempoEntrega().isBlank()) {
            throw new RuntimeException("El tiempo estimado de entrega es obligatorio");
        }
        if (request.getPrecio() == null || request.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio debe ser mayor a cero");
        }

        Optional<ExamenLaboratorio> existenteCodigo = examenLaboratorioRepository.findByCodigoIgnoreCase(request.getCodigo().trim());
        if (existenteCodigo.isPresent() && !existenteCodigo.get().getIdExamen().equals(idExamenActual)) {
            throw new RuntimeException("El codigo del examen ya existe");
        }

        Optional<ExamenLaboratorio> existenteNombre = examenLaboratorioRepository.findByNombreIgnoreCase(request.getNombre().trim());
        if (existenteNombre.isPresent() && !existenteNombre.get().getIdExamen().equals(idExamenActual)) {
            throw new RuntimeException("El nombre del examen ya existe");
        }

        normalizarArea(request.getAreaLaboratorio());
    }

    private String normalizarArea(String area) {
        if (area == null || area.isBlank()) {
            return null;
        }
        String normalizada = area.trim().toUpperCase();
        if (!AREAS_LABORATORIO_VALIDAS.contains(normalizada)) {
            throw new RuntimeException("Area de laboratorio invalida");
        }
        return normalizada;
    }

    private String limpiarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    @Data
    public static class MedicoRequest {
        @NotNull
        private String nombres;
        @NotNull
        private String apellidos;
        @NotNull
        private String cmp;
        private String telefono;
        private String email;
        private String consultorio;
        private java.time.LocalTime horarioInicio;
        private java.time.LocalTime horarioFin;
        private Boolean activo;
        private Set<Integer> idsEspecialidad;
    }

    @Data
    public static class CitaRequest {
        @NotNull
        private Integer idPaciente;
        @NotNull
        private Integer idMedico;
        @NotNull
        private Integer idEspecialidad;
        @NotNull
        private LocalDateTime fechaHora;
        private String motivo;
        private String observaciones;
    }

    @Data
    public static class CitaEstadoRequest {
        @NotNull
        private String estado;
        private LocalDateTime fechaHora;
    }

    @Data
    public static class AdmisionRequest {
        @NotNull
        private Integer idPaciente;
        private Integer idCita;
        private String tipoIngreso;
        private String derivacion;
    }

    @Data
    public static class TriajeRequest {
        @NotNull
        private Integer idAdmision;
        private BigDecimal pesoKg;
        private BigDecimal tallaM;
        private String presionArterial;
        private BigDecimal temperatura;
        private Integer frecuenciaCardiaca;
        private Integer saturacionOxigeno;
        private String motivoConsulta;
    }

    @Data
    public static class AtencionRequest {
        @NotNull
        private Integer idAdmision;
        @NotNull
        private Integer idMedico;
        private Integer idUsuarioMedico;
        private String sintomas;
        private String examenFisico;
        private String tratamiento;
        private String evolucion;
        private String observaciones;
    }

    @Data
    public static class DiagnosticoRequest {
        private String codigoCie10;
        @NotNull
        private String descripcion;
        private String tipo;
    }

    @Data
    public static class RecetaRequest {
        private String observaciones;
        @NotNull
        private List<RecetaDetalleRequest> detalles;
    }

    @Data
    public static class RecetaDetalleRequest {
        private Integer idProducto;
        @NotNull
        private String medicamento;
        @NotNull
        private Integer cantidad;
        private String dosis;
        private String frecuencia;
        private String duracion;
        private String indicaciones;
    }

    @Data
    public static class LaboratorioOrdenRequest {
        private String examen;
        private Integer idExamen;
        private String observaciones;
    }

    @Data
    public static class ExamenLaboratorioRequest {
        @NotNull
        private String codigo;
        @NotNull
        private String nombre;
        private String descripcion;
        @NotNull
        private String areaLaboratorio;
        @NotNull
        private BigDecimal precio;
        @NotNull
        private String tiempoEntrega;
        private Boolean requiereAyuno;
        private Boolean requiereMuestraEspecial;
        private String indicacionesPaciente;
        private Boolean activo;
    }

    @Data
    public static class LaboratorioResultadoRequest {
        @NotNull
        private String resultado;
        private String archivoAdjunto;
    }

    @Data
    public static class ProcedimientoRequest {
        @NotNull
        private String procedimiento;
        private String detalle;
        private BigDecimal tarifa;
        private String estado;
    }
}
