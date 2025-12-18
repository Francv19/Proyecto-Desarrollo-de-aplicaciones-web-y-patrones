package com.urbanbites.controller;

import com.urbanbites.domain.Evento;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.EventoService;
import com.urbanbites.service.FoodtruckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/eventos")
public class EventoController {
    @Autowired
    private EventoService eventoService;
    
    @Autowired
    private FoodtruckService foodtruckService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String verMisEventos(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            List<Evento> eventos = eventoService.obtenerEventosPorSolicitante(usuario.getIdUsuario());
            model.addAttribute("eventos", eventos != null ? eventos : new java.util.ArrayList<>());
            model.addAttribute("page", "eventos");
            
            return "eventos/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar eventos: " + e.getMessage());
            model.addAttribute("eventos", new java.util.ArrayList<>());
            return "eventos/index";
        }
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(@RequestParam(required = false) Integer idFoodtruck, Model model) {
        try {
            List<com.urbanbites.domain.Foodtruck> foodtrucks = foodtruckService.obtenerTodosFoodtrucks();
            model.addAttribute("foodtrucks", foodtrucks);
            if (idFoodtruck != null) {
                model.addAttribute("foodtruckSeleccionado", idFoodtruck);
            }
            model.addAttribute("evento", new Evento());
            model.addAttribute("page", "eventos");
            
            return "eventos/form";
        } catch (Exception e) {
            return "redirect:/eventos";
        }
    }

    @PostMapping("/crear")
    public String crearSolicitud(@RequestParam Integer idFoodtruck,
                                @RequestParam String tipoServicio,
                                @RequestParam String nombre,
                                @RequestParam(required = false) String descripcion,
                                @RequestParam String direccion,
                                @RequestParam(required = false) Integer invitados,
                                @RequestParam String fechaInicio,
                                @RequestParam String fechaFin,
                                @RequestParam(required = false) BigDecimal latitud,
                                @RequestParam(required = false) BigDecimal longitud,
                                RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            Evento.TipoServicio tipo = Evento.TipoServicio.valueOf(tipoServicio);
            LocalDateTime fechaInicioDT = LocalDateTime.parse(fechaInicio);
            LocalDateTime fechaFinDT = LocalDateTime.parse(fechaFin);
            
            eventoService.crearSolicitudEvento(usuario.getIdUsuario(), idFoodtruck, tipo, nombre,
                                             descripcion, direccion, invitados, fechaInicioDT,
                                             fechaFinDT, latitud, longitud);
            redirectAttributes.addFlashAttribute("mensaje", "Solicitud de evento creada exitosamente. El dueño del food truck recibirá una notificación.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/eventos";
    }
    
    @GetMapping("/{idEvento}/editar")
    public String mostrarFormularioEdicion(@PathVariable Integer idEvento, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            Evento evento = eventoService.obtenerEventoPorId(idEvento);
            if (evento == null || !evento.getSolicitante().getIdUsuario().equals(usuario.getIdUsuario())) {
                return "redirect:/eventos";
            }
            
            if (evento.getEstado() != Evento.EstadoEvento.pendiente) {
                return "redirect:/eventos";
            }
            
            List<com.urbanbites.domain.Foodtruck> foodtrucks = foodtruckService.obtenerTodosFoodtrucks();
            model.addAttribute("foodtrucks", foodtrucks);
            model.addAttribute("evento", evento);
            model.addAttribute("page", "eventos");
            
            return "eventos/form";
        } catch (Exception e) {
            return "redirect:/eventos";
        }
    }
    
    @PostMapping("/{idEvento}/actualizar")
    public String actualizarEvento(@PathVariable Integer idEvento,
                                   @RequestParam Integer idFoodtruck,
                                   @RequestParam String tipoServicio,
                                   @RequestParam String nombre,
                                   @RequestParam(required = false) String descripcion,
                                   @RequestParam String direccion,
                                   @RequestParam(required = false) Integer invitados,
                                   @RequestParam String fechaInicio,
                                   @RequestParam String fechaFin,
                                   @RequestParam(required = false) BigDecimal latitud,
                                   @RequestParam(required = false) BigDecimal longitud,
                                   RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            Evento.TipoServicio tipo = Evento.TipoServicio.valueOf(tipoServicio);
            LocalDateTime fechaInicioDT = LocalDateTime.parse(fechaInicio);
            LocalDateTime fechaFinDT = LocalDateTime.parse(fechaFin);
            
            eventoService.actualizarEvento(idEvento, usuario.getIdUsuario(), idFoodtruck, tipo, nombre,
                                         descripcion, direccion, invitados, fechaInicioDT,
                                         fechaFinDT, latitud, longitud);
            redirectAttributes.addFlashAttribute("mensaje", "Evento actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/eventos";
    }
    
    @PostMapping("/{idEvento}/estado")
    public String actualizarEstado(@PathVariable Integer idEvento,
                                   @RequestParam String estado,
                                   RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            Evento evento = eventoService.obtenerEventoPorId(idEvento);
            if (evento == null || !evento.getSolicitante().getIdUsuario().equals(usuario.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para modificar este evento");
                return "redirect:/eventos";
            }
            
            Evento.EstadoEvento nuevoEstado = Evento.EstadoEvento.valueOf(estado);
            
            // Validaciones de transición de estado
            if (nuevoEstado == Evento.EstadoEvento.aceptado && evento.getEstado() != Evento.EstadoEvento.cotizado) {
                redirectAttributes.addFlashAttribute("error", "Solo se pueden aceptar eventos cotizados");
                return "redirect:/eventos";
            }
            
            if (nuevoEstado == Evento.EstadoEvento.rechazado && evento.getEstado() != Evento.EstadoEvento.cotizado) {
                redirectAttributes.addFlashAttribute("error", "Solo se pueden rechazar eventos cotizados");
                return "redirect:/eventos";
            }
            
            if (nuevoEstado == Evento.EstadoEvento.cancelado && evento.getEstado() != Evento.EstadoEvento.pendiente) {
                redirectAttributes.addFlashAttribute("error", "Solo se pueden cancelar eventos pendientes");
                return "redirect:/eventos";
            }
            
            eventoService.actualizarEstadoEvento(idEvento, nuevoEstado);
            
            String mensaje = nuevoEstado == Evento.EstadoEvento.aceptado ? 
                "Cotización aceptada exitosamente" :
                nuevoEstado == Evento.EstadoEvento.rechazado ?
                "Cotización rechazada" :
                "Evento cancelado exitosamente";
            
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/eventos";
    }
    
    @PostMapping("/{idEvento}/eliminar")
    public String eliminarEvento(@PathVariable Integer idEvento,
                                RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            eventoService.eliminarEvento(idEvento, usuario.getIdUsuario(), false);
            redirectAttributes.addFlashAttribute("mensaje", "Evento eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/eventos";
    }
}

