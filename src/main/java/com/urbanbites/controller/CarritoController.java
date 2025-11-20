package com.urbanbites.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @GetMapping
    public String verCarrito() {
        return "carrito/index";
    }
}

