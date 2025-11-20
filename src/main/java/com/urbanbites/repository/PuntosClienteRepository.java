package com.urbanbites.repository;

import com.urbanbites.domain.PuntosCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PuntosClienteRepository extends JpaRepository<PuntosCliente, Integer> {
    List<PuntosCliente> findByUsuarioIdUsuario(Integer idUsuario);
    
    @Query("SELECT COALESCE(SUM(CASE WHEN p.tipo = 'acumulados' THEN p.puntos ELSE -p.puntos END), 0) " +
           "FROM PuntosCliente p WHERE p.usuario.idUsuario = :idUsuario")
    Integer calcularSaldoPuntos(@Param("idUsuario") Integer idUsuario);
}

