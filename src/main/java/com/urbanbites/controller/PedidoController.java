package com.urbanbites.controller;

import com.urbanbites.domain.Pedido;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.PedidoService;
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

    @GetMapping
    public String verPedidosCliente(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByUsername(auth.getName());
        
        List<Pedido> pedidos = pedidoService.obtenerPedidosPorUsuario(usuario.getIdUsuario());
        model.addAttribute("pedidos", pedidos);
        
        return "pedidos/cliente";
    }

    @GetMapping("/owner")
    public String verPedidosOwner(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByUsername(auth.getName());
        
        List<Pedido> pedidos = pedidoService.obtenerPedidosPorDueno(usuario.getIdUsuario());
        model.addAttribute("pedidos", pedidos);
        
        return "pedidos/owner";
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

