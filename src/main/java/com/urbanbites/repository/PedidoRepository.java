package com.urbanbites.repository;

import com.urbanbites.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByUsuarioIdUsuario(Integer idUsuario);
    List<Pedido> findByFoodtruckIdFoodtruck(Integer idFoodtruck);
    
    @Query("SELECT p FROM Pedido p WHERE p.foodtruck.dueno.idUsuario = :idDueno ORDER BY p.fechaCreacion DESC")
    List<Pedido> findPedidosPorDueno(@Param("idDueno") Integer idDueno);
}

