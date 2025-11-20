package com.urbanbites.repository;

import com.urbanbites.domain.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Integer> {
    List<Resena> findByFoodtruckIdFoodtruckAndEstado(Integer idFoodtruck, Resena.EstadoResena estado);
    Resena findByPedidoIdPedido(Integer idPedido);
    List<Resena> findByUsuarioIdUsuario(Integer idUsuario);
}

