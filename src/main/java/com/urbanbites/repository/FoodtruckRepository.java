package com.urbanbites.repository;

import com.urbanbites.domain.Foodtruck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface FoodtruckRepository extends JpaRepository<Foodtruck, Integer> {
    List<Foodtruck> findByActivoTrue();
    List<Foodtruck> findByDuenoIdUsuario(Integer idDueno);
    
    @Query("SELECT f FROM Foodtruck f LEFT JOIN FETCH f.dueno ORDER BY f.nombre")
    List<Foodtruck> findAllWithDueno();
}

