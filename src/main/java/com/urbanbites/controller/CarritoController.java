package com.urbanbites.controller;

import com.urbanbites.domain.Carrito;
import com.urbanbites.domain.Pedido;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.CarritoService;
import com.urbanbites.service.PuntosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/carrito")
public class CarritoController {
    
    @Autowired
    private CarritoService carritoService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PuntosService puntosService;
    
    private Usuario obtenerUsuarioActual() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            String username = auth.getName();
            if (username == null || username.isEmpty()) {
                return null;
            }
            return usuarioRepository.findByUsername(username);
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping({"/", ""})
    public String verCarrito(Model model) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            if (usuario == null) {
                // Si no hay usuario, retornar carrito vacío
                model.addAttribute("carrito", new Carrito());
                model.addAttribute("total", BigDecimal.ZERO);
                model.addAttribute("page", "carrito");
                return "carrito/index";
            }
            
            Carrito carrito = carritoService.obtenerCarritoAbierto(usuario.getIdUsuario());
            if (carrito == null) {
                carrito = new Carrito();
                carrito.setDetalles(new java.util.ArrayList<>());
            }
            if (carrito.getDetalles() == null) {
                carrito.setDetalles(new java.util.ArrayList<>());
            }
            
            BigDecimal total = BigDecimal.ZERO;
            Integer idFoodtruck = null;
            if (carrito.getDetalles() != null && !carrito.getDetalles().isEmpty()) {
                for (var detalle : carrito.getDetalles()) {
                    if (detalle != null && detalle.getPrecioUnit() != null && detalle.getCantidad() != null) {
                        BigDecimal subtotal = detalle.getPrecioUnit().multiply(new BigDecimal(detalle.getCantidad()));
                        total = total.add(subtotal);
                    }
                    // Obtener el food truck del primer detalle
                    if (idFoodtruck == null && detalle != null && detalle.getProducto() != null && 
                        detalle.getProducto().getFoodtruck() != null) {
                        idFoodtruck = detalle.getProducto().getFoodtruck().getIdFoodtruck();
                    }
                }
            }
            
            // Obtener saldo de puntos del usuario
            Integer saldoPuntos = puntosService.obtenerSaldoPuntos(usuario.getIdUsuario());
            
            // Calcular puntos que se obtendrán con este pedido
            PuntosService.PuntosInfo puntosInfo = null;
            if (idFoodtruck != null && total.compareTo(BigDecimal.ZERO) > 0) {
                puntosInfo = puntosService.calcularPuntosAObtener(total, idFoodtruck);
            }
            
            model.addAttribute("carrito", carrito);
            model.addAttribute("total", total);
            model.addAttribute("saldoPuntos", saldoPuntos);
            model.addAttribute("puntosInfo", puntosInfo);
            model.addAttribute("page", "carrito");
            
            return "carrito/index";
        } catch (Exception e) {
            Carrito carritoVacio = new Carrito();
            carritoVacio.setDetalles(new java.util.ArrayList<>());
            model.addAttribute("carrito", carritoVacio);
            model.addAttribute("total", BigDecimal.ZERO);
            model.addAttribute("page", "carrito");
            model.addAttribute("error", "Error al cargar el carrito: " + e.getMessage());
            return "carrito/index";
        }
    }
    
    @PostMapping("/agregar")
    public String agregarProducto(@RequestParam Integer idProducto,
                                  @RequestParam(defaultValue = "1") Integer cantidad,
                                  RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            if (usuario == null) {
                redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para agregar productos al carrito");
                return "redirect:/login";
            }
            
            if (idProducto == null) {
                redirectAttributes.addFlashAttribute("error", "El ID del producto no puede estar vacío");
                return "redirect:/menu";
            }
            
            carritoService.agregarProducto(usuario.getIdUsuario(), idProducto, cantidad);
            redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito exitosamente");
            return "redirect:/menu";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/menu";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/menu";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al agregar producto al carrito: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido"));
            return "redirect:/menu";
        }
    }
    
    @PostMapping("/actualizar")
    public String actualizarCantidad(@RequestParam Integer idDetalle,
                                     @RequestParam Integer cantidad,
                                     RedirectAttributes redirectAttributes) {
        try {
            carritoService.actualizarCantidad(idDetalle, cantidad);
            redirectAttributes.addFlashAttribute("mensaje", "Cantidad actualizada");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/carrito";
    }
    
    @PostMapping("/eliminar")
    public String eliminarProducto(@RequestParam Integer idDetalle,
                                   RedirectAttributes redirectAttributes) {
        try {
            carritoService.eliminarProducto(idDetalle);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/carrito";
    }
    
    @PostMapping("/confirmar")
    public String confirmarPedido(@RequestParam(required = false) Integer puntosACanjear,
                                   RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            if (usuario == null) {
                redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para confirmar un pedido");
                return "redirect:/login";
            }
            
            // Validar que los puntos a canjear no sean negativos
            if (puntosACanjear != null && puntosACanjear < 0) {
                redirectAttributes.addFlashAttribute("error", "La cantidad de puntos a canjear no puede ser negativa");
                return "redirect:/carrito";
            }
            
            Pedido pedido = carritoService.confirmarPedido(usuario.getIdUsuario(), puntosACanjear);
            
            String mensaje = "Pedido confirmado exitosamente. Número de pedido: #" + pedido.getIdPedido();
            if (puntosACanjear != null && puntosACanjear > 0) {
                mensaje += ". Se canjearon " + puntosACanjear + " puntos (₡" + 
                    String.format("%.2f", pedido.getDescuento().doubleValue()) + " de descuento)";
            }
            
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
            return "redirect:/pedidos";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/carrito";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/carrito";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al confirmar pedido: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido"));
            return "redirect:/carrito";
        }
    }
    
    @PostMapping("/vaciar")
    public String vaciarCarrito(RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            carritoService.vaciarCarrito(usuario.getIdUsuario());
            redirectAttributes.addFlashAttribute("mensaje", "Carrito vaciado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/carrito";
    }
}
