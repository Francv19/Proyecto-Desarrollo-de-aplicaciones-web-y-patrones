package com.urbanbites.repository;

import com.urbanbites.domain.DetalleCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Integer> {
    List<DetalleCarrito> findByCarritoIdCarrito(Integer idCarrito);
    void deleteByCarritoIdCarrito(Integer idCarrito);
}

