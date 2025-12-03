package com.urbanbites.controller;

import com.urbanbites.domain.Pedido;
import com.urbanbites.domain.PuntosCliente;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.PedidoService;
import com.urbanbites.service.PuntosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {
    @Autowired
    private PedidoService pedidoService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PuntosService puntosService;

    @GetMapping
    public String verPedidosCliente(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }
            
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            if (usuario == null) {
                return "redirect:/login";
            }
            
            List<Pedido> pedidos = pedidoService.obtenerPedidosPorUsuario(usuario.getIdUsuario());
            
            // Obtener puntos ganados por cada pedido entregado
            java.util.Map<Integer, PuntosCliente> puntosPorPedido = new java.util.HashMap<>();
            if (pedidos != null) {
                for (Pedido pedido : pedidos) {
                    if (pedido.getEstado() == Pedido.EstadoPedido.entregado && pedido.getIdPedido() != null) {
                        PuntosCliente puntos = puntosService.obtenerPuntosPorPedido(pedido.getIdPedido());
                        if (puntos != null) {
                            puntosPorPedido.put(pedido.getIdPedido(), puntos);
                        }
                    }
                }
            }
            
            model.addAttribute("pedidos", pedidos != null ? pedidos : new java.util.ArrayList<>());
            model.addAttribute("puntosPorPedido", puntosPorPedido);
            
            return "pedidos/cliente";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar pedidos: " + e.getMessage());
            model.addAttribute("pedidos", new java.util.ArrayList<>());
            return "pedidos/cliente";
        }
    }

    @GetMapping("/owner")
    public String verPedidosOwner(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }
            
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            if (usuario == null) {
                return "redirect:/login";
            }
            
            System.out.println("Buscando pedidos para dueño con ID: " + usuario.getIdUsuario());
            List<Pedido> pedidos = pedidoService.obtenerPedidosPorDueno(usuario.getIdUsuario());
            System.out.println("Pedidos encontrados: " + (pedidos != null ? pedidos.size() : 0));
            
            if (pedidos != null && !pedidos.isEmpty()) {
                for (Pedido p : pedidos) {
                    System.out.println("Pedido ID: " + p.getIdPedido() + ", Food Truck: " + 
                        (p.getFoodtruck() != null ? p.getFoodtruck().getNombre() : "null") + 
                        ", Dueño: " + (p.getFoodtruck() != null && p.getFoodtruck().getDueno() != null ? 
                        p.getFoodtruck().getDueno().getIdUsuario() : "null"));
                }
            }
            
            model.addAttribute("pedidos", pedidos != null ? pedidos : new java.util.ArrayList<>());
            model.addAttribute("page", "pedidos");
            
            return "pedidos/owner";
        } catch (Exception e) {
            System.err.println("Error al cargar pedidos del owner: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los pedidos: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido"));
            model.addAttribute("pedidos", new java.util.ArrayList<>());
            model.addAttribute("page", "pedidos");
            return "pedidos/owner";
        }
    }

    @PostMapping("/{idPedido}/estado")
    public String actualizarEstado(@PathVariable Integer idPedido, 
                                   @RequestParam String estado,
                                   RedirectAttributes redirectAttributes) {
        try {
            Pedido.EstadoPedido nuevoEstado = Pedido.EstadoPedido.valueOf(estado);
            pedidoService.actualizarEstado(idPedido, nuevoEstado);
            redirectAttributes.addFlashAttribute("mensaje", "Estado actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/owner";
    }

    @PostMapping("/{idPedido}/eta")
    public String actualizarEta(@PathVariable Integer idPedido,
                                @RequestParam Integer etaMinutos,
                                RedirectAttributes redirectAttributes) {
        try {
            pedidoService.actualizarEta(idPedido, etaMinutos);
            redirectAttributes.addFlashAttribute("mensaje", "ETA actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/owner";
    }
}

