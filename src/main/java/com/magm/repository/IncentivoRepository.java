package com.magm.repository;

import com.magm.entity.Incentivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncentivoRepository extends JpaRepository<Incentivo, Integer> {
    List<Incentivo> findByUsuarioClienteZonaIdZonaOrderByFechaDesc(Integer idZona);
}
