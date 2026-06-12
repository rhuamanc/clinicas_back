package com.magm.repository;

import com.magm.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
    List<Proveedor> findByEstadoOrderByNombreProveedorAsc(Integer estado);
    List<Proveedor> findByNombreProveedorContainingIgnoreCaseAndEstado(String nombre, Integer estado);
}
