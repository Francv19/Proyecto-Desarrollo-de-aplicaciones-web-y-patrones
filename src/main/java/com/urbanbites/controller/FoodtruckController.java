package com.urbanbites.controller;

import com.urbanbites.domain.Foodtruck;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.FoodtruckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/owner/foodtrucks")
public class FoodtruckController {
    
    @Autowired
    private FoodtruckService foodtruckService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByUsername(auth.getName());
    }
    
    @GetMapping
    public String listarFoodtrucks(Model model) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            if (usuario == null) {
                return "redirect:/login";
            }
            
            List<Foodtruck> foodtrucks = foodtruckService.obtenerFoodtrucksPorDueno(usuario.getIdUsuario());
            
            model.addAttribute("foodtrucks", foodtrucks != null ? foodtrucks : new java.util.ArrayList<>());
            model.addAttribute("page", "foodtrucks");
            
            return "owner/foodtrucks/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar food trucks: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido"));
            model.addAttribute("foodtrucks", new java.util.ArrayList<>());
            model.addAttribute("page", "foodtrucks");
            return "owner/foodtrucks/index";
        }
    }
    
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("foodtruck", new Foodtruck());
        model.addAttribute("page", "foodtrucks");
        return "owner/foodtrucks/form";
    }
    
    @PostMapping("/crear")
    public String crearFoodtruck(@RequestParam String nombre,
                                 @RequestParam(required = false) String descripcion,
                                 @RequestParam(required = false) String telefono,
                                 @RequestParam(required = false) String email,
                                 @RequestParam(required = false) Integer porcentajePuntos,
                                 @RequestParam(defaultValue = "true") Boolean activo,
                                 RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            
            foodtruckService.crearFoodtruck(
                usuario.getIdUsuario(),
                nombre,
                descripcion,
                telefono,
                email,
                porcentajePuntos,
                activo
            );
            
            redirectAttributes.addFlashAttribute("mensaje", "Food truck creado exitosamente. Se ha creado un menú principal por defecto.");
            return "redirect:/owner/foodtrucks";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/foodtrucks/nuevo";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear food truck: " + e.getMessage());
            return "redirect:/owner/foodtrucks/nuevo";
        }
    }
    
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Foodtruck foodtruck = foodtruckService.obtenerFoodtruckPorId(id);
        
        if (foodtruck == null) {
            return "redirect:/owner/foodtrucks?error=Food truck no encontrado";
        }
        
        Usuario usuario = obtenerUsuarioActual();
        
        // Verificar que el food truck pertenece al dueño
        if (!foodtruck.getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
            return "redirect:/owner/foodtrucks?error=No tienes permiso para editar este food truck";
        }
        
        model.addAttribute("foodtruck", foodtruck);
        model.addAttribute("page", "foodtrucks");
        
        return "owner/foodtrucks/form";
    }
    
    @PostMapping("/{id}/actualizar")
    public String actualizarFoodtruck(@PathVariable Integer id,
                                      @RequestParam String nombre,
                                      @RequestParam(required = false) String descripcion,
                                      @RequestParam(required = false) String telefono,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) Integer porcentajePuntos,
                                      @RequestParam(defaultValue = "true") Boolean activo,
                                      RedirectAttributes redirectAttributes) {
        try {
            Foodtruck foodtruck = foodtruckService.obtenerFoodtruckPorId(id);
            
            if (foodtruck == null) {
                redirectAttributes.addFlashAttribute("error", "Food truck no encontrado");
                return "redirect:/owner/foodtrucks";
            }
            
            Usuario usuario = obtenerUsuarioActual();
            
            // Verificar que el food truck pertenece al dueño
            if (!foodtruck.getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar este food truck");
                return "redirect:/owner/foodtrucks";
            }
            
            foodtruckService.actualizarFoodtruck(id, nombre, descripcion, telefono, email, porcentajePuntos, activo);
            
            redirectAttributes.addFlashAttribute("mensaje", "Food truck actualizado exitosamente");
            return "redirect:/owner/foodtrucks";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/foodtrucks/" + id + "/editar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar food truck: " + e.getMessage());
            return "redirect:/owner/foodtrucks/" + id + "/editar";
        }
    }
    
    @PostMapping("/{id}/eliminar")
    public String eliminarFoodtruck(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Foodtruck foodtruck = foodtruckService.obtenerFoodtruckPorId(id);
            
            if (foodtruck == null) {
                redirectAttributes.addFlashAttribute("error", "Food truck no encontrado");
                return "redirect:/owner/foodtrucks";
            }
            
            Usuario usuario = obtenerUsuarioActual();
            
            // Verificar que el food truck pertenece al dueño
            if (!foodtruck.getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar este food truck");
                return "redirect:/owner/foodtrucks";
            }
            
            foodtruckService.eliminarFoodtruck(id);
            redirectAttributes.addFlashAttribute("mensaje", "Food truck eliminado exitosamente");
            return "redirect:/owner/foodtrucks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar food truck: " + e.getMessage());
            return "redirect:/owner/foodtrucks";
        }
    }
}

