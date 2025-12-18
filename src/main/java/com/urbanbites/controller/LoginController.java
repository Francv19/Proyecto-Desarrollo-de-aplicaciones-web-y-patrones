package com.urbanbites.controller;

import com.urbanbites.domain.Usuario;
import com.urbanbites.domain.Rol;
import com.urbanbites.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    
    private final UsuarioRepository usuarioRepository;
    
    public LoginController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    
    @GetMapping("/login/success")
    public String redirectAfterLogin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Usuario usuario = usuarioRepository.findByUsername(auth.getName());
        
        if (usuario == null || usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            return "redirect:/";
        }
        
        for (Rol rol : usuario.getRoles()) {
            String nombreRol = rol.getNombre();
            
            if ("admin".equals(nombreRol)) {
                return "redirect:/app/admin";
            } else if ("dueno".equals(nombreRol)) {
                return "redirect:/app/owner";
            } else if ("cliente".equals(nombreRol)) {
                return "redirect:/menu";
            }
        }
        
        return "redirect:/";
    }
}

