package com.magm.repository.clinica;

import com.magm.entity.clinica.Admision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdmisionRepository extends JpaRepository<Admision, Integer> {
    List<Admision> findByEstadoOrderByFechaLlegadaDesc(String estado);
    List<Admision> findAllByOrderByFechaLlegadaDesc();
}
