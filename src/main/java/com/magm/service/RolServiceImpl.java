package com.magm.service;

import com.magm.dto.RolDTO;
import com.magm.entity.Rol;
import com.magm.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    @Override
    @Transactional
    public RolDTO crearRol(RolDTO rolDTO) {
        if (rolRepository.findByNombre(rolDTO.getNombre()).isPresent()) {
            throw new RuntimeException("El rol '" + rolDTO.getNombre() + "' ya existe.");
        }

        Rol rol = Rol.builder()
                .nombre(rolDTO.getNombre())
                .descripcion(rolDTO.getDescripcion())
                .estado(1)
                .recursos(normalizarRecursos(rolDTO.getRecursos()))
                .build();

        Rol rolGuardado = rolRepository.save(rol);
        return toDTO(rolGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public RolDTO obtenerRolPorId(Integer id) {
        return rolRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolDTO> listarRoles() {
        return rolRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolDTO> listarRolesActivos() {
        return rolRepository.findAllActivos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RolDTO actualizarRol(Integer id, RolDTO rolDTO) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + id));

        // Verificar si el nuevo nombre ya existe en otro rol
        if (!rol.getNombre().equals(rolDTO.getNombre()) &&
            rolRepository.findByNombre(rolDTO.getNombre()).isPresent()) {
            throw new RuntimeException("El rol '" + rolDTO.getNombre() + "' ya existe.");
        }

        rol.setNombre(rolDTO.getNombre());
        rol.setDescripcion(rolDTO.getDescripcion());
        rol.setRecursos(normalizarRecursos(rolDTO.getRecursos()));

        Rol rolActualizado = rolRepository.save(rol);
        return toDTO(rolActualizado);
    }

    @Override
    @Transactional
    public void desactivarRol(Integer id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + id));

        rol.setEstado(0);
        rolRepository.save(rol);
    }

    private RolDTO toDTO(Rol rol) {
        RolDTO dto = new RolDTO();
        dto.setIdRol(rol.getIdRol());
        dto.setNombre(rol.getNombre());
        dto.setDescripcion(rol.getDescripcion());
        dto.setEstado(rol.getEstado());
        dto.setRecursos(rol.getRecursos() == null ? new ArrayList<>() : new ArrayList<>(rol.getRecursos()));
        return dto;
    }

    private List<String> normalizarRecursos(List<String> recursos) {
        if (recursos == null) {
            return new ArrayList<>();
        }

        return recursos.stream()
                .filter(recurso -> recurso != null && !recurso.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
