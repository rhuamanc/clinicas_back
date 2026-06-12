package com.magm.repository;

import com.magm.entity.Generico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenericoRepository extends JpaRepository<Generico, Integer> {
    List<Generico> findByEstadoOrderByNombreAsc(Integer estado);
}
