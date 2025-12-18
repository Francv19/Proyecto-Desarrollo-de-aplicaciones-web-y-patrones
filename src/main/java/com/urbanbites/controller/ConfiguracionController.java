package com.urbanbites.controller;

import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ConfiguracionController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @GetMapping("/configuracion")
    public String mostrarConfiguracion(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            model.addAttribute("usuario", usuario);
            model.addAttribute("page", "configuracion");
            
            // Determinar qué vista mostrar según el rol
            boolean esCliente = usuario.getRoles() != null && 
                usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("cliente"));
            boolean esOwner = usuario.getRoles() != null && 
                usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("dueno"));
            boolean esAdmin = usuario.getRoles() != null && 
                usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("admin"));
            
            if (esCliente && !esOwner && !esAdmin) {
                return "configuracion/cliente";
            } else if (esOwner) {
                return "redirect:/configuracion/owner";
            } else if (esAdmin) {
                return "configuracion/owner";
            } else {
                return "configuracion/cliente";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar configuración: " + e.getMessage());
            return "redirect:/login";
        }
    }
    
    @GetMapping("/configuracion/owner")
    public String mostrarConfiguracionOwner(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                return "redirect:/login";
            }
            
            model.addAttribute("usuario", usuario);
            model.addAttribute("page", "configuracion");
            
            return "configuracion/owner";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar configuración: " + e.getMessage());
            return "redirect:/login";
        }
    }
    
    @PostMapping("/configuracion/actualizar")
    public String actualizarPerfil(
            @RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmPassword,
            RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            
            if (usuario == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/login";
            }
            
            // Determinar la ruta de redirección según el rol
            boolean esOwner = usuario.getRoles() != null && 
                usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("dueno"));
            String redirectUrl = esOwner ? "/configuracion/owner" : "/configuracion";
            
            // Validar contraseña si se proporciona
            if (password != null && !password.isEmpty()) {
                if (!password.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                    return "redirect:" + redirectUrl;
                }
                if (password.length() < 6) {
                    redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                    return "redirect:" + redirectUrl;
                }
            }
            
            // Verificar si el correo ya está en uso por otro usuario
            Usuario usuarioConMismoCorreo = usuarioRepository.findByCorreo(correo);
            if (usuarioConMismoCorreo != null && !usuarioConMismoCorreo.getIdUsuario().equals(usuario.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("error", "El correo ya está en uso por otro usuario");
                return "redirect:" + redirectUrl;
            }
            
            usuarioService.actualizarPerfil(usuario.getIdUsuario(), nombre, apellidos, correo, telefono, password);
            
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado exitosamente");
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioRepository.findByUsername(auth.getName());
            boolean esOwner = usuario != null && usuario.getRoles() != null && 
                usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("dueno"));
            String redirectUrl = esOwner ? "/configuracion/owner" : "/configuracion";
            redirectAttributes.addFlashAttribute("error", "Error al actualizar perfil: " + e.getMessage());
            return "redirect:" + redirectUrl;
        }
    }
}

