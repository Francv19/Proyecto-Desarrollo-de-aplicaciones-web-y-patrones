package com.urbanbites.controller;

import com.urbanbites.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador temporal para crear el usuario administrador inicial.
 * IMPORTANTE: Eliminar o deshabilitar este controlador después de crear el admin.
 */
@Controller
public class AdminSetupController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @GetMapping("/setup/admin")
    public String mostrarFormulario() {
        return "setup/admin";
    }
    
    @PostMapping("/setup/admin")
    public String crearAdmin(
            @RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String correo,
            @RequestParam String password,
            @RequestParam(required = false) String telefono,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.registrarAdmin(nombre, apellidos, correo, password, telefono);
            redirectAttributes.addFlashAttribute("mensaje", "Administrador creado exitosamente. Username: " + correo);
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/setup/admin";
        }
    }
}

