package com.urbanbites.repository;

import com.urbanbites.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Rol findByNombre(String nombre);
}

