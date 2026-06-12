package com.magm.repository;

import com.magm.entity.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @EntityGraph(attributePaths = {"zona", "laboratorio"})
    List<Producto> findByZonaIdZonaAndEstadoOrderByNombreProductoAsc(Integer idZona, Integer estado);

    @EntityGraph(attributePaths = {"zona", "laboratorio"})
    List<Producto> findByNombreProductoContainingIgnoreCaseAndZonaIdZonaAndEstado(
            String nombre, Integer idZona, Integer estado);

    @EntityGraph(attributePaths = {"zona", "laboratorio"})
    Optional<Producto> findByIdProductoAndZonaIdZona(Integer idProducto, Integer idZona);
}
