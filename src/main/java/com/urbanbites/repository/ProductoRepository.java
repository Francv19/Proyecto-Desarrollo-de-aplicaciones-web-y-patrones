package com.urbanbites.repository;

import com.urbanbites.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByFoodtruckIdFoodtruckAndDisponibleTrue(Integer idFoodtruck);
    List<Producto> findByFoodtruckIdFoodtruck(Integer idFoodtruck);
    
    @Query("SELECT p FROM Producto p WHERE p.foodtruck.idFoodtruck = :idFoodtruck AND p.disponible = true ORDER BY p.nombre ASC")
    List<Producto> findProductosDisponiblesPorFoodtruck(@Param("idFoodtruck") Integer idFoodtruck);
    
    List<Producto> findByDisponibleTrue();
}

