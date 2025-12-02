package com.urbanbites.controller;

import com.urbanbites.domain.Foodtruck;
import com.urbanbites.domain.Producto;
import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MenuController {
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private FoodtruckRepository foodtruckRepository;

    @GetMapping("/menu")
    public String verMenu(@RequestParam(required = false) Integer foodtruck, Model model) {
        List<Foodtruck> foodtrucks = foodtruckRepository.findByActivoTrue();
        List<Producto> productos = productoService.obtenerTodosProductosDisponibles();
        
        model.addAttribute("foodtrucks", foodtrucks);
        model.addAttribute("productos", productos);
        model.addAttribute("selectedFoodtruck", foodtruck);
        
        return "menu/index";
    }

    @GetMapping("/menu/{idFoodtruck}")
    public String verMenuPorFoodtruck(@PathVariable Integer idFoodtruck, Model model) {
        return verMenu(idFoodtruck, model);
    }
}

