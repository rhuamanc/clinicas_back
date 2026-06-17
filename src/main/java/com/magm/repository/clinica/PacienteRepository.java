package com.magm.repository.clinica;

import com.magm.entity.clinica.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    Optional<Paciente> findByDni(String dni);
    List<Paciente> findByEstadoOrderByApellidosAscNombresAsc(Integer estado);
    List<Paciente> findByEstadoAndNombresContainingIgnoreCaseOrEstadoAndApellidosContainingIgnoreCase(Integer estadoNombres, String nombres, Integer estadoApellidos, String apellidos);
}
