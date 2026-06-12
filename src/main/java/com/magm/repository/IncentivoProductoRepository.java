package com.magm.repository;

import com.magm.entity.IncentivoProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncentivoProductoRepository extends JpaRepository<IncentivoProducto, Integer> {
    List<IncentivoProducto> findByProductoZonaIdZonaAndEstadoOrderByFechaRegistroDesc(Integer idZona, Integer estado);

    List<IncentivoProducto> findByProductoIdProductoAndEstadoOrderByFechaRegistroDesc(Integer idProducto, Integer estado);
}
