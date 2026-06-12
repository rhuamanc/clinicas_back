package com.magm.controller;

import com.magm.dto.GenericoDTO;
import com.magm.entity.Generico;
import com.magm.repository.GenericoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/genericos")
@RequiredArgsConstructor
public class GenericoController {

    private final GenericoRepository genericoRepository;

    @GetMapping
    public ResponseEntity<List<GenericoDTO>> listar() {
        return ResponseEntity.ok(genericoRepository.findByEstadoOrderByNombreAsc(1).stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<GenericoDTO> crear(@Valid @RequestBody GenericoDTO dto) {
        Generico g = new Generico();
        g.setNombre(dto.getNombre());
        g.setDescripcion(dto.getDescripcion());
        g.setEstado(1);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(genericoRepository.save(g)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericoDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody GenericoDTO dto) {
        Generico g = genericoRepository.findById(id).orElseThrow(() -> new RuntimeException("Generico no encontrado"));
        g.setNombre(dto.getNombre());
        g.setDescripcion(dto.getDescripcion());
        return ResponseEntity.ok(toDTO(genericoRepository.save(g)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Generico g = genericoRepository.findById(id).orElseThrow(() -> new RuntimeException("Generico no encontrado"));
        g.setEstado(0);
        genericoRepository.save(g);
        return ResponseEntity.noContent().build();
    }

    private GenericoDTO toDTO(Generico g) {
        GenericoDTO dto = new GenericoDTO();
        dto.setIdGenerico(g.getIdGenerico());
        dto.setNombre(g.getNombre());
        dto.setDescripcion(g.getDescripcion());
        dto.setEstado(g.getEstado());
        return dto;
    }
}
