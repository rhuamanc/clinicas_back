package com.magm.repository;

import com.magm.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query("""
            SELECT p FROM Pedido p
            JOIN FETCH p.detallePedidos dp
            JOIN FETCH dp.producto pr
            WHERE p.usuario.cliente.zona.idZona = :idZona
              AND p.fechaRegistro BETWEEN :inicio AND :fin
            ORDER BY p.fechaRegistro DESC
            """)
    List<Pedido> findByZonaAndFecha(@Param("idZona") Integer idZona,
                                    @Param("inicio") LocalDateTime inicio,
                                    @Param("fin") LocalDateTime fin);
}
