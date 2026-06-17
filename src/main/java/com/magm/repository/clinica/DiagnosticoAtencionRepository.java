package com.magm.repository.clinica;

import com.magm.entity.clinica.DiagnosticoAtencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosticoAtencionRepository extends JpaRepository<DiagnosticoAtencion, Integer> {
    List<DiagnosticoAtencion> findByAtencionIdAtencion(Integer idAtencion);
}
