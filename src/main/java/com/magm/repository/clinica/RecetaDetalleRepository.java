package com.magm.repository.clinica;

import com.magm.entity.clinica.RecetaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaDetalleRepository extends JpaRepository<RecetaDetalle, Integer> {
    List<RecetaDetalle> findByRecetaIdReceta(Integer idReceta);
}
