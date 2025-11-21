package com.urbanbites.repository;

import com.urbanbites.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Integer> {
    List<Menu> findByFoodtruckIdFoodtruckAndActivoTrue(Integer idFoodtruck);
    List<Menu> findByFoodtruckIdFoodtruck(Integer idFoodtruck);
}

