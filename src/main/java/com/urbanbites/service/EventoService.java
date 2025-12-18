package com.urbanbites.service;

import com.urbanbites.domain.Evento;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.EventoRepository;
import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EventoService {
    @Autowired
    private EventoRepository eventoRepository;
    
    @Autowired
    private FoodtruckRepository foodtruckRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    public Evento crearSolicitudEvento(Integer idSolicitante, Integer idFoodtruck,
                                       Evento.TipoServicio tipoServicio, String nombre,
                                       String descripcion, String direccion, Integer invitados,
                                       LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                       BigDecimal latitud, BigDecimal longitud) {
        if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
            throw new RuntimeException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        
        Usuario solicitante = usuarioRepository.findById(idSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        com.urbanbites.domain.Foodtruck foodtruck = foodtruckRepository.findById(idFoodtruck)
            .orElseThrow(() -> new RuntimeException("Food truck no encontrado"));
        
        Evento evento = new Evento();
        evento.setSolicitante(solicitante);
        evento.setFoodtruck(foodtruck);
        evento.setTipoServicio(tipoServicio);
        evento.setNombre(nombre);
        evento.setDescripcion(descripcion);
        evento.setDireccion(direccion);
        evento.setInvitados(invitados);
        evento.setFechaInicio(fechaInicio);
        evento.setFechaFin(fechaFin);
        evento.setLatitud(latitud);
        evento.setLongitud(longitud);
        evento.setEstado(Evento.EstadoEvento.pendiente);
        evento.setFechaCreacion(LocalDateTime.now());
        
        return eventoRepository.save(evento);
    }
    
    public Evento cotizarEvento(Integer idEvento, Integer idDueno, BigDecimal montoCotizado,
                                String detallesCotizacion) {
        Evento evento = eventoRepository.findById(idEvento)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        
        if (!evento.getFoodtruck().getDueno().getIdUsuario().equals(idDueno)) {
            throw new RuntimeException("No tienes permiso para cotizar este evento");
        }
        
        if (evento.getEstado() != Evento.EstadoEvento.pendiente) {
            throw new RuntimeException("Solo se pueden cotizar eventos pendientes");
        }
        
        Usuario dueno = usuarioRepository.findById(idDueno)
            .orElseThrow(() -> new RuntimeException("Dueño no encontrado"));
        
        evento.setDuenoCotizador(dueno);
        evento.setMontoCotizado(montoCotizado);
        evento.setDetallesCotizacion(detallesCotizacion);
        evento.setFechaCotizacion(LocalDateTime.now());
        evento.setEstado(Evento.EstadoEvento.cotizado);
        
        return eventoRepository.save(evento);
    }
    
    public Evento actualizarCotizacion(Integer idEvento, Integer idDueno, BigDecimal montoCotizado,
                                       String detallesCotizacion) {
        Evento evento = eventoRepository.findById(idEvento)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        
        if (!evento.getFoodtruck().getDueno().getIdUsuario().equals(idDueno)) {
            throw new RuntimeException("No tienes permiso para modificar este evento");
        }
        
        if (evento.getEstado() != Evento.EstadoEvento.cotizado) {
            throw new RuntimeException("Solo se pueden editar cotizaciones de eventos cotizados");
        }
        
        evento.setMontoCotizado(montoCotizado);
        evento.setDetallesCotizacion(detallesCotizacion);
        evento.setFechaCotizacion(LocalDateTime.now());
        
        return eventoRepository.save(evento);
    }
    
    public Evento actualizarEstadoEvento(Integer idEvento, Evento.EstadoEvento nuevoEstado) {
        Evento evento = eventoRepository.findById(idEvento)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        evento.setEstado(nuevoEstado);
        return eventoRepository.save(evento);
    }
    
    @Transactional(readOnly = true)
    public List<Evento> obtenerEventosPorSolicitante(Integer idSolicitante) {
        return eventoRepository.findEventosPorSolicitante(idSolicitante);
    }
    
    @Transactional(readOnly = true)
    public List<Evento> obtenerEventosPorDueno(Integer idDueno) {
        return eventoRepository.findEventosPorDueno(idDueno);
    }
    
    @Transactional(readOnly = true)
    public Evento obtenerEventoPorId(Integer idEvento) {
        return eventoRepository.findById(idEvento).orElse(null);
    }
    
    public Evento actualizarEvento(Integer idEvento, Integer idSolicitante, Integer idFoodtruck,
                                    Evento.TipoServicio tipoServicio, String nombre,
                                    String descripcion, String direccion, Integer invitados,
                                    LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                    BigDecimal latitud, BigDecimal longitud) {
        Evento evento = eventoRepository.findById(idEvento)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        
        if (!evento.getSolicitante().getIdUsuario().equals(idSolicitante)) {
            throw new RuntimeException("No tienes permiso para modificar este evento");
        }
        
        if (evento.getEstado() != Evento.EstadoEvento.pendiente) {
            throw new RuntimeException("Solo se pueden editar eventos pendientes");
        }
        
        if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
            throw new RuntimeException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        
        com.urbanbites.domain.Foodtruck foodtruck = foodtruckRepository.findById(idFoodtruck)
            .orElseThrow(() -> new RuntimeException("Food truck no encontrado"));
        
        evento.setFoodtruck(foodtruck);
        evento.setTipoServicio(tipoServicio);
        evento.setNombre(nombre);
        evento.setDescripcion(descripcion);
        evento.setDireccion(direccion);
        evento.setInvitados(invitados);
        evento.setFechaInicio(fechaInicio);
        evento.setFechaFin(fechaFin);
        evento.setLatitud(latitud);
        evento.setLongitud(longitud);
        
        return eventoRepository.save(evento);
    }
    
    public void eliminarEvento(Integer idEvento, Integer idUsuario, boolean esOwner) {
        Evento evento = eventoRepository.findById(idEvento)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        
        if (esOwner) {
            // Owner solo puede eliminar eventos de sus food trucks
            if (!evento.getFoodtruck().getDueno().getIdUsuario().equals(idUsuario)) {
                throw new RuntimeException("No tienes permiso para eliminar este evento");
            }
        } else {
            // Cliente solo puede eliminar sus propios eventos
            if (!evento.getSolicitante().getIdUsuario().equals(idUsuario)) {
                throw new RuntimeException("No tienes permiso para eliminar este evento");
            }
            // Cliente solo puede eliminar eventos pendientes o cancelados
            if (evento.getEstado() != Evento.EstadoEvento.pendiente && 
                evento.getEstado() != Evento.EstadoEvento.cancelado) {
                throw new RuntimeException("Solo se pueden eliminar eventos pendientes o cancelados");
            }
        }
        
        eventoRepository.delete(evento);
    }
}

