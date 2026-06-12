package com.magm.controller;

import com.magm.dto.IncentivoDTO;
import com.magm.entity.Incentivo;
import com.magm.entity.IncentivoProducto;
import com.magm.entity.Producto;
import com.magm.repository.IncentivoProductoRepository;
import com.magm.repository.IncentivoRepository;
import com.magm.repository.ProductoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incentivos")
@RequiredArgsConstructor
public class IncentivoController {

    private final IncentivoProductoRepository incentivoProductoRepository;
    private final ProductoRepository productoRepository;

    @GetMapping
    public ResponseEntity<List<IncentivoDTO>> listar(@RequestParam Integer idZona) {
        return ResponseEntity.ok(incentivoProductoRepository.findByProductoZonaIdZonaAndEstadoOrderByFechaRegistroDesc(idZona, 1)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<IncentivoDTO> crear(@Valid @RequestBody IncentivoDTO dto) {
        Producto producto = productoRepository.findById(dto.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        var existentesActivos = incentivoProductoRepository
            .findByProductoIdProductoAndEstadoOrderByFechaRegistroDesc(producto.getIdProducto(), 1);
        for (IncentivoProducto existente : existentesActivos) {
            existente.setEstado(0);
            incentivoProductoRepository.save(existente);
        }

        IncentivoProducto i = new IncentivoProducto();
        i.setMonto(dto.getMonto());
        i.setDescripcion(dto.getDescripcion());
        i.setProducto(producto);
        i.setEstado(1);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(incentivoProductoRepository.save(i)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        IncentivoProducto incentivo = incentivoProductoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incentivo no encontrado"));
        incentivo.setEstado(0);
        incentivoProductoRepository.save(incentivo);
        return ResponseEntity.noContent().build();
    }

    private IncentivoDTO toDTO(IncentivoProducto i) {
        IncentivoDTO dto = new IncentivoDTO();
        dto.setIdIncentivo(i.getIdIncentivoProducto());
        dto.setFecha(i.getFechaRegistro());
        dto.setMonto(i.getMonto());
        dto.setDescripcion(i.getDescripcion());
        dto.setIdProducto(i.getProducto().getIdProducto());
        dto.setNombreProducto(i.getProducto().getNombreProducto());
        return dto;
    }
}
