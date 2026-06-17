package com.magm.repository.clinica;

import com.magm.entity.clinica.Triaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TriajeRepository extends JpaRepository<Triaje, Integer> {
    List<Triaje> findByAdmisionIdAdmisionOrderByFechaRegistroDesc(Integer idAdmision);
}
