package com.urbanbites.service;

import com.urbanbites.domain.Pedido;
import com.urbanbites.domain.PuntosCliente;
import com.urbanbites.domain.ReglaPuntos;
import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.repository.PedidoRepository;
import com.urbanbites.repository.PuntosClienteRepository;
import com.urbanbites.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
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
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private FoodtruckRepository foodtruckRepository;


    public PuntosInfo calcularPuntosAObtener(BigDecimal totalNeto, Integer idFoodtruck) {
        if (totalNeto == null || totalNeto.compareTo(BigDecimal.ZERO) <= 0) {
            return new PuntosInfo(0, 0, "Sin puntos", false);
        }
        
        Integer porcentajePuntos = null;
        String tipoRegla = "Porcentaje general";
        boolean esReglaEspecial = false;
        
        ReglaPuntos reglaVigente = reglaPuntosService.obtenerReglaVigente(idFoodtruck);
        
        if (reglaVigente != null && reglaVigente.getPorcentaje() != null) {
            porcentajePuntos = reglaVigente.getPorcentaje();
            tipoRegla = "Regla especial (" + porcentajePuntos + "%)";
            esReglaEspecial = true;
        } else {
            com.urbanbites.domain.Foodtruck foodtruck = foodtruckRepository.findById(idFoodtruck)
                .orElse(null);
            if (foodtruck != null && foodtruck.getPorcentajePuntos() != null) {
                porcentajePuntos = foodtruck.getPorcentajePuntos();
                tipoRegla = "Porcentaje general (" + porcentajePuntos + "%)";
            }
        }
        
        if (porcentajePuntos != null && porcentajePuntos > 0) {
            Integer puntos = totalNeto.multiply(new BigDecimal(porcentajePuntos))
                .divide(new BigDecimal(100), 0, java.math.RoundingMode.DOWN)
                .intValue();
            
            return new PuntosInfo(puntos, porcentajePuntos, tipoRegla, esReglaEspecial);
        }
        
        return new PuntosInfo(0, 0, "Sin puntos", false);
    }
    
    /**
     * Clase auxiliar para información de puntos
     */
    public static class PuntosInfo {
        private final Integer puntos;
        private final Integer porcentaje;
        private final String descripcion;
        private final boolean esReglaEspecial;
        
        public PuntosInfo(Integer puntos, Integer porcentaje, String descripcion, boolean esReglaEspecial) {
            this.puntos = puntos;
            this.porcentaje = porcentaje;
            this.descripcion = descripcion;
            this.esReglaEspecial = esReglaEspecial;
        }
        
        public Integer getPuntos() { return puntos; }
        public Integer getPorcentaje() { return porcentaje; }
        public String getDescripcion() { return descripcion; }
        public boolean isEsReglaEspecial() { return esReglaEspecial; }
    }
    
    public void acumularPuntosPorPedido(Integer idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        if (pedido.getEstado() == Pedido.EstadoPedido.entregado) {
            PuntosInfo puntosInfo = calcularPuntosAObtener(pedido.getTotalNeto(), pedido.getFoodtruck().getIdFoodtruck());
            
            if (puntosInfo.getPuntos() > 0) {
                PuntosCliente movimiento = new PuntosCliente();
                movimiento.setUsuario(pedido.getUsuario());
                movimiento.setFoodtruck(pedido.getFoodtruck());
                movimiento.setPedido(pedido);
                movimiento.setTipo(PuntosCliente.TipoPunto.acumulados);
                movimiento.setPuntos(puntosInfo.getPuntos());
                
                String motivo = String.format("Pedido #%d: %d puntos (%s) - Total: ₡%.2f", 
                    idPedido, 
                    puntosInfo.getPuntos(), 
                    puntosInfo.getDescripcion(),
                    pedido.getTotalNeto().doubleValue());
                movimiento.setMotivo(motivo);
                movimiento.setFechaCreacion(java.time.LocalDateTime.now());
                
                puntosClienteRepository.save(movimiento);
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
    
    /**
     * Obtiene los puntos acumulados para un pedido específico
     * @param idPedido ID del pedido
     * @return Puntos acumulados o null si no hay puntos
     */
    public PuntosCliente obtenerPuntosPorPedido(Integer idPedido) {
        List<PuntosCliente> movimientos = puntosClienteRepository.findByPedidoIdPedidoAndTipoAcumulados(idPedido);
        return movimientos.isEmpty() ? null : movimientos.get(0);
    }

    public BigDecimal redimirPuntos(Integer idUsuario, Integer puntosACanjear, Integer idPedido, Integer idFoodtruck) {
        if (puntosACanjear == null || puntosACanjear <= 0) {
            return BigDecimal.ZERO;
        }
        
        Integer saldoDisponible = obtenerSaldoPuntos(idUsuario);
        if (saldoDisponible < puntosACanjear) {
            throw new RuntimeException("No tienes suficientes puntos. Saldo disponible: " + saldoDisponible);
        }
        
        // Conversión: 10 puntos = 1 colón
        BigDecimal descuento = new BigDecimal(puntosACanjear).divide(new BigDecimal(10), 2, java.math.RoundingMode.DOWN);
        
        // Registrar el movimiento de puntos redimidos
        PuntosCliente movimiento = new PuntosCliente();
        Pedido pedido = null;
        if (idPedido != null) {
            pedido = pedidoRepository.findById(idPedido).orElse(null);
        }
        
        movimiento.setUsuario(pedido != null ? pedido.getUsuario() : 
            usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado")));
        movimiento.setFoodtruck(pedido != null ? pedido.getFoodtruck() :
            foodtruckRepository.findById(idFoodtruck)
                .orElseThrow(() -> new RuntimeException("Food truck no encontrado")));
        movimiento.setPedido(pedido);
        movimiento.setTipo(PuntosCliente.TipoPunto.redimidos);
        movimiento.setPuntos(puntosACanjear);
        movimiento.setMotivo("Canje de puntos en pedido" + (idPedido != null ? " #" + idPedido : ""));
        movimiento.setFechaCreacion(java.time.LocalDateTime.now());
        
        puntosClienteRepository.save(movimiento);
        
        return descuento;
    }
}

