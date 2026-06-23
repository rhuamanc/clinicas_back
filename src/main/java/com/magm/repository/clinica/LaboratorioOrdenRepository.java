package com.magm.repository.clinica;

import com.magm.entity.clinica.LaboratorioOrden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaboratorioOrdenRepository extends JpaRepository<LaboratorioOrden, Integer> {
    List<LaboratorioOrden> findByEstadoOrderByFechaOrdenDesc(String estado);
    List<LaboratorioOrden> findByAtencionAdmisionPacienteIdPacienteOrderByFechaOrdenDesc(Integer idPaciente);
    List<LaboratorioOrden> findByAtencionIdAtencionOrderByFechaOrdenDesc(Integer idAtencion);
    boolean existsByExamenCatalogoIdExamen(Integer idExamen);
}
