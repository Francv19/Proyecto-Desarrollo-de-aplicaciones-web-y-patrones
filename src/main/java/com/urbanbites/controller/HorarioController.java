package com.urbanbites.controller;

import com.urbanbites.service.HorarioFoodtruckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/horarios")
public class HorarioController {
    @Autowired
    private HorarioFoodtruckService horarioFoodtruckService;

    @GetMapping
    public String verHorarios(@RequestParam(required = false) Integer idFoodtruck, Model model) {
        if (idFoodtruck != null) {
            List<com.urbanbites.domain.HorarioFoodtruck> horarios = 
                horarioFoodtruckService.obtenerHorariosPorFoodtruck(idFoodtruck);
            model.addAttribute("horarios", horarios);
            model.addAttribute("foodtruckId", idFoodtruck);
        } else {
            Map<Integer, List<com.urbanbites.domain.HorarioFoodtruck>> horariosAgrupados = 
                horarioFoodtruckService.obtenerHorariosAgrupadosPorFoodtruck();
            model.addAttribute("horariosAgrupados", horariosAgrupados);
        }
        
        return "horarios/index";
    }
}

