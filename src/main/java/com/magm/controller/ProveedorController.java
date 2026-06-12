package com.magm.controller;

import com.magm.dto.ProveedorDTO;
import com.magm.entity.Proveedor;
import com.magm.repository.ProveedorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorRepository proveedorRepository;

    @GetMapping
    public ResponseEntity<List<ProveedorDTO>> listar() {
        return ResponseEntity.ok(proveedorRepository.findByEstadoOrderByNombreProveedorAsc(1)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProveedorDTO>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(proveedorRepository.findByNombreProveedorContainingIgnoreCaseAndEstado(nombre, 1)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<ProveedorDTO> crear(@Valid @RequestBody ProveedorDTO dto) {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombreProveedor(dto.getNombreProveedor());
        proveedor.setRuc(dto.getRuc());
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEstado(1);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(proveedorRepository.save(proveedor)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody ProveedorDTO dto) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        proveedor.setNombreProveedor(dto.getNombreProveedor());
        proveedor.setRuc(dto.getRuc());
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setTelefono(dto.getTelefono());
        return ResponseEntity.ok(toDTO(proveedorRepository.save(proveedor)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        proveedor.setEstado(0);
        proveedorRepository.save(proveedor);
        return ResponseEntity.noContent().build();
    }

    private ProveedorDTO toDTO(Proveedor p) {
        ProveedorDTO dto = new ProveedorDTO();
        dto.setIdProveedor(p.getIdProveedor());
        dto.setNombreProveedor(p.getNombreProveedor());
        dto.setRuc(p.getRuc());
        dto.setDireccion(p.getDireccion());
        dto.setTelefono(p.getTelefono());
        dto.setEstado(p.getEstado());
        return dto;
    }
}
