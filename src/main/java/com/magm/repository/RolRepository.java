package com.magm.repository;

import com.magm.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombre(String nombre);

    @Query("SELECT r FROM Rol r WHERE r.estado = 1 ORDER BY r.nombre ASC")
    List<Rol> findAllActivos();
}
