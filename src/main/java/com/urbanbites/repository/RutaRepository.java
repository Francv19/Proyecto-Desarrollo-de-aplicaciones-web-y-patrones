package com.urbanbites.repository;

import com.urbanbites.domain.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RutaRepository extends JpaRepository<Ruta, Integer> {
    @Query("SELECT r FROM Ruta r LEFT JOIN FETCH r.rol WHERE r.requiereRol = false")
    List<Ruta> findRutasPublicas();
    
    @Query("SELECT r FROM Ruta r LEFT JOIN FETCH r.rol WHERE r.requiereRol = true AND r.rol.idRol = :idRol")
    List<Ruta> findRutasPorRol(@Param("idRol") Integer idRol);
    
    @Query("SELECT r FROM Ruta r LEFT JOIN FETCH r.rol")
    List<Ruta> findAllRutas();
    
    Ruta findByRuta(String ruta);
}


