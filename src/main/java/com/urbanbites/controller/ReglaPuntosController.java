package com.urbanbites.controller;

import com.urbanbites.domain.ReglaPuntos;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.FoodtruckService;
import com.urbanbites.service.ReglaPuntosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/owner/reglas-puntos")
public class ReglaPuntosController {
    @Autowired
    private ReglaPuntosService reglaPuntosService;
    
    @Autowired
    private FoodtruckService foodtruckService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String listarReglas(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            List<com.urbanbites.domain.Foodtruck> foodtrucks = foodtruckService.obtenerFoodtrucksPorDueno(usuario.getIdUsuario());
            
            if (foodtrucks == null || foodtrucks.isEmpty()) {
                model.addAttribute("error", "No tienes food trucks registrados. Crea uno primero.");
                return "owner/reglas-puntos/index";
            }
            
            // Obtener reglas para todos los food trucks del dueño
            java.util.Map<Integer, List<ReglaPuntos>> reglasPorFoodtruck = new java.util.HashMap<>();
            for (com.urbanbites.domain.Foodtruck ft : foodtrucks) {
                List<ReglaPuntos> reglas = reglaPuntosService.obtenerReglasPorFoodtruck(ft.getIdFoodtruck());
                reglasPorFoodtruck.put(ft.getIdFoodtruck(), reglas);
            }
            
            model.addAttribute("foodtrucks", foodtrucks);
            model.addAttribute("reglasPorFoodtruck", reglasPorFoodtruck);
            model.addAttribute("page", "reglas-puntos");
            
            return "owner/reglas-puntos/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar reglas: " + e.getMessage());
            return "owner/reglas-puntos/index";
        }
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(@RequestParam Integer idFoodtruck, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            com.urbanbites.domain.Foodtruck foodtruck = foodtruckService.obtenerFoodtruckPorId(idFoodtruck);
            if (foodtruck == null || !foodtruck.getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                return "redirect:/owner/reglas-puntos";
            }
            
            model.addAttribute("foodtruck", foodtruck);
            model.addAttribute("regla", new ReglaPuntos());
            model.addAttribute("page", "reglas-puntos");
            
            return "owner/reglas-puntos/form";
        } catch (Exception e) {
            return "redirect:/owner/reglas-puntos";
        }
    }

    @PostMapping("/crear")
    public String crearRegla(@RequestParam Integer idFoodtruck,
                            @RequestParam Integer porcentaje,
                            @RequestParam String fechaInicio,
                            @RequestParam String fechaFin,
                            RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            com.urbanbites.domain.Foodtruck foodtruck = foodtruckService.obtenerFoodtruckPorId(idFoodtruck);
            if (foodtruck == null || !foodtruck.getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para crear reglas en este food truck");
                return "redirect:/owner/reglas-puntos";
            }
            
            LocalDateTime fechaInicioDT = LocalDateTime.parse(fechaInicio);
            LocalDateTime fechaFinDT = LocalDateTime.parse(fechaFin);
            
            reglaPuntosService.crearRegla(idFoodtruck, porcentaje, fechaInicioDT, fechaFinDT);
            redirectAttributes.addFlashAttribute("mensaje", "Regla de puntos creada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/reglas-puntos";
    }

    @PostMapping("/{idRegla}/eliminar")
    public String eliminarRegla(@PathVariable Integer idRegla, RedirectAttributes redirectAttributes) {
        try {
            reglaPuntosService.eliminarRegla(idRegla);
            redirectAttributes.addFlashAttribute("mensaje", "Regla eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/reglas-puntos";
    }

    @PostMapping("/{idRegla}/desactivar")
    public String desactivarRegla(@PathVariable Integer idRegla, RedirectAttributes redirectAttributes) {
        try {
            reglaPuntosService.desactivarRegla(idRegla);
            redirectAttributes.addFlashAttribute("mensaje", "Regla desactivada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/reglas-puntos";
    }
}

