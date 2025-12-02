package com.urbanbites.controller;

import com.urbanbites.domain.HorarioFoodtruck;
import com.urbanbites.service.FoodtruckService;
import com.urbanbites.service.HorarioFoodtruckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/admin/horarios")
public class HorarioAdminController {
    @Autowired
    private HorarioFoodtruckService horarioFoodtruckService;
    
    @Autowired
    private FoodtruckService foodtruckService;

    @GetMapping
    public String listarHorarios(@RequestParam(required = false) Integer idFoodtruck, Model model) {
        try {
            List<com.urbanbites.domain.Foodtruck> foodtrucks = foodtruckService.obtenerTodosFoodtrucks();
            
            if (idFoodtruck != null) {
                List<HorarioFoodtruck> horarios = horarioFoodtruckService.obtenerHorariosPorFoodtruck(idFoodtruck);
                model.addAttribute("horarios", horarios);
                model.addAttribute("foodtruckId", idFoodtruck);
            }
            
            model.addAttribute("foodtrucks", foodtrucks);
            model.addAttribute("page", "horarios");
            
            return "admin/horarios/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar horarios: " + e.getMessage());
            return "admin/horarios/index";
        }
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(@RequestParam Integer idFoodtruck, Model model) {
        com.urbanbites.domain.Foodtruck foodtruck = foodtruckService.obtenerFoodtruckPorId(idFoodtruck);
        if (foodtruck == null) {
            return "redirect:/admin/horarios";
        }
        
        model.addAttribute("foodtruck", foodtruck);
        model.addAttribute("horario", new HorarioFoodtruck());
        model.addAttribute("page", "horarios");
        
        return "admin/horarios/form";
    }

    @PostMapping("/crear")
    public String crearHorario(@RequestParam Integer idFoodtruck,
                               @RequestParam Integer diaSemana,
                               @RequestParam(required = false) String direccion,
                               @RequestParam(required = false) BigDecimal latitud,
                               @RequestParam(required = false) BigDecimal longitud,
                               @RequestParam String horaApertura,
                               @RequestParam String horaCierre,
                               RedirectAttributes redirectAttributes) {
        try {
            LocalTime apertura = LocalTime.parse(horaApertura);
            LocalTime cierre = LocalTime.parse(horaCierre);
            
            horarioFoodtruckService.crearHorario(idFoodtruck, diaSemana, direccion, 
                                                latitud, longitud, apertura, cierre);
            redirectAttributes.addFlashAttribute("mensaje", "Horario creado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/horarios?idFoodtruck=" + idFoodtruck;
    }

    @PostMapping("/{idHorario}/eliminar")
    public String eliminarHorario(@PathVariable Integer idHorario,
                                  @RequestParam(required = false) Integer idFoodtruck,
                                  RedirectAttributes redirectAttributes) {
        try {
            horarioFoodtruckService.eliminarHorario(idHorario);
            redirectAttributes.addFlashAttribute("mensaje", "Horario eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        String redirect = idFoodtruck != null ? "redirect:/admin/horarios?idFoodtruck=" + idFoodtruck : "redirect:/admin/horarios";
        return redirect;
    }

    @PostMapping("/{idHorario}/desactivar")
    public String desactivarHorario(@PathVariable Integer idHorario,
                                    @RequestParam(required = false) Integer idFoodtruck,
                                    RedirectAttributes redirectAttributes) {
        try {
            horarioFoodtruckService.desactivarHorario(idHorario);
            redirectAttributes.addFlashAttribute("mensaje", "Horario desactivado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        String redirect = idFoodtruck != null ? "redirect:/admin/horarios?idFoodtruck=" + idFoodtruck : "redirect:/admin/horarios";
        return redirect;
    }
}

