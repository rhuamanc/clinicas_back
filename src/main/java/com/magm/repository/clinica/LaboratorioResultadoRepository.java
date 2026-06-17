package com.magm.repository.clinica;

import com.magm.entity.clinica.LaboratorioResultado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaboratorioResultadoRepository extends JpaRepository<LaboratorioResultado, Integer> {
    List<LaboratorioResultado> findByOrdenIdOrdenLaboratorio(Integer idOrdenLaboratorio);
}
