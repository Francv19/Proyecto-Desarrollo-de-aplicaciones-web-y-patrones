package com.urbanbites.controller;

import com.urbanbites.domain.Rol;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.EventoRepository;
import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.repository.PedidoRepository;
import com.urbanbites.repository.RolRepository;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.domain.Evento;
import com.urbanbites.domain.Foodtruck;
import com.urbanbites.service.FoodtruckService;
import com.urbanbites.service.FirebaseStorageService;
import org.springframework.web.multipart.MultipartFile;
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
    private EventoRepository eventoRepository;
    
    @Autowired
    private FoodtruckService foodtruckService;
    
    @Autowired
    private FirebaseStorageService firebaseStorageService;
    
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
        List<Foodtruck> foodtrucks = foodtruckRepository.findAllWithDueno();
        model.addAttribute("foodtrucks", foodtrucks);
        model.addAttribute("page", "foodtrucks");
        return "admin/foodtrucks/index";
    }
    
    @GetMapping("/food-trucks/nuevo")
    public String mostrarFormularioNuevoFoodtruck(Model model) {
        List<Usuario> duenos = usuarioRepository.findAll().stream()
            .filter(u -> u.getRoles() != null && u.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("dueno")))
            .collect(java.util.stream.Collectors.toList());
        model.addAttribute("duenos", duenos);
        model.addAttribute("foodtruck", new Foodtruck());
        model.addAttribute("page", "foodtrucks");
        return "admin/foodtrucks/form";
    }
    
    @PostMapping("/food-trucks/crear")
    public String crearFoodtruck(@RequestParam Integer idDueno,
                                 @RequestParam String nombre,
                                 @RequestParam(required = false) String descripcion,
                                 @RequestParam(required = false) String telefono,
                                 @RequestParam(required = false) String email,
                                 @RequestParam(required = false) Integer porcentajePuntos,
                                 @RequestParam(defaultValue = "true") Boolean activo,
                                 @RequestParam(required = false) MultipartFile imagen,
                                 RedirectAttributes redirectAttributes) {
        try {
            String rutaImagen = null;
            if (imagen != null && !imagen.isEmpty()) {
                try {
                    rutaImagen = firebaseStorageService.cargaImagen(imagen, "foodtrucks/admin/");
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
                    return "redirect:/admin/food-trucks/nuevo";
                }
            }
            
            foodtruckService.crearFoodtruck(
                idDueno,
                nombre,
                descripcion,
                telefono,
                email,
                porcentajePuntos,
                activo,
                rutaImagen
            );
            
            redirectAttributes.addFlashAttribute("mensaje", "Food truck creado exitosamente");
            return "redirect:/admin/food-trucks";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/food-trucks/nuevo";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear food truck: " + e.getMessage());
            return "redirect:/admin/food-trucks/nuevo";
        }
    }
    
    @GetMapping("/food-trucks/{id}/editar")
    public String mostrarFormularioEditarFoodtruck(@PathVariable Integer id, Model model) {
        Foodtruck foodtruck = foodtruckService.obtenerFoodtruckPorId(id);
        
        if (foodtruck == null) {
            return "redirect:/admin/food-trucks?error=Food truck no encontrado";
        }
        
        List<Usuario> duenos = usuarioRepository.findAll().stream()
            .filter(u -> u.getRoles() != null && u.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("dueno")))
            .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("foodtruck", foodtruck);
        model.addAttribute("duenos", duenos);
        model.addAttribute("page", "foodtrucks");
        
        return "admin/foodtrucks/form";
    }
    
    @PostMapping("/food-trucks/{id}/actualizar")
    public String actualizarFoodtruck(@PathVariable Integer id,
                                      @RequestParam Integer idDueno,
                                      @RequestParam String nombre,
                                      @RequestParam(required = false) String descripcion,
                                      @RequestParam(required = false) String telefono,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) Integer porcentajePuntos,
                                      @RequestParam(defaultValue = "true") Boolean activo,
                                      @RequestParam(required = false) MultipartFile imagen,
                                      RedirectAttributes redirectAttributes) {
        try {
            Foodtruck foodtruck = foodtruckService.obtenerFoodtruckPorId(id);
            
            if (foodtruck == null) {
                redirectAttributes.addFlashAttribute("error", "Food truck no encontrado");
                return "redirect:/admin/food-trucks";
            }
            
            String rutaImagen = foodtruck.getRutaImagen();
            if (imagen != null && !imagen.isEmpty()) {
                try {
                    rutaImagen = firebaseStorageService.cargaImagen(imagen, "foodtrucks/admin/");
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
                    return "redirect:/admin/food-trucks/" + id + "/editar";
                }
            }
            
            foodtruckService.actualizarFoodtruckConDueno(id, idDueno, nombre, descripcion, telefono, email, porcentajePuntos, activo, rutaImagen);
            
            redirectAttributes.addFlashAttribute("mensaje", "Food truck actualizado exitosamente");
            return "redirect:/admin/food-trucks";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/food-trucks/" + id + "/editar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar food truck: " + e.getMessage());
            return "redirect:/admin/food-trucks/" + id + "/editar";
        }
    }
    
    @PostMapping("/food-trucks/{id}/eliminar")
    public String eliminarFoodtruck(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Foodtruck foodtruck = foodtruckService.obtenerFoodtruckPorId(id);
            
            if (foodtruck == null) {
                redirectAttributes.addFlashAttribute("error", "Food truck no encontrado");
                return "redirect:/admin/food-trucks";
            }
            
            foodtruckService.eliminarFoodtruck(id);
            redirectAttributes.addFlashAttribute("mensaje", "Food truck eliminado exitosamente");
            return "redirect:/admin/food-trucks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar food truck: " + e.getMessage());
            return "redirect:/admin/food-trucks";
        }
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
                // Primero, actualizar los eventos que referencian a este usuario
                List<Evento> eventosRelacionados = eventoRepository.findEventosPorUsuario(id);
                for (Evento evento : eventosRelacionados) {
                    if (evento.getSolicitante() != null && evento.getSolicitante().getIdUsuario().equals(id)) {
                        evento.setSolicitante(null);
                    }
                    if (evento.getDuenoCotizador() != null && evento.getDuenoCotizador().getIdUsuario().equals(id)) {
                        evento.setDuenoCotizador(null);
                    }
                    eventoRepository.save(evento);
                }
                
                // Ahora sí podemos eliminar el usuario
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

