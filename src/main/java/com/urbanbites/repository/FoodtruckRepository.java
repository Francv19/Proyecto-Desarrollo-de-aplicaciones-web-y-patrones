package com.urbanbites.repository;

import com.urbanbites.domain.Foodtruck;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoodtruckRepository extends JpaRepository<Foodtruck, Integer> {
    List<Foodtruck> findByActivoTrue();
    List<Foodtruck> findByDuenoIdUsuario(Integer idDueno);
}

