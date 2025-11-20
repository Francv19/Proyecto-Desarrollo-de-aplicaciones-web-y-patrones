package com.urbanbites.controller;

import com.urbanbites.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistroController {
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro/nuevo")
    public String registrarCliente(
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam String contraseña,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.registrarCliente(nombre, "", correo, contraseña, null);
            redirectAttributes.addFlashAttribute("mensaje", "Registro exitoso. Puedes iniciar sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro";
        }
    }
}

