package com.magm.repository.clinica;

import com.magm.entity.clinica.HistoriaClinicaEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriaClinicaEventoRepository extends JpaRepository<HistoriaClinicaEvento, Integer> {
    List<HistoriaClinicaEvento> findByPacienteIdPacienteOrderByFechaEventoDesc(Integer idPaciente);
}
