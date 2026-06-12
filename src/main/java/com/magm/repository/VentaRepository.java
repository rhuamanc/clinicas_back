package com.magm.repository;

import com.magm.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    @Query("""
            SELECT DISTINCT v FROM Venta v
            JOIN FETCH v.usuario u
            JOIN FETCH u.cliente cl
            JOIN FETCH cl.zona z
            LEFT JOIN FETCH v.detalleVentas dv
            LEFT JOIN FETCH dv.producto p
            WHERE z.idZona = :idZona
              AND v.fechaTransaccion BETWEEN :inicio AND :fin
            ORDER BY v.fechaTransaccion DESC
            """)
    List<Venta> findByZonaAndFecha(@Param("idZona") Integer idZona,
                                   @Param("inicio") LocalDateTime inicio,
                                   @Param("fin") LocalDateTime fin);
}
