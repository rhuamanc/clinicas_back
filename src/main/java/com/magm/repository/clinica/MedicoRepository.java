package com.magm.repository.clinica;

import com.magm.entity.clinica.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Integer> {
    List<Medico> findByActivoTrueOrderByApellidosAscNombresAsc();
}
