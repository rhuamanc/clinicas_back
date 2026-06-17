package com.magm.repository.clinica;

import com.magm.entity.clinica.AtencionMedica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtencionMedicaRepository extends JpaRepository<AtencionMedica, Integer> {
    List<AtencionMedica> findByAdmisionPacienteIdPacienteOrderByFechaAtencionDesc(Integer idPaciente);
    List<AtencionMedica> findAllByOrderByFechaAtencionDesc();
}
