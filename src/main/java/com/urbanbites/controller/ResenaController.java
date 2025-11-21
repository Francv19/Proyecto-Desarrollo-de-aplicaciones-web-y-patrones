package com.urbanbites.controller;

import com.urbanbites.domain.Resena;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.ResenaRepository;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.ResenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/resenas")
public class ResenaController {
    @Autowired
    private ResenaService resenaService;
    
    @Autowired
    private ResenaRepository resenaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private com.urbanbites.service.PedidoService pedidoService;

    @GetMapping
    public String verResenas(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            List<Resena> misResenas = resenaRepository.findByUsuarioIdUsuario(usuario.getIdUsuario());
            
            // Logging para debug
            System.out.println("Mis reseñas encontradas: " + (misResenas != null ? misResenas.size() : 0));
            if (misResenas != null && !misResenas.isEmpty()) {
                for (Resena r : misResenas) {
                    System.out.println("Reseña ID: " + r.getIdResena());
                    System.out.println("  - Foodtruck: " + (r.getFoodtruck() != null ? r.getFoodtruck().getNombre() : "null"));
                    System.out.println("  - Comentario: " + (r.getComentario() != null ? r.getComentario() : "null"));
                    System.out.println("  - Calificación: " + r.getCalificacion());
                }
            }
            
            // Obtener pedidos entregados sin reseña para mostrar opción de crear
            List<com.urbanbites.domain.Pedido> pedidosEntregados = pedidoService.obtenerPedidosPorUsuario(usuario.getIdUsuario())
                .stream()
                .filter(p -> p.getEstado() == com.urbanbites.domain.Pedido.EstadoPedido.entregado)
                .filter(p -> {
                    Resena resenaExistente = resenaRepository.findByPedidoIdPedido(p.getIdPedido());
                    return resenaExistente == null;
                })
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
            
            // Obtener todas las reseñas aprobadas (de otros usuarios)
            List<Resena> todasResenas = resenaRepository.findAll();
            List<Resena> resenasOtros = todasResenas.stream()
                .filter(r -> r.getEstado() == Resena.EstadoResena.aprobada && 
                            !r.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
                .limit(8)
                .collect(java.util.stream.Collectors.toList());
            
            model.addAttribute("misResenas", misResenas);
            model.addAttribute("resenasOtros", resenasOtros);
            model.addAttribute("pedidosEntregados", pedidosEntregados);
            
            return "resenas/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar reseñas: " + e.getMessage());
            return "resenas/index";
        }
    }

    @GetMapping("/crear")
    public String mostrarFormularioResena(@RequestParam Integer idPedido, Model model) {
        try {
            com.urbanbites.domain.Pedido pedido = pedidoService.obtenerPedidoPorId(idPedido);
            if (pedido == null) {
                return "redirect:/pedidos";
            }
            
            // Verificar que el pedido pertenece al usuario actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            if (!pedido.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
                return "redirect:/pedidos";
            }
            
            // Verificar que el pedido está entregado
            if (pedido.getEstado() != com.urbanbites.domain.Pedido.EstadoPedido.entregado) {
                model.addAttribute("error", "Solo se pueden dejar reseñas de pedidos entregados");
                return "redirect:/pedidos";
            }
            
            // Verificar que no existe ya una reseña
            Resena resenaExistente = resenaRepository.findByPedidoIdPedido(idPedido);
            if (resenaExistente != null) {
                model.addAttribute("error", "Ya existe una reseña para este pedido");
                return "redirect:/pedidos";
            }
            
            model.addAttribute("pedido", pedido);
            return "resenas/crear";
        } catch (Exception e) {
            return "redirect:/pedidos";
        }
    }

    @PostMapping("/crear")
    public String crearResena(@RequestParam Integer idPedido,
                              @RequestParam Integer calificacion,
                              @RequestParam(required = false) String comentario,
                              RedirectAttributes redirectAttributes) {
        try {
            resenaService.crearResena(idPedido, calificacion, comentario);
            redirectAttributes.addFlashAttribute("mensaje", "Reseña creada exitosamente. Está pendiente de aprobación.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/resenas";
    }
    
    @GetMapping("/owner")
    public String verResenasOwner(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            List<Resena> resenas = resenaService.obtenerResenasPorDueno(usuario.getIdUsuario());
            
            System.out.println("Reseñas encontradas: " + (resenas != null ? resenas.size() : 0));
            if (resenas != null && !resenas.isEmpty()) {
                for (Resena r : resenas) {
                    System.out.println("Reseña ID: " + r.getIdResena());
                    System.out.println("  - Foodtruck: " + (r.getFoodtruck() != null ? r.getFoodtruck().getNombre() : "null"));
                    System.out.println("  - Usuario: " + (r.getUsuario() != null ? r.getUsuario().getNombre() + " " + r.getUsuario().getApellidos() : "null"));
                    System.out.println("  - Pedido: " + (r.getPedido() != null ? r.getPedido().getIdPedido() : "null"));
                    System.out.println("  - Comentario: " + (r.getComentario() != null ? r.getComentario() : "null"));
                    System.out.println("  - Calificación: " + r.getCalificacion());
                }
            }
            
            model.addAttribute("resenas", resenas != null ? resenas : new java.util.ArrayList<>());
            model.addAttribute("page", "resenas");
            
            return "resenas/owner";
        } catch (Exception e) {
            System.err.println("Error al cargar reseñas del owner: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar reseñas: " + e.getMessage());
            model.addAttribute("resenas", new java.util.ArrayList<>());
            model.addAttribute("page", "resenas");
            return "resenas/owner";
        }
    }
    
    @PostMapping("/owner/{idResena}/estado")
    public String actualizarEstadoResena(@PathVariable Integer idResena,
                                         @RequestParam String estado,
                                         RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            Resena.EstadoResena nuevoEstado = Resena.EstadoResena.valueOf(estado);
            resenaService.actualizarEstadoResena(idResena, nuevoEstado, usuario.getIdUsuario());
            redirectAttributes.addFlashAttribute("mensaje", "Estado de la reseña actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/resenas/owner";
    }
}

