package com.urbanbites.service;

import com.urbanbites.domain.Pedido;
import com.urbanbites.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class PedidoService {
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private PuntosService puntosService;

    public Pedido actualizarEstado(Integer idPedido, Pedido.EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(idPedido)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        Pedido.EstadoPedido estadoAnterior = pedido.getEstado();
        pedido.setEstado(nuevoEstado);
        pedido = pedidoRepository.save(pedido);
        
        // Si el pedido cambió a "entregado", acumular puntos
        if (estadoAnterior != Pedido.EstadoPedido.entregado && 
            nuevoEstado == Pedido.EstadoPedido.entregado) {
            try {
                puntosService.acumularPuntosPorPedido(idPedido);
            } catch (Exception e) {
                // Log el error pero no fallar la actualización del estado
                System.err.println("Error al acumular puntos: " + e.getMessage());
            }
        }
        
        return pedido;
    }

    public Pedido actualizarEta(Integer idPedido, Integer etaMinutos) {
        Pedido pedido = pedidoRepository.findById(idPedido)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setEtaMinutos(etaMinutos);
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> obtenerPedidosPorUsuario(Integer idUsuario) {
        return pedidoRepository.findByUsuarioIdUsuario(idUsuario);
    }

    public List<Pedido> obtenerPedidosPorDueno(Integer idDueno) {
        return pedidoRepository.findPedidosPorDueno(idDueno);
    }
    
    public Pedido obtenerPedidoPorId(Integer idPedido) {
        return pedidoRepository.findById(idPedido).orElse(null);
    }
}

