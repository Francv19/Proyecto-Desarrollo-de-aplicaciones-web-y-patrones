package com.urbanbites.controller;

import com.urbanbites.domain.Resena;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.ResenaRepository;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.ResenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/resenas")
public class ResenaController {
    @Autowired
    private ResenaService resenaService;
    
    @Autowired
    private ResenaRepository resenaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String verResenas(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByUsername(auth.getName());
        
        List<Resena> misResenas = resenaRepository.findByUsuarioIdUsuario(usuario.getIdUsuario());
        // Obtener todas las reseñas aprobadas (de otros usuarios)
        List<Resena> todasResenas = resenaRepository.findAll();
        List<Resena> resenasOtros = todasResenas.stream()
            .filter(r -> r.getEstado() == Resena.EstadoResena.aprobada && 
                        !r.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
            .limit(8)
            .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("misResenas", misResenas);
        model.addAttribute("resenasOtros", resenasOtros);
        
        return "resenas/index";
    }

    @PostMapping("/crear")
    public String crearResena(@RequestParam Integer idPedido,
                              @RequestParam Integer calificacion,
                              @RequestParam(required = false) String comentario,
                              RedirectAttributes redirectAttributes) {
        try {
            resenaService.crearResena(idPedido, calificacion, comentario);
            redirectAttributes.addFlashAttribute("mensaje", "Reseña creada exitosamente. Está pendiente de aprobación.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos";
    }
}

