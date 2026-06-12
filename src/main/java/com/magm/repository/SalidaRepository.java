package com.magm.repository;

import com.magm.entity.Salida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SalidaRepository extends JpaRepository<Salida, Integer> {

    @Query("""
            SELECT DISTINCT s FROM Salida s
            JOIN FETCH s.usuario u
            JOIN FETCH u.cliente cl
            JOIN FETCH cl.zona z
            LEFT JOIN FETCH s.detalleSalidas ds
            LEFT JOIN FETCH ds.producto pr
            WHERE z.idZona = :idZona
              AND s.fechaSalida BETWEEN :inicio AND :fin
            ORDER BY s.fechaSalida DESC
            """)
    List<Salida> findByZonaAndFecha(@Param("idZona") Integer idZona,
                                    @Param("inicio") LocalDateTime inicio,
                                    @Param("fin") LocalDateTime fin);
}
