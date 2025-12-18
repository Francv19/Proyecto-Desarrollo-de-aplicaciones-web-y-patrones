package com.urbanbites.controller;

import com.urbanbites.domain.Foodtruck;
import com.urbanbites.domain.Promocion;
import com.urbanbites.domain.Usuario;
import com.urbanbites.domain.Rol;
import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.PromocionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class LandingController {
    @Autowired
    private FoodtruckRepository foodtruckRepository;
    
    @Autowired
    private PromocionService promocionService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String landing(Model model) {
        List<Foodtruck> foodtrucksDestacados = 
            foodtruckRepository.findByActivoTrue();
        
        List<Foodtruck> todosFoodtrucks = 
            foodtruckRepository.findByActivoTrue();
        
        List<Promocion> promocionesDestacadas = 
            promocionService.obtenerPromocionesVigentes().stream()
                .limit(3)
                .collect(java.util.stream.Collectors.toList());
        
        // Obtener usuario actual si está autenticado
        Usuario usuarioActual = null;
        String rutaApp = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                usuarioActual = usuarioRepository.findByUsername(auth.getName());
                if (usuarioActual != null && usuarioActual.getRoles() != null) {
                    for (Rol rol : usuarioActual.getRoles()) {
                        if ("admin".equals(rol.getNombre())) {
                            rutaApp = "/app/admin";
                            break;
                        } else if ("dueno".equals(rol.getNombre())) {
                            rutaApp = "/app/owner";
                            break;
                        } else if ("cliente".equals(rol.getNombre()) && rutaApp == null) {
                            rutaApp = "/menu";
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Si hay error, simplemente no mostrar usuario
        }
        
        model.addAttribute("foodtrucksDestacados", foodtrucksDestacados);
        model.addAttribute("todosFoodtrucks", todosFoodtrucks);
        model.addAttribute("promocionesDestacadas", promocionesDestacadas);
        model.addAttribute("usuarioActual", usuarioActual);
        model.addAttribute("rutaApp", rutaApp != null ? rutaApp : "/menu");
        
        return "landing/index";
    }
}

