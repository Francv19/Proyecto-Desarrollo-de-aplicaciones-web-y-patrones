package com.urbanbites.repository;

import com.urbanbites.domain.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Integer> {
    @Query("SELECT e FROM Evento e LEFT JOIN FETCH e.foodtruck f LEFT JOIN FETCH f.dueno " +
           "LEFT JOIN FETCH e.solicitante LEFT JOIN FETCH e.duenoCotizador " +
           "WHERE e.solicitante.idUsuario = :idUsuario ORDER BY e.fechaCreacion DESC")
    List<Evento> findEventosPorSolicitante(@Param("idUsuario") Integer idUsuario);
    
    @Query("SELECT e FROM Evento e LEFT JOIN FETCH e.foodtruck f LEFT JOIN FETCH f.dueno " +
           "LEFT JOIN FETCH e.solicitante LEFT JOIN FETCH e.duenoCotizador " +
           "WHERE f.dueno.idUsuario = :idDueno ORDER BY e.fechaCreacion DESC")
    List<Evento> findEventosPorDueno(@Param("idDueno") Integer idDueno);
    
    @Query("SELECT e FROM Evento e LEFT JOIN FETCH e.foodtruck f LEFT JOIN FETCH f.dueno " +
           "LEFT JOIN FETCH e.solicitante LEFT JOIN FETCH e.duenoCotizador " +
           "WHERE e.foodtruck.idFoodtruck = :idFoodtruck ORDER BY e.fechaCreacion DESC")
    List<Evento> findEventosPorFoodtruck(@Param("idFoodtruck") Integer idFoodtruck);
    
    @Query("SELECT e FROM Evento e WHERE e.solicitante.idUsuario = :idUsuario OR e.duenoCotizador.idUsuario = :idUsuario")
    List<Evento> findEventosPorUsuario(@Param("idUsuario") Integer idUsuario);
}

