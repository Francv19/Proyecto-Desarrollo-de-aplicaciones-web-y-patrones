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
    
    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByUsername(auth.getName());
    }
    
    private List<Foodtruck> obtenerFoodtrucksDelDueno() {
        Usuario usuario = obtenerUsuarioActual();
        List<Foodtruck> foodtrucks = foodtruckRepository.findByDuenoIdUsuario(usuario.getIdUsuario());
        if (foodtrucks.isEmpty()) {
            throw new RuntimeException("No se encontró un food truck para este dueño");
        }
        return foodtrucks;
    }
    
    @GetMapping
    public String listarProductos(@RequestParam(required = false) Integer foodtruckId, Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Foodtruck> foodtrucks = obtenerFoodtrucksDelDueno();
            List<Producto> productos = new java.util.ArrayList<>();
            Foodtruck foodtruckSeleccionado = null;
            
            if (foodtruckId != null) {
                foodtruckSeleccionado = foodtrucks.stream()
                    .filter(ft -> ft.getIdFoodtruck().equals(foodtruckId))
                    .findFirst()
                    .orElse(null);
                
                if (foodtruckSeleccionado != null) {
                    productos = productoService.obtenerTodosProductosPorFoodtruck(foodtruckSeleccionado.getIdFoodtruck());
                }
            } else if (!foodtrucks.isEmpty()) {
                // Si no hay selección, usar el primero por defecto
                foodtruckSeleccionado = foodtrucks.get(0);
                productos = productoService.obtenerTodosProductosPorFoodtruck(foodtruckSeleccionado.getIdFoodtruck());
            }
            
            model.addAttribute("productos", productos != null ? productos : new java.util.ArrayList<>());
            model.addAttribute("foodtrucks", foodtrucks);
            model.addAttribute("foodtruckSeleccionado", foodtruckSeleccionado);
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
    public String mostrarFormularioNuevo(@RequestParam(required = false) Integer foodtruckId, Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Foodtruck> foodtrucks = obtenerFoodtrucksDelDueno();
            Foodtruck foodtruckSeleccionado = null;
            Menu menuPrincipal = null;
            java.util.Map<Integer, Integer> menusPorFoodtruck = new java.util.HashMap<>();
            
            if (foodtruckId != null) {
                foodtruckSeleccionado = foodtrucks.stream()
                    .filter(ft -> ft.getIdFoodtruck().equals(foodtruckId))
                    .findFirst()
                    .orElse(null);
            }
            
            if (foodtruckSeleccionado == null && !foodtrucks.isEmpty()) {
                foodtruckSeleccionado = foodtrucks.get(0);
            }
            
            for (Foodtruck ft : foodtrucks) {
                List<Menu> menus = menuRepository.findByFoodtruckIdFoodtruckAndActivoTrue(ft.getIdFoodtruck());
                if (!menus.isEmpty()) {
                    Menu menu = menus.stream()
                        .filter(m -> "Menú Principal".equalsIgnoreCase(m.getNombre()))
                        .findFirst()
                        .orElse(menus.get(0));
                    menusPorFoodtruck.put(ft.getIdFoodtruck(), menu.getIdMenu());
                    
                    if (foodtruckSeleccionado != null && ft.getIdFoodtruck().equals(foodtruckSeleccionado.getIdFoodtruck())) {
                        menuPrincipal = menu;
                    }
                }
            }
            
            model.addAttribute("producto", new Producto());
            model.addAttribute("foodtrucks", foodtrucks);
            model.addAttribute("foodtruckSeleccionado", foodtruckSeleccionado);
            model.addAttribute("menuPrincipal", menuPrincipal);
            model.addAttribute("menusPorFoodtruck", menusPorFoodtruck);
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
    public String crearProducto(@RequestParam Integer idFoodtruck,
                                @RequestParam(required = false) Integer idMenu,
                                @RequestParam String nombre,
                                @RequestParam(required = false) String descripcion,
                                @RequestParam BigDecimal precio,
                                @RequestParam(defaultValue = "true") Boolean disponible,
                                RedirectAttributes redirectAttributes) {
        try {
            // Validar que el food truck pertenece al dueño
            List<Foodtruck> foodtrucks = obtenerFoodtrucksDelDueno();
            boolean perteneceAlDueno = foodtrucks.stream()
                .anyMatch(ft -> ft.getIdFoodtruck().equals(idFoodtruck));
            
            if (!perteneceAlDueno) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para crear productos en este food truck");
                return "redirect:/owner/productos/nuevo?foodtruckId=" + idFoodtruck;
            }
            
            // Si no se proporciona idMenu, obtener el menú principal del food truck
            if (idMenu == null) {
                List<Menu> menus = menuRepository.findByFoodtruckIdFoodtruckAndActivoTrue(idFoodtruck);
                if (menus.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "No hay menús disponibles para este food truck");
                    return "redirect:/owner/productos/nuevo?foodtruckId=" + idFoodtruck;
                }
                // Buscar "Menú Principal" o tomar el primero
                Menu menuPrincipal = menus.stream()
                    .filter(m -> "Menú Principal".equalsIgnoreCase(m.getNombre()))
                    .findFirst()
                    .orElse(menus.get(0));
                idMenu = menuPrincipal.getIdMenu();
            }
            
            productoService.crearProducto(
                idFoodtruck,
                idMenu,
                nombre,
                descripcion,
                precio,
                disponible
            );
            
            redirectAttributes.addFlashAttribute("mensaje", "Producto creado exitosamente");
            return "redirect:/owner/productos?foodtruckId=" + idFoodtruck;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/productos/nuevo?foodtruckId=" + idFoodtruck;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear producto: " + e.getMessage());
            return "redirect:/owner/productos/nuevo?foodtruckId=" + idFoodtruck;
        }
    }
    
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        try {
            List<Foodtruck> foodtrucks = obtenerFoodtrucksDelDueno();
            Producto producto = productoService.obtenerProductoPorId(id);
            
            if (producto == null) {
                return "redirect:/owner/productos?error=Producto no encontrado";
            }
            
            boolean perteneceAlDueno = foodtrucks.stream()
                .anyMatch(ft -> ft.getIdFoodtruck().equals(producto.getFoodtruck().getIdFoodtruck()));
            
            if (!perteneceAlDueno) {
                return "redirect:/owner/productos?error=No tienes permiso para editar este producto";
            }
            
            Foodtruck foodtruckProducto = producto.getFoodtruck();
            Menu menuPrincipal = null;
            java.util.Map<Integer, Integer> menusPorFoodtruck = new java.util.HashMap<>();
            
            for (Foodtruck ft : foodtrucks) {
                List<Menu> menus = menuRepository.findByFoodtruckIdFoodtruckAndActivoTrue(ft.getIdFoodtruck());
                if (!menus.isEmpty()) {
                    Menu menu = menus.stream()
                        .filter(m -> "Menú Principal".equalsIgnoreCase(m.getNombre()))
                        .findFirst()
                        .orElse(menus.get(0));
                    menusPorFoodtruck.put(ft.getIdFoodtruck(), menu.getIdMenu());
                    
                    if (ft.getIdFoodtruck().equals(foodtruckProducto.getIdFoodtruck())) {
                        menuPrincipal = menu;
                    }
                }
            }
            
            model.addAttribute("producto", producto);
            model.addAttribute("foodtrucks", foodtrucks);
            model.addAttribute("foodtruckSeleccionado", foodtruckProducto);
            model.addAttribute("menuPrincipal", menuPrincipal);
            model.addAttribute("menusPorFoodtruck", menusPorFoodtruck);
            model.addAttribute("page", "productos");
            
            return "owner/productos/form";
        } catch (Exception e) {
            return "redirect:/owner/productos?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/{id}/actualizar")
    public String actualizarProducto(@PathVariable Integer id,
                                     @RequestParam Integer idFoodtruck,
                                     @RequestParam(required = false) Integer idMenu,
                                     @RequestParam String nombre,
                                     @RequestParam(required = false) String descripcion,
                                     @RequestParam BigDecimal precio,
                                     @RequestParam(defaultValue = "true") Boolean disponible,
                                     RedirectAttributes redirectAttributes) {
        try {
            List<Foodtruck> foodtrucks = obtenerFoodtrucksDelDueno();
            Producto producto = productoService.obtenerProductoPorId(id);
            
            if (producto == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/owner/productos";
            }
            
            boolean perteneceAlDueno = foodtrucks.stream()
                .anyMatch(ft -> ft.getIdFoodtruck().equals(idFoodtruck));
            
            if (!perteneceAlDueno) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar este producto");
                return "redirect:/owner/productos";
            }
            
            if (idMenu == null) {
                List<Menu> menus = menuRepository.findByFoodtruckIdFoodtruckAndActivoTrue(idFoodtruck);
                if (menus.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "No hay menús disponibles para este food truck");
                    return "redirect:/owner/productos/" + id + "/editar";
                }
                Menu menuPrincipal = menus.stream()
                    .filter(m -> "Menú Principal".equalsIgnoreCase(m.getNombre()))
                    .findFirst()
                    .orElse(menus.get(0));
                idMenu = menuPrincipal.getIdMenu();
            }
            
            productoService.actualizarProducto(id, idFoodtruck, idMenu, nombre, descripcion, precio, disponible);
            
            redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente");
            return "redirect:/owner/productos?foodtruckId=" + idFoodtruck;
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
            List<Foodtruck> foodtrucks = obtenerFoodtrucksDelDueno();
            Producto producto = productoService.obtenerProductoPorId(id);
            
            if (producto == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/owner/productos";
            }
            
            boolean perteneceAlDueno = foodtrucks.stream()
                .anyMatch(ft -> ft.getIdFoodtruck().equals(producto.getFoodtruck().getIdFoodtruck()));
            
            if (!perteneceAlDueno) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar este producto");
                return "redirect:/owner/productos";
            }
            
            Integer foodtruckId = producto.getFoodtruck().getIdFoodtruck();
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
            return "redirect:/owner/productos?foodtruckId=" + foodtruckId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar producto: " + e.getMessage());
            return "redirect:/owner/productos";
        }
    }
}

