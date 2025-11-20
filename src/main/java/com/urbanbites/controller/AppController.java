package com.urbanbites.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app")
public class AppController {

    @GetMapping("/cliente")
    public String appCliente() {
        return "app/cliente";
    }

    @GetMapping("/owner")
    public String appOwner() {
        return "app/owner";
    }

    @GetMapping("/admin")
    public String appAdmin() {
        return "app/admin";
    }
}

