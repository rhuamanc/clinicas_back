package com.magm.repository.clinica;

import com.magm.entity.clinica.ProcedimientoAtencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcedimientoAtencionRepository extends JpaRepository<ProcedimientoAtencion, Integer> {
    List<ProcedimientoAtencion> findByAtencionIdAtencion(Integer idAtencion);
}
