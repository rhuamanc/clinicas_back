package com.magm.controller;

import com.magm.dto.LaboratorioDTO;
import com.magm.entity.Laboratorio;
import com.magm.repository.LaboratorioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/laboratorios")
@RequiredArgsConstructor
public class LaboratorioController {

    private final LaboratorioRepository laboratorioRepository;

    @GetMapping
    public ResponseEntity<List<LaboratorioDTO>> listar() {
        return ResponseEntity.ok(laboratorioRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<LaboratorioDTO> crear(@Valid @RequestBody LaboratorioDTO dto) {
        Laboratorio laboratorio = new Laboratorio();
        laboratorio.setNombreLaboratorio(dto.getNombreLaboratorio());
        laboratorio.setAbreviatura(dto.getAbreviatura());
        laboratorio.setRuc(dto.getRuc());
        laboratorio.setDireccion(dto.getDireccion());
        laboratorio.setEstado(dto.getEstado() == null ? 1 : dto.getEstado());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(laboratorioRepository.save(laboratorio)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LaboratorioDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody LaboratorioDTO dto) {
        Laboratorio laboratorio = laboratorioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laboratorio no encontrado"));
        laboratorio.setNombreLaboratorio(dto.getNombreLaboratorio());
        laboratorio.setAbreviatura(dto.getAbreviatura());
        laboratorio.setRuc(dto.getRuc());
        laboratorio.setDireccion(dto.getDireccion());
        laboratorio.setEstado(dto.getEstado() == null ? laboratorio.getEstado() : dto.getEstado());
        return ResponseEntity.ok(toDTO(laboratorioRepository.save(laboratorio)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Laboratorio laboratorio = laboratorioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laboratorio no encontrado"));
        laboratorio.setEstado(0);
        laboratorioRepository.save(laboratorio);
        return ResponseEntity.noContent().build();
    }

    private LaboratorioDTO toDTO(Laboratorio l) {
        LaboratorioDTO dto = new LaboratorioDTO();
        dto.setIdLaboratorio(l.getIdLaboratorio());
        dto.setNombreLaboratorio(l.getNombreLaboratorio());
        dto.setAbreviatura(l.getAbreviatura());
        dto.setRuc(l.getRuc());
        dto.setDireccion(l.getDireccion());
        dto.setEstado(l.getEstado());
        return dto;
    }
}
