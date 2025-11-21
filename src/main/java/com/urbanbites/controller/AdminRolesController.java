package com.urbanbites.controller;

import com.urbanbites.domain.Rol;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.repository.PedidoRepository;
import com.urbanbites.repository.RolRepository;
import com.urbanbites.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminRolesController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private FoodtruckRepository foodtruckRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping
    public String adminDashboard(Model model) {
        long totalUsuarios = usuarioRepository.count();
        long totalFoodtrucks = foodtruckRepository.count();
        long totalPedidos = pedidoRepository.count();
        
        // Contar usuarios activos
        List<Usuario> todosUsuarios = usuarioRepository.findAll();
        long usuariosActivos = todosUsuarios.stream()
            .filter(Usuario::isActivo)
            .count();
        
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("usuariosActivos", usuariosActivos);
        model.addAttribute("totalFoodtrucks", totalFoodtrucks);
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("page", "dashboard");
        
        return "app/admin";
    }
    
    @GetMapping("/roles")
    public String gestionRolesDesdeSidebar(Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Rol> roles = rolRepository.findAll();
        
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", roles);
        model.addAttribute("page", "roles");
        
        return "admin/configuracion/roles";
    }
    
    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        model.addAttribute("page", "configuracion");
        return "admin/configuracion/index";
    }
    
    @GetMapping("/food-trucks")
    public String listarFoodtrucks(Model model) {
        List<com.urbanbites.domain.Foodtruck> foodtrucks = foodtruckRepository.findAll();
        model.addAttribute("foodtrucks", foodtrucks);
        model.addAttribute("page", "foodtrucks");
        return "admin/foodtrucks/index";
    }
    
    @GetMapping("/configuracion/usuarios/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        List<Rol> roles = rolRepository.findAll();
        model.addAttribute("roles", roles);
        return "admin/configuracion/usuario-form";
    }
    
    @PostMapping("/configuracion/usuarios/crear")
    public String crearUsuario(
            @RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String correo,
            @RequestParam String password,
            @RequestParam(required = false) String telefono,
            @RequestParam Integer idRol,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellidos(apellidos);
            usuario.setCorreo(correo);
            usuario.setUsername(correo);
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setTelefono(telefono);
            usuario.setActivo(true);
            
            Rol rol = rolRepository.findById(idRol).orElse(null);
            if (rol != null) {
                if (usuario.getRoles() == null) {
                    usuario.setRoles(new java.util.ArrayList<>());
                }
                usuario.getRoles().add(rol);
            }
            
            usuarioRepository.save(usuario);
            usuarioRepository.flush();
            
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado exitosamente");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/configuracion/usuarios/nuevo";
        }
    }
    
    @GetMapping("/configuracion/usuarios/{id}/editar")
    public String mostrarFormularioEditarUsuario(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        List<Rol> roles = rolRepository.findAll();
        
        if (usuario == null) {
            return "redirect:/admin";
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", roles);
        return "admin/configuracion/usuario-form";
    }
    
    @PostMapping("/configuracion/usuarios/{id}/actualizar")
    public String actualizarUsuario(
            @PathVariable Integer id,
            @RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String correo,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String telefono,
            @RequestParam Integer idRol,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioRepository.findById(id).orElse(null);
            if (usuario == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/admin";
            }
            
            usuario.setNombre(nombre);
            usuario.setApellidos(apellidos);
            usuario.setCorreo(correo);
            usuario.setUsername(correo);
            if (password != null && !password.isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(password));
            }
            usuario.setTelefono(telefono);
            
            // Actualizar rol
            Rol nuevoRol = rolRepository.findById(idRol).orElse(null);
            if (nuevoRol != null) {
                usuario.setRoles(new java.util.ArrayList<>());
                usuario.getRoles().add(nuevoRol);
            }
            
            usuarioRepository.save(usuario);
            usuarioRepository.flush();
            
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/configuracion/usuarios/" + id + "/editar";
        }
    }
    
    @PostMapping("/configuracion/usuarios/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioRepository.findById(id).orElse(null);
            if (usuario != null) {
                usuarioRepository.delete(usuario);
                redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar usuario: " + e.getMessage());
        }
        return "redirect:/admin";
    }
}

