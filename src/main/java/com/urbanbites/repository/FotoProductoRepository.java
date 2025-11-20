package com.urbanbites.repository;

import com.urbanbites.domain.FotoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FotoProductoRepository extends JpaRepository<FotoProducto, Integer> {
    List<FotoProducto> findByProductoIdProductoAndActivoTrue(Integer idProducto);
}

