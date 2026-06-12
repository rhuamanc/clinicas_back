package com.magm.repository;

import com.magm.entity.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaboratorioRepository extends JpaRepository<Laboratorio, Integer> {
	List<Laboratorio> findByEstadoOrderByNombreLaboratorioAsc(Integer estado);
}
