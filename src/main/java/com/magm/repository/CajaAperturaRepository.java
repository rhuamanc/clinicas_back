package com.magm.repository;

import com.magm.entity.CajaApertura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CajaAperturaRepository extends JpaRepository<CajaApertura, Integer> {
    List<CajaApertura> findByUsuarioClienteZonaIdZonaOrderByFechaAperturaDesc(Integer idZona);
}
