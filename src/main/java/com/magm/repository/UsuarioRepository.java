package com.magm.repository;

import com.magm.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByNombre(String nombre);

    @Query("""
            SELECT u FROM Usuario u
            JOIN FETCH u.cliente c
            JOIN FETCH c.zona z
            WHERE u.nombre = :nombre
              AND z.idZona = :idZona
              AND u.estado = 1
              AND c.cuentaHabilitada = true
            """)
    Optional<Usuario> findByNombreAndZona(@Param("nombre") String nombre,
                                          @Param("idZona") Integer idZona);

    List<Usuario> findByEstadoAndClienteZonaIdZona(Integer estado, Integer idZona);
}
