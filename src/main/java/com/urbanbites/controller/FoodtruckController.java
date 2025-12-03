package com.urbanbites.controller;

import com.urbanbites.domain.Foodtruck;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.FoodtruckService;
import com.urbanbites.service.FirebaseStorageService;
import com.urbanbites.service.ReglaPuntosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/owner/foodtrucks")
public class FoodtruckController {
    
    @Autowired
    private FoodtruckService foodtruckService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private FirebaseStorageService firebaseStorageService;
    
    @Autowired
    private ReglaPuntosService reglaPuntosService;
    
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
            
            // Debug: verificar que las imágenes se están cargando
            if (foodtrucks != null) {
                for (Foodtruck ft : foodtrucks) {
                    System.out.println("Food Truck: " + ft.getNombre() + ", rutaImagen: " + ft.getRutaImagen());
                }
            }
            
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
                                 @RequestParam(required = false) MultipartFile imagen,
                                 RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            
            String rutaImagen = null;
            if (imagen != null && !imagen.isEmpty()) {
                try {
                    rutaImagen = firebaseStorageService.cargaImagen(imagen, "foodtrucks/" + usuario.getIdUsuario() + "/");
                    System.out.println("Imagen subida exitosamente. URL: " + rutaImagen);
                } catch (Exception e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
                    return "redirect:/owner/foodtrucks/nuevo";
                }
            }
            
            foodtruckService.crearFoodtruck(
                usuario.getIdUsuario(),
                nombre,
                descripcion,
                telefono,
                email,
                porcentajePuntos,
                activo,
                rutaImagen
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
        
        // Obtener información sobre reglas de puntos
        List<com.urbanbites.domain.ReglaPuntos> reglas = reglaPuntosService.obtenerReglasPorFoodtruck(id);
        long reglasActivas = reglas.stream().filter(r -> r.getActivo() != null && r.getActivo()).count();
        
        model.addAttribute("foodtruck", foodtruck);
        model.addAttribute("tieneReglas", !reglas.isEmpty());
        model.addAttribute("reglasActivas", reglasActivas);
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
                                      @RequestParam(required = false) MultipartFile imagen,
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
            
            String rutaImagen = foodtruck.getRutaImagen(); // Mantener la imagen actual si no se sube una nueva
            if (imagen != null && !imagen.isEmpty()) {
                try {
                    rutaImagen = firebaseStorageService.cargaImagen(imagen, "foodtrucks/" + usuario.getIdUsuario() + "/");
                    System.out.println("Imagen actualizada exitosamente. URL: " + rutaImagen);
                } catch (Exception e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
                    return "redirect:/owner/foodtrucks/" + id + "/editar";
                }
            }
            
            foodtruckService.actualizarFoodtruck(id, nombre, descripcion, telefono, email, porcentajePuntos, activo, rutaImagen);
            
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

