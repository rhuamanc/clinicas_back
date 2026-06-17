package com.magm.repository.clinica;

import com.magm.entity.clinica.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Integer> {
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraAsc(LocalDateTime inicio, LocalDateTime fin);
    boolean existsByMedicoIdMedicoAndFechaHora(Integer idMedico, LocalDateTime fechaHora);
}
