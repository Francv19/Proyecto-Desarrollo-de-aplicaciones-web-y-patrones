package com.urbanbites.controller;

import com.urbanbites.domain.Promocion;
import com.urbanbites.service.PromocionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PromocionController {
    @Autowired
    private PromocionService promocionService;

    @GetMapping("/promociones")
    public String verPromociones(Model model) {
        List<Promocion> todasPromociones = promocionService.obtenerPromocionesVigentes();
        
        // Promociones destacadas: las primeras 3
        List<Promocion> promocionesDestacadas = todasPromociones.stream()
                .limit(3)
                .collect(java.util.stream.Collectors.toList());
        
        // Cupones: todas las promociones vigentes
        model.addAttribute("promocionesDestacadas", promocionesDestacadas);
        model.addAttribute("cupones", todasPromociones);
        return "promociones/index";
    }
}

