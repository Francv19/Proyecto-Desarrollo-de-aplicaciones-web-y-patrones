package com.urbanbites.service;

import com.urbanbites.domain.Pedido;
import com.urbanbites.domain.Resena;
import com.urbanbites.repository.PedidoRepository;
import com.urbanbites.repository.ResenaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ResenaService {
    @Autowired
    private ResenaRepository resenaRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;

    public Resena crearResena(Integer idPedido, Integer calificacion, String comentario) {
        Pedido pedido = pedidoRepository.findById(idPedido)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        if (pedido.getEstado() != Pedido.EstadoPedido.entregado) {
            throw new RuntimeException("Solo se pueden dejar reseñas de pedidos entregados");
        }
        
        if (resenaRepository.findByPedidoIdPedido(idPedido) != null) {
            throw new RuntimeException("Ya existe una reseña para este pedido");
        }
        
        if (calificacion < 1 || calificacion > 5) {
            throw new RuntimeException("La calificación debe estar entre 1 y 5");
        }
        
        Resena resena = new Resena();
        resena.setUsuario(pedido.getUsuario());
        resena.setFoodtruck(pedido.getFoodtruck());
        resena.setPedido(pedido);
        resena.setCalificacion(calificacion);
        resena.setComentario(comentario);
        resena.setEstado(Resena.EstadoResena.pendiente);
        
        return resenaRepository.save(resena);
    }

    public List<Resena> obtenerResenasAprobadasPorFoodtruck(Integer idFoodtruck) {
        return resenaRepository.findByFoodtruckIdFoodtruckAndEstado(idFoodtruck, Resena.EstadoResena.aprobada);
    }
}

