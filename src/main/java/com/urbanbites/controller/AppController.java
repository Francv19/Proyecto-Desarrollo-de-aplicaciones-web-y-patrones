package com.urbanbites.controller;

import com.urbanbites.domain.Pedido;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.repository.PedidoRepository;
import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/app")
public class AppController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private FoodtruckRepository foodtruckRepository;
    
    @Autowired
    private PedidoService pedidoService;
    
    @Autowired
    private FoodtruckService foodtruckService;
    
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private PromocionService promocionService;

    @GetMapping("/cliente")
    public String appCliente() {
        return "app/cliente";
    }

    @GetMapping("/owner")
    public String appOwner(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            // Obtener food trucks del owner
            List<com.urbanbites.domain.Foodtruck> foodtrucks = foodtruckService.obtenerFoodtrucksPorDueno(usuario.getIdUsuario());
            
            // Obtener pedidos
            List<Pedido> todosPedidos = pedidoService.obtenerPedidosPorDueno(usuario.getIdUsuario());
            
            // Calcular estadísticas
            long totalPedidos = todosPedidos != null ? todosPedidos.size() : 0;
            long pedidosPendientes = todosPedidos != null ? 
                todosPedidos.stream().filter(p -> 
                    p.getEstado() == Pedido.EstadoPedido.recibido || 
                    p.getEstado() == Pedido.EstadoPedido.en_preparacion
                ).count() : 0;
            long pedidosHoy = todosPedidos != null ? 
                todosPedidos.stream().filter(p -> 
                    p.getFechaCreacion() != null && 
                    p.getFechaCreacion().toLocalDate().equals(java.time.LocalDate.now())
                ).count() : 0;
            
            // Contar productos totales
            int totalProductos = 0;
            if (foodtrucks != null) {
                for (com.urbanbites.domain.Foodtruck ft : foodtrucks) {
                    List<com.urbanbites.domain.Producto> productos = productoService.obtenerTodosProductosPorFoodtruck(ft.getIdFoodtruck());
                    totalProductos += productos != null ? productos.size() : 0;
                }
            }
            
            // Contar promociones activas
            int totalPromociones = 0;
            if (foodtrucks != null) {
                for (com.urbanbites.domain.Foodtruck ft : foodtrucks) {
                    List<com.urbanbites.domain.Promocion> promociones = promocionService.obtenerPromocionesPorFoodtruck(ft.getIdFoodtruck());
                    totalPromociones += promociones != null ? promociones.size() : 0;
                }
            }
            
            // Pedidos recientes (últimos 5)
            List<Pedido> pedidosRecientes = todosPedidos != null ? 
                todosPedidos.stream()
                    .sorted((p1, p2) -> {
                        if (p1.getFechaCreacion() == null) return 1;
                        if (p2.getFechaCreacion() == null) return -1;
                        return p2.getFechaCreacion().compareTo(p1.getFechaCreacion());
                    })
                    .limit(5)
                    .collect(Collectors.toList()) : new java.util.ArrayList<>();
            
            model.addAttribute("foodtrucks", foodtrucks);
            model.addAttribute("totalPedidos", totalPedidos);
            model.addAttribute("pedidosPendientes", pedidosPendientes);
            model.addAttribute("pedidosHoy", pedidosHoy);
            model.addAttribute("totalProductos", totalProductos);
            model.addAttribute("totalPromociones", totalPromociones);
            model.addAttribute("pedidosRecientes", pedidosRecientes);
            model.addAttribute("page", "dashboard");
            
            return "app/owner";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar dashboard: " + e.getMessage());
            model.addAttribute("page", "dashboard");
            return "app/owner";
        }
    }

    @GetMapping("/admin")
    public String appAdmin(Model model) {
        try {
            // Obtener estadísticas del sistema
            long totalUsuarios = usuarioRepository.count();
            long totalFoodtrucks = foodtruckRepository.count();
            long totalPedidos = pedidoRepository.count();
            
            // Contar usuarios activos
            List<Usuario> todosUsuarios = usuarioRepository.findAll();
            long usuariosActivos = todosUsuarios != null ? 
                todosUsuarios.stream()
                    .filter(Usuario::isActivo)
                    .count() : 0;
            
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("usuariosActivos", usuariosActivos);
            model.addAttribute("totalFoodtrucks", totalFoodtrucks);
            model.addAttribute("totalPedidos", totalPedidos);
            model.addAttribute("page", "dashboard");
            
            return "app/admin";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar dashboard: " + e.getMessage());
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("usuariosActivos", 0);
            model.addAttribute("totalFoodtrucks", 0);
            model.addAttribute("totalPedidos", 0);
            model.addAttribute("page", "dashboard");
            return "app/admin";
        }
    }
}

