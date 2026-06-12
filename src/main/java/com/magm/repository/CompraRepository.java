package com.magm.repository;

import com.magm.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Integer> {

    @Query("""
            SELECT c FROM Compra c
            JOIN FETCH c.proveedor p
            JOIN FETCH c.detalleCompras dc
            JOIN FETCH dc.producto pr
            WHERE c.usuario.cliente.zona.idZona = :idZona
              AND c.fechaTransaccion BETWEEN :inicio AND :fin
            ORDER BY c.fechaTransaccion DESC
            """)
    List<Compra> findByZonaAndFecha(@Param("idZona") Integer idZona,
                                    @Param("inicio") LocalDateTime inicio,
                                    @Param("fin") LocalDateTime fin);
}
