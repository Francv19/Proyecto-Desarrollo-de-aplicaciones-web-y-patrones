package com.urbanbites.repository;

import com.urbanbites.domain.HorarioFoodtruck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface HorarioFoodtruckRepository extends JpaRepository<HorarioFoodtruck, Integer> {
    List<HorarioFoodtruck> findByFoodtruckIdFoodtruck(Integer idFoodtruck);
    
    @Query("SELECT h FROM HorarioFoodtruck h LEFT JOIN FETCH h.foodtruck WHERE h.foodtruck.idFoodtruck = :idFoodtruck AND h.activo = true ORDER BY h.diaSemana, h.horaApertura")
    List<HorarioFoodtruck> findHorariosActivosPorFoodtruck(@Param("idFoodtruck") Integer idFoodtruck);
    
    @Query("SELECT h FROM HorarioFoodtruck h LEFT JOIN FETCH h.foodtruck WHERE h.activo = true ORDER BY h.foodtruck.nombre, h.diaSemana, h.horaApertura")
    List<HorarioFoodtruck> findAllHorariosActivos();
}

