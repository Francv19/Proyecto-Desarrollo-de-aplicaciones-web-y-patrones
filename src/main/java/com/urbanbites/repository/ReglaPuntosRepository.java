package com.urbanbites.repository;

import com.urbanbites.domain.ReglaPuntos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReglaPuntosRepository extends JpaRepository<ReglaPuntos, Integer> {
    List<ReglaPuntos> findByFoodtruckIdFoodtruck(Integer idFoodtruck);
    
    @Query("SELECT r FROM ReglaPuntos r WHERE r.foodtruck.idFoodtruck = :idFoodtruck " +
           "AND r.activo = true " +
           "AND r.fechaInicio <= :fecha AND r.fechaFin >= :fecha " +
           "ORDER BY r.fechaInicio DESC")
    List<ReglaPuntos> findReglaVigentePorFoodtruck(@Param("idFoodtruck") Integer idFoodtruck, 
                                                   @Param("fecha") LocalDateTime fecha);
}

