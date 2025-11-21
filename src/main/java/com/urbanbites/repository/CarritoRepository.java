package com.urbanbites.repository;

import com.urbanbites.domain.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Integer> {
    @Query("SELECT c FROM Carrito c WHERE c.usuario.idUsuario = :idUsuario AND c.estado = :estado")
    Optional<Carrito> findByUsuarioIdUsuarioAndEstado(@Param("idUsuario") Integer idUsuario, @Param("estado") Carrito.EstadoCarrito estado);
    
    List<Carrito> findByUsuarioIdUsuario(Integer idUsuario);
}

