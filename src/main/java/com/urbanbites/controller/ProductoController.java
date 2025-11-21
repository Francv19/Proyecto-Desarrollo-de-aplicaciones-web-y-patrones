package com.urbanbites.controller;

import com.urbanbites.domain.Foodtruck;
import com.urbanbites.domain.Menu;
import com.urbanbites.domain.Producto;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.repository.MenuRepository;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.ProductoService;
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
@RequestMapping("/owner/productos")
public class ProductoController {
    
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private FoodtruckRepository foodtruckRepository;
    
    @Autowired
    private MenuRepository menuRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    private Foodtruck obtenerFoodtruckDelDueno() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByUsername(auth.getName());
        
        List<Foodtruck> foodtrucks = foodtruckRepository.findByDuenoIdUsuario(usuario.getIdUsuario());
        if (foodtrucks.isEmpty()) {
            throw new RuntimeException("No se encontró un food truck para este dueño");
        }
        return foodtrucks.get(0); // Tomar el primero, se puede mejorar para manejar múltiples
    }
    
    @GetMapping
    public String listarProductos(Model model, RedirectAttributes redirectAttributes) {
        try {
            Foodtruck foodtruck = obtenerFoodtruckDelDueno();
            List<Producto> productos = productoService.obtenerTodosProductosPorFoodtruck(foodtruck.getIdFoodtruck());
            
            model.addAttribute("productos", productos != null ? productos : new java.util.ArrayList<>());
            model.addAttribute("foodtruck", foodtruck);
            model.addAttribute("page", "productos");
            
            return "owner/productos/index";
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("No se encontró un food truck")) {
                redirectAttributes.addFlashAttribute("error", "Primero debes crear un food truck");
                return "redirect:/owner/foodtrucks";
            }
            model.addAttribute("error", e.getMessage() != null ? e.getMessage() : "Error desconocido");
            model.addAttribute("productos", new java.util.ArrayList<>());
            model.addAttribute("page", "productos");
            return "owner/productos/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar productos: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido"));
            model.addAttribute("productos", new java.util.ArrayList<>());
            model.addAttribute("page", "productos");
            return "owner/productos/index";
        }
    }
    
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, RedirectAttributes redirectAttributes) {
        try {
            Foodtruck foodtruck = obtenerFoodtruckDelDueno();
            List<Menu> menus = menuRepository.findByFoodtruckIdFoodtruckAndActivoTrue(foodtruck.getIdFoodtruck());
            
            if (menus.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No hay menús disponibles. Por favor, contacta al administrador.");
                return "redirect:/owner/productos";
            }
            
            model.addAttribute("producto", new Producto());
            model.addAttribute("menus", menus);
            model.addAttribute("foodtruck", foodtruck);
            model.addAttribute("page", "productos");
            
            return "owner/productos/form";
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("No se encontró un food truck")) {
                redirectAttributes.addFlashAttribute("error", "Primero debes crear un food truck");
                return "redirect:/owner/foodtrucks";
            }
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/productos";
        }
    }
    
    @PostMapping("/crear")
    public String crearProducto(@RequestParam Integer idMenu,
                                @RequestParam String nombre,
                                @RequestParam(required = false) String descripcion,
                                @RequestParam BigDecimal precio,
                                @RequestParam(defaultValue = "true") Boolean disponible,
                                RedirectAttributes redirectAttributes) {
        try {
            Foodtruck foodtruck = obtenerFoodtruckDelDueno();
            
            productoService.crearProducto(
                foodtruck.getIdFoodtruck(),
                idMenu,
                nombre,
                descripcion,
                precio,
                disponible
            );
            
            redirectAttributes.addFlashAttribute("mensaje", "Producto creado exitosamente");
            return "redirect:/owner/productos";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/productos/nuevo";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear producto: " + e.getMessage());
            return "redirect:/owner/productos/nuevo";
        }
    }
    
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        try {
            Foodtruck foodtruck = obtenerFoodtruckDelDueno();
            Producto producto = productoService.obtenerProductoPorId(id);
            
            if (producto == null) {
                return "redirect:/owner/productos?error=Producto no encontrado";
            }
                        if (!producto.getFoodtruck().getIdFoodtruck().equals(foodtruck.getIdFoodtruck())) {
                return "redirect:/owner/productos?error=No tienes permiso para editar este producto";
            }
            
            List<Menu> menus = menuRepository.findByFoodtruckIdFoodtruckAndActivoTrue(foodtruck.getIdFoodtruck());
            
            model.addAttribute("producto", producto);
            model.addAttribute("menus", menus);
            model.addAttribute("foodtruck", foodtruck);
            model.addAttribute("page", "productos");
            
            return "owner/productos/form";
        } catch (Exception e) {
            return "redirect:/owner/productos?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/{id}/actualizar")
    public String actualizarProducto(@PathVariable Integer id,
                                     @RequestParam Integer idMenu,
                                     @RequestParam String nombre,
                                     @RequestParam(required = false) String descripcion,
                                     @RequestParam BigDecimal precio,
                                     @RequestParam(defaultValue = "true") Boolean disponible,
                                     RedirectAttributes redirectAttributes) {
        try {
            Foodtruck foodtruck = obtenerFoodtruckDelDueno();
            Producto producto = productoService.obtenerProductoPorId(id);
            
            if (producto == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/owner/productos";
            }
            
            if (!producto.getFoodtruck().getIdFoodtruck().equals(foodtruck.getIdFoodtruck())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar este producto");
                return "redirect:/owner/productos";
            }
            
            productoService.actualizarProducto(id, idMenu, nombre, descripcion, precio, disponible);
            
            redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente");
            return "redirect:/owner/productos";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/productos/" + id + "/editar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar producto: " + e.getMessage());
            return "redirect:/owner/productos/" + id + "/editar";
        }
    }
    
    @PostMapping("/{id}/eliminar")
    public String eliminarProducto(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Foodtruck foodtruck = obtenerFoodtruckDelDueno();
            Producto producto = productoService.obtenerProductoPorId(id);
            
            if (producto == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/owner/productos";
            }
            
            // Verificar que el producto pertenece al food truck del dueño
            if (!producto.getFoodtruck().getIdFoodtruck().equals(foodtruck.getIdFoodtruck())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar este producto");
                return "redirect:/owner/productos";
            }
            
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
            return "redirect:/owner/productos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar producto: " + e.getMessage());
            return "redirect:/owner/productos";
        }
    }
}

