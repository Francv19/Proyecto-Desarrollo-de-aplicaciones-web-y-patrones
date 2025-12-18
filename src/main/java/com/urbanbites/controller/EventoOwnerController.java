package com.urbanbites.controller;

import com.urbanbites.domain.Evento;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/owner/eventos")
public class EventoOwnerController {
    @Autowired
    private EventoService eventoService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String verEventos(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            List<Evento> eventos = eventoService.obtenerEventosPorDueno(usuario.getIdUsuario());
            model.addAttribute("eventos", eventos != null ? eventos : new java.util.ArrayList<>());
            model.addAttribute("page", "eventos");
            
            return "owner/eventos/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar eventos: " + e.getMessage());
            model.addAttribute("eventos", new java.util.ArrayList<>());
            model.addAttribute("page", "eventos");
            return "owner/eventos/index";
        }
    }

    @GetMapping("/{idEvento}/cotizar")
    public String mostrarFormularioCotizacion(@PathVariable Integer idEvento, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            Evento evento = eventoService.obtenerEventoPorId(idEvento);
            if (evento == null || !evento.getFoodtruck().getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                return "redirect:/owner/eventos";
            }
            
            model.addAttribute("evento", evento);
            model.addAttribute("page", "eventos");
            model.addAttribute("esEdicion", evento.getEstado() == Evento.EstadoEvento.cotizado);
            
            return "owner/eventos/cotizar";
        } catch (Exception e) {
            return "redirect:/owner/eventos";
        }
    }

    @PostMapping("/{idEvento}/cotizar")
    public String cotizarEvento(@PathVariable Integer idEvento,
                               @RequestParam BigDecimal montoCotizado,
                               @RequestParam(required = false) String detallesCotizacion,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            Evento evento = eventoService.obtenerEventoPorId(idEvento);
            if (evento == null || !evento.getFoodtruck().getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para modificar este evento");
                return "redirect:/owner/eventos";
            }
            
            if (evento.getEstado() == Evento.EstadoEvento.cotizado) {
                // Actualizar cotización existente
                eventoService.actualizarCotizacion(idEvento, usuario.getIdUsuario(), montoCotizado, detallesCotizacion);
                redirectAttributes.addFlashAttribute("mensaje", "Cotización actualizada exitosamente");
            } else {
                // Crear nueva cotización
                eventoService.cotizarEvento(idEvento, usuario.getIdUsuario(), montoCotizado, detallesCotizacion);
                redirectAttributes.addFlashAttribute("mensaje", "Cotización enviada exitosamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/eventos";
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
            if (evento == null || !evento.getFoodtruck().getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para modificar este evento");
                return "redirect:/owner/eventos";
            }
            
            Evento.EstadoEvento nuevoEstado = Evento.EstadoEvento.valueOf(estado);
            eventoService.actualizarEstadoEvento(idEvento, nuevoEstado);
            redirectAttributes.addFlashAttribute("mensaje", "Estado actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/eventos";
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
            
            eventoService.eliminarEvento(idEvento, usuario.getIdUsuario(), true);
            redirectAttributes.addFlashAttribute("mensaje", "Evento eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/eventos";
    }
}

