package com.urbanbites.controller;

import com.urbanbites.domain.PuntosCliente;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.PuntosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/puntos")
public class PuntosController {
    @Autowired
    private PuntosService puntosService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/saldo")
    public String verSaldoPuntos(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByUsername(auth.getName());
        
        Integer saldo = puntosService.obtenerSaldoPuntos(usuario.getIdUsuario());
        List<PuntosCliente> movimientos = puntosService.obtenerMovimientosPuntos(usuario.getIdUsuario());
        
        model.addAttribute("saldo", saldo);
        model.addAttribute("movimientos", movimientos);
        
        return "puntos/saldo";
    }
}

