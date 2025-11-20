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
        List<Promocion> promociones = promocionService.obtenerPromocionesVigentes();
        model.addAttribute("promocionesDestacadas", promociones);
        model.addAttribute("cupones", promociones);
        return "promociones/index";
    }
}

