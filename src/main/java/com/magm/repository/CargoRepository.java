package com.magm.repository;

import com.magm.entity.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CargoRepository extends JpaRepository<Cargo, Integer> {

    @Query("""
            SELECT c FROM Cargo c
            WHERE c.usuario.cliente.zona.idZona = :idZona
              AND c.fecha BETWEEN :inicio AND :fin
            ORDER BY c.fecha DESC
            """)
    List<Cargo> findByZonaAndFecha(@Param("idZona") Integer idZona,
                                   @Param("inicio") LocalDateTime inicio,
                                   @Param("fin") LocalDateTime fin);
}
