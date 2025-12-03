package com.urbanbites.controller;

import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.service.PromocionService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/")
    public String landing(Model model) {
        // Obtener todos los food trucks activos para el carrusel
        List<com.urbanbites.domain.Foodtruck> foodtrucksDestacados = 
            foodtruckRepository.findByActivoTrue();
        
        // Obtener todos los food trucks activos
        List<com.urbanbites.domain.Foodtruck> todosFoodtrucks = 
            foodtruckRepository.findByActivoTrue();
        
        // Obtener promociones vigentes (las primeras 3)
        List<com.urbanbites.domain.Promocion> promocionesDestacadas = 
            promocionService.obtenerPromocionesVigentes().stream()
                .limit(3)
                .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("foodtrucksDestacados", foodtrucksDestacados);
        model.addAttribute("todosFoodtrucks", todosFoodtrucks);
        model.addAttribute("promocionesDestacadas", promocionesDestacadas);
        return "landing/index";
    }
}

