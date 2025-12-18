package com.urbanbites.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/errores")
public class ErrorController {
    
    @GetMapping("/403")
    public String accesoDenegado() {
        return "errores/403";
    }
}

