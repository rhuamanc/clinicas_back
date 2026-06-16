package com.magm.controller;

import com.magm.dto.RolDTO;
import com.magm.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RolController {

    private final RolService rolService;

    @GetMapping
    public ResponseEntity<List<RolDTO>> listar() {
        return ResponseEntity.ok(rolService.listarRoles());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<RolDTO>> listarActivos() {
        return ResponseEntity.ok(rolService.listarRolesActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(rolService.obtenerRolPorId(id));
    }

    @PostMapping
    public ResponseEntity<RolDTO> crear(@RequestBody RolDTO rolDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crearRol(rolDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolDTO> actualizar(@PathVariable Integer id, @RequestBody RolDTO rolDTO) {
        return ResponseEntity.ok(rolService.actualizarRol(id, rolDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        rolService.desactivarRol(id);
        return ResponseEntity.noContent().build();
    }
}
