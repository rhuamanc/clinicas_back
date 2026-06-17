package com.magm.repository.clinica;

import com.magm.entity.clinica.Receta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaRepository extends JpaRepository<Receta, Integer> {
    List<Receta> findByEstadoOrderByFechaRecetaDesc(String estado);
    List<Receta> findByAtencionAdmisionPacienteIdPacienteOrderByFechaRecetaDesc(Integer idPaciente);
    List<Receta> findByEstadoAndAtencionAdmisionPacienteIdPacienteOrderByFechaRecetaDesc(String estado, Integer idPaciente);
}
