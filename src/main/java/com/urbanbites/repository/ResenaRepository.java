package com.urbanbites.repository;

import com.urbanbites.domain.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Integer> {
    List<Resena> findByFoodtruckIdFoodtruckAndEstado(Integer idFoodtruck, Resena.EstadoResena estado);
    Resena findByPedidoIdPedido(Integer idPedido);
    
    @Query("SELECT r FROM Resena r LEFT JOIN FETCH r.foodtruck LEFT JOIN FETCH r.usuario LEFT JOIN FETCH r.pedido WHERE r.usuario.idUsuario = :idUsuario ORDER BY r.fechaCreacion DESC")
    List<Resena> findByUsuarioIdUsuario(@Param("idUsuario") Integer idUsuario);
    
    @Query("SELECT DISTINCT r FROM Resena r " +
           "LEFT JOIN FETCH r.foodtruck f " +
           "LEFT JOIN FETCH f.dueno " +
           "LEFT JOIN FETCH r.usuario " +
           "LEFT JOIN FETCH r.pedido p " +
           "LEFT JOIN FETCH p.usuario " +
           "WHERE f.dueno.idUsuario = :idDueno ORDER BY r.fechaCreacion DESC")
    List<Resena> findResenasPorDueno(@Param("idDueno") Integer idDueno);
}

