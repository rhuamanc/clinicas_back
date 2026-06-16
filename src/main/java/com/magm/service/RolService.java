package com.magm.service;

import com.magm.dto.RolDTO;
import java.util.List;

public interface RolService {
    RolDTO crearRol(RolDTO rolDTO);
    RolDTO obtenerRolPorId(Integer id);
    List<RolDTO> listarRoles();
    List<RolDTO> listarRolesActivos();
    RolDTO actualizarRol(Integer id, RolDTO rolDTO);
    void desactivarRol(Integer id);
}
