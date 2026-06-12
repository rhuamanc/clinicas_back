package com.magm.controller;

import com.magm.dto.UsuarioAdminDTO;
import com.magm.entity.Cliente;
import com.magm.entity.Usuario;
import com.magm.entity.Zona;
import com.magm.repository.ClienteRepository;
import com.magm.repository.UsuarioRepository;
import com.magm.repository.ZonaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ZonaRepository zonaRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<UsuarioAdminDTO>> listar(@RequestParam Integer idZona) {
        return ResponseEntity.ok(usuarioRepository.findByEstadoAndClienteZonaIdZona(1, idZona)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<UsuarioAdminDTO> crear(@Valid @RequestBody UsuarioAdminDTO dto) {
        Zona zona = zonaRepository.findById(dto.getIdZona())
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        Cliente cliente = Cliente.builder()
            .nombreCliente(dto.getNombreCliente())
            .cuentaHabilitada(dto.getCuentaHabilitada() == null || dto.getCuentaHabilitada())
            .zona(zona)
            .build();
        clienteRepository.save(cliente);

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(dto.getRol() == null ? "VENDEDOR" : dto.getRol());
        usuario.setEstado(1);
        usuario.setAdmin("ADMIN".equalsIgnoreCase(usuario.getRol()));
        usuario.setCliente(cliente);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(usuarioRepository.save(usuario)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioAdminDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioAdminDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            usuario.setNombre(dto.getNombre());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRol() != null && !dto.getRol().isBlank()) {
            usuario.setRol(dto.getRol());
            usuario.setAdmin("ADMIN".equalsIgnoreCase(dto.getRol()));
        }
        if (dto.getEstado() != null) {
            usuario.setEstado(dto.getEstado());
        }
        if (dto.getNombreCliente() != null) {
            usuario.getCliente().setNombreCliente(dto.getNombreCliente());
        }
        if (dto.getCuentaHabilitada() != null) {
            usuario.getCliente().setCuentaHabilitada(dto.getCuentaHabilitada());
        }
        clienteRepository.save(usuario.getCliente());

        return ResponseEntity.ok(toDTO(usuarioRepository.save(usuario)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setEstado(0);
        usuarioRepository.save(usuario);
        return ResponseEntity.noContent().build();
    }

    private UsuarioAdminDTO toDTO(Usuario u) {
        UsuarioAdminDTO dto = new UsuarioAdminDTO();
        dto.setId(u.getId());
        dto.setNombre(u.getNombre());
        dto.setRol(u.getRol());
        dto.setEstado(u.getEstado());
        dto.setIdCliente(u.getCliente() != null ? u.getCliente().getIdCliente() : null);
        dto.setIdZona(u.getCliente() != null && u.getCliente().getZona() != null ? u.getCliente().getZona().getIdZona() : null);
        dto.setNombreCliente(u.getCliente() != null ? u.getCliente().getNombreCliente() : null);
        dto.setCuentaHabilitada(u.getCliente() != null && u.getCliente().isCuentaHabilitada());
        return dto;
    }
}
