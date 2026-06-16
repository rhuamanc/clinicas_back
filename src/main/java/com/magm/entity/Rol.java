package com.magm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rol_recursos", joinColumns = @JoinColumn(name = "id_rol"))
    @Column(name = "recurso", nullable = false, length = 100)
    @Builder.Default
    private List<String> recursos = new ArrayList<>();

    @Override
    public String toString() {
        return nombre;
    }
}
