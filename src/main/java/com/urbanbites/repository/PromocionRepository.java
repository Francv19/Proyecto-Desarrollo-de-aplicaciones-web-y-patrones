package com.urbanbites.repository;

import com.urbanbites.domain.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Integer> {
    @Query("SELECT p FROM Promocion p WHERE p.activo = true " +
           "AND p.fechaInicio <= :ahora AND p.fechaFin >= :ahora ORDER BY p.fechaInicio ASC")
    List<Promocion> findPromocionesVigentes(@Param("ahora") LocalDateTime ahora);
    
    List<Promocion> findByFoodtruckIdFoodtruck(Integer idFoodtruck);
}

