package com.magm.repository.clinica;

import com.magm.entity.clinica.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {
    List<Especialidad> findByActivaTrueOrderByNombreAsc();
}
