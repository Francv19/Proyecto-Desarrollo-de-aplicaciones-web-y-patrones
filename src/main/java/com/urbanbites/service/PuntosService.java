package com.urbanbites.service;

import com.urbanbites.domain.Pedido;
import com.urbanbites.domain.PuntosCliente;
import com.urbanbites.domain.ReglaPuntos;
import com.urbanbites.repository.PedidoRepository;
import com.urbanbites.repository.PuntosClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class PuntosService {
    @Autowired
    private PuntosClienteRepository puntosClienteRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private ReglaPuntosService reglaPuntosService;

    public void acumularPuntosPorPedido(Integer idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        if (pedido.getEstado() == Pedido.EstadoPedido.entregado) {
            Integer porcentajePuntos = null;
            
            // Intentar obtener regla vigente primero
            ReglaPuntos reglaVigente = reglaPuntosService.obtenerReglaVigente(
                pedido.getFoodtruck().getIdFoodtruck());
            
            if (reglaVigente != null && reglaVigente.getPorcentaje() != null) {
                porcentajePuntos = reglaVigente.getPorcentaje();
            } else {
                // Si no hay regla vigente, usar el porcentaje del foodtruck
                porcentajePuntos = pedido.getFoodtruck().getPorcentajePuntos();
            }
            
            if (porcentajePuntos != null && porcentajePuntos > 0) {
                Integer puntos = (int) (pedido.getTotalNeto().doubleValue() * porcentajePuntos / 100.0);
                
                if (puntos > 0) {
                    PuntosCliente movimiento = new PuntosCliente();
                    movimiento.setUsuario(pedido.getUsuario());
                    movimiento.setFoodtruck(pedido.getFoodtruck());
                    movimiento.setPedido(pedido);
                    movimiento.setTipo(PuntosCliente.TipoPunto.acumulados);
                    movimiento.setPuntos(puntos);
                    movimiento.setMotivo("Acumulación por pedido #" + idPedido);
                    movimiento.setFechaCreacion(java.time.LocalDateTime.now());
                    
                    puntosClienteRepository.save(movimiento);
                }
            }
        }
    }

    public Integer obtenerSaldoPuntos(Integer idUsuario) {
        Integer saldo = puntosClienteRepository.calcularSaldoPuntos(idUsuario);
        return saldo != null ? saldo : 0;
    }

    public List<PuntosCliente> obtenerMovimientosPuntos(Integer idUsuario) {
        return puntosClienteRepository.findByUsuarioIdUsuario(idUsuario);
    }
}

