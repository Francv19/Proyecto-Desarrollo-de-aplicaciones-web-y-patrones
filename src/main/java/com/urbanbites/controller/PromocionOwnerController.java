package com.urbanbites.controller;

import com.urbanbites.domain.Promocion;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.FoodtruckService;
import com.urbanbites.service.PromocionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/owner/promociones")
public class PromocionOwnerController {
    @Autowired
    private PromocionService promocionService;
    
    @Autowired
    private FoodtruckService foodtruckService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String listarPromociones(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            List<com.urbanbites.domain.Foodtruck> foodtrucks = foodtruckService.obtenerFoodtrucksPorDueno(usuario.getIdUsuario());
            
            if (foodtrucks == null || foodtrucks.isEmpty()) {
                model.addAttribute("error", "No tienes food trucks registrados. Crea uno primero.");
                return "owner/promociones/index";
            }
            
            java.util.Map<Integer, List<Promocion>> promocionesPorFoodtruck = new java.util.HashMap<>();
            for (com.urbanbites.domain.Foodtruck ft : foodtrucks) {
                List<Promocion> promociones = promocionService.obtenerPromocionesPorFoodtruck(ft.getIdFoodtruck());
                promocionesPorFoodtruck.put(ft.getIdFoodtruck(), promociones);
            }
            
            model.addAttribute("foodtrucks", foodtrucks);
            model.addAttribute("promocionesPorFoodtruck", promocionesPorFoodtruck);
            model.addAttribute("page", "promociones");
            
            return "owner/promociones/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar promociones: " + e.getMessage());
            return "owner/promociones/index";
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
                return "redirect:/owner/promociones";
            }
            
            model.addAttribute("foodtruck", foodtruck);
            model.addAttribute("promocion", new Promocion());
            model.addAttribute("page", "promociones");
            
            return "owner/promociones/form";
        } catch (Exception e) {
            return "redirect:/owner/promociones";
        }
    }

    @PostMapping("/crear")
    public String crearPromocion(@RequestParam Integer idFoodtruck,
                                 @RequestParam String tipoDescuento,
                                 @RequestParam BigDecimal valor,
                                 @RequestParam(required = false) String descripcion,
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
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para crear promociones en este food truck");
                return "redirect:/owner/promociones";
            }
            
            Promocion.TipoDescuento tipo = Promocion.TipoDescuento.valueOf(tipoDescuento);
            LocalDateTime fechaInicioDT = LocalDateTime.parse(fechaInicio);
            LocalDateTime fechaFinDT = LocalDateTime.parse(fechaFin);
            
            promocionService.crearPromocion(idFoodtruck, tipo, valor, descripcion, fechaInicioDT, fechaFinDT);
            redirectAttributes.addFlashAttribute("mensaje", "Promoción creada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/promociones";
    }

    @PostMapping("/{idPromocion}/eliminar")
    public String eliminarPromocion(@PathVariable Integer idPromocion, RedirectAttributes redirectAttributes) {
        try {
            promocionService.eliminarPromocion(idPromocion);
            redirectAttributes.addFlashAttribute("mensaje", "Promoción eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/promociones";
    }

    @PostMapping("/{idPromocion}/desactivar")
    public String desactivarPromocion(@PathVariable Integer idPromocion, RedirectAttributes redirectAttributes) {
        try {
            promocionService.desactivarPromocion(idPromocion);
            redirectAttributes.addFlashAttribute("mensaje", "Promoción desactivada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/promociones";
    }
}

