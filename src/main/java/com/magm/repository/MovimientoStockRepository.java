package com.magm.repository;

import com.magm.entity.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Integer> {
    List<MovimientoStock> findByIdZonaAndFechaBetweenOrderByFechaDesc(Integer idZona, LocalDateTime inicio, LocalDateTime fin);
}
