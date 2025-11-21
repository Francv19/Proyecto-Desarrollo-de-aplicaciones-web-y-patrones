package com.urbanbites.repository;

import com.urbanbites.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.detalles WHERE p.usuario.idUsuario = :idUsuario ORDER BY p.fechaCreacion DESC")
    List<Pedido> findByUsuarioIdUsuario(@Param("idUsuario") Integer idUsuario);
    
    List<Pedido> findByFoodtruckIdFoodtruck(Integer idFoodtruck);
    
    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.detalles LEFT JOIN FETCH p.foodtruck f LEFT JOIN FETCH f.dueno WHERE f.dueno.idUsuario = :idDueno ORDER BY p.fechaCreacion DESC")
    List<Pedido> findPedidosPorDueno(@Param("idDueno") Integer idDueno);
}

