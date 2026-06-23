package com.magm.repository.clinica;

import com.magm.entity.clinica.ExamenLaboratorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ExamenLaboratorioRepository extends JpaRepository<ExamenLaboratorio, Integer> {
    Optional<ExamenLaboratorio> findByCodigoIgnoreCase(String codigo);

    Optional<ExamenLaboratorio> findByNombreIgnoreCase(String nombre);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByNombreIgnoreCase(String nombre);

    @Query("""
            SELECT e
            FROM ExamenLaboratorio e
            WHERE (:activo IS NULL OR e.activo = :activo)
              AND (:area IS NULL OR e.areaLaboratorio = :area)
              AND (
                    :q IS NULL
                    OR LOWER(e.codigo) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
                  )
            ORDER BY e.nombre ASC
            """)
    Page<ExamenLaboratorio> buscar(Boolean activo, String area, String q, Pageable pageable);
}
