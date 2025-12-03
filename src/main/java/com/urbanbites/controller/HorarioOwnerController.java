package com.urbanbites.controller;

import com.urbanbites.domain.HorarioFoodtruck;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import com.urbanbites.service.FoodtruckService;
import com.urbanbites.service.HorarioFoodtruckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/owner/horarios")
public class HorarioOwnerController {
    @Autowired
    private HorarioFoodtruckService horarioFoodtruckService;
    
    @Autowired
    private FoodtruckService foodtruckService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByUsername(auth.getName());
    }

    @GetMapping
    public String listarHorarios(@RequestParam(required = false) Integer idFoodtruck, Model model) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            if (usuario == null) {
                return "redirect:/login";
            }
            
            List<com.urbanbites.domain.Foodtruck> foodtrucks = foodtruckService.obtenerFoodtrucksPorDueno(usuario.getIdUsuario());
            
            if (foodtrucks == null || foodtrucks.isEmpty()) {
                model.addAttribute("error", "No tienes food trucks registrados. Crea uno primero.");
                return "owner/horarios/index";
            }
            
            if (idFoodtruck != null) {
                // Verificar que el food truck pertenece al dueño
                boolean perteneceAlDueno = foodtrucks.stream()
                    .anyMatch(ft -> ft.getIdFoodtruck().equals(idFoodtruck));
                
                if (!perteneceAlDueno) {
                    model.addAttribute("error", "No tienes permiso para ver los horarios de este food truck");
                    return "owner/horarios/index";
                }
                
                List<HorarioFoodtruck> horarios = horarioFoodtruckService.obtenerTodosHorariosPorFoodtruck(idFoodtruck);
                model.addAttribute("horarios", horarios);
                model.addAttribute("foodtruckId", idFoodtruck);
            }
            
            model.addAttribute("foodtrucks", foodtrucks);
            model.addAttribute("page", "horarios");
            
            return "owner/horarios/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar horarios: " + e.getMessage());
            return "owner/horarios/index";
        }
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(@RequestParam Integer idFoodtruck, Model model) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            if (usuario == null) {
                return "redirect:/login";
            }
            
            com.urbanbites.domain.Foodtruck foodtruck = foodtruckService.obtenerFoodtruckPorId(idFoodtruck);
            if (foodtruck == null || !foodtruck.getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                return "redirect:/owner/horarios";
            }
            
            model.addAttribute("foodtruck", foodtruck);
            model.addAttribute("horario", new HorarioFoodtruck());
            model.addAttribute("page", "horarios");
            
            return "owner/horarios/form";
        } catch (Exception e) {
            return "redirect:/owner/horarios";
        }
    }

    @PostMapping("/crear")
    public String crearHorario(@RequestParam Integer idFoodtruck,
                               @RequestParam Integer diaSemana,
                               @RequestParam(required = false) String direccion,
                               @RequestParam(required = false) BigDecimal latitud,
                               @RequestParam(required = false) BigDecimal longitud,
                               @RequestParam String horaApertura,
                               @RequestParam String horaCierre,
                               RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            if (usuario == null) {
                return "redirect:/login";
            }
            
            com.urbanbites.domain.Foodtruck foodtruck = foodtruckService.obtenerFoodtruckPorId(idFoodtruck);
            if (foodtruck == null || !foodtruck.getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para crear horarios en este food truck");
                return "redirect:/owner/horarios";
            }
            
            LocalTime apertura = LocalTime.parse(horaApertura);
            LocalTime cierre = LocalTime.parse(horaCierre);
            
            horarioFoodtruckService.crearHorario(idFoodtruck, diaSemana, direccion, 
                                                latitud, longitud, apertura, cierre);
            redirectAttributes.addFlashAttribute("mensaje", "Horario creado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/horarios?idFoodtruck=" + idFoodtruck;
    }

    @GetMapping("/{idHorario}/editar")
    public String mostrarFormularioEditar(@PathVariable Integer idHorario, Model model) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            if (usuario == null) {
                return "redirect:/login";
            }
            
            HorarioFoodtruck horario = horarioFoodtruckService.obtenerHorarioPorId(idHorario);
            if (horario == null) {
                return "redirect:/owner/horarios?error=Horario no encontrado";
            }
            
            // Verificar que el horario pertenece a un food truck del dueño
            if (!horario.getFoodtruck().getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                return "redirect:/owner/horarios?error=No tienes permiso para editar este horario";
            }
            
            model.addAttribute("horario", horario);
            model.addAttribute("foodtruck", horario.getFoodtruck());
            model.addAttribute("page", "horarios");
            
            return "owner/horarios/form";
        } catch (Exception e) {
            return "redirect:/owner/horarios?error=" + e.getMessage();
        }
    }

    @PostMapping("/{idHorario}/actualizar")
    public String actualizarHorario(@PathVariable Integer idHorario,
                                    @RequestParam Integer diaSemana,
                                    @RequestParam(required = false) String direccion,
                                    @RequestParam(required = false) BigDecimal latitud,
                                    @RequestParam(required = false) BigDecimal longitud,
                                    @RequestParam String horaApertura,
                                    @RequestParam String horaCierre,
                                    RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            if (usuario == null) {
                return "redirect:/login";
            }
            
            HorarioFoodtruck horario = horarioFoodtruckService.obtenerHorarioPorId(idHorario);
            if (horario == null) {
                redirectAttributes.addFlashAttribute("error", "Horario no encontrado");
                return "redirect:/owner/horarios";
            }
            
            // Verificar que el horario pertenece a un food truck del dueño
            if (!horario.getFoodtruck().getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar este horario");
                return "redirect:/owner/horarios";
            }
            
            LocalTime apertura = LocalTime.parse(horaApertura);
            LocalTime cierre = LocalTime.parse(horaCierre);
            
            horarioFoodtruckService.actualizarHorario(idHorario, diaSemana, direccion, 
                                                      latitud, longitud, apertura, cierre);
            redirectAttributes.addFlashAttribute("mensaje", "Horario actualizado exitosamente");
            return "redirect:/owner/horarios?idFoodtruck=" + horario.getFoodtruck().getIdFoodtruck();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/horarios/" + idHorario + "/editar";
        }
    }

    @PostMapping("/{idHorario}/eliminar")
    public String eliminarHorario(@PathVariable Integer idHorario,
                                  RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            if (usuario == null) {
                return "redirect:/login";
            }
            
            HorarioFoodtruck horario = horarioFoodtruckService.obtenerHorarioPorId(idHorario);
            if (horario == null) {
                redirectAttributes.addFlashAttribute("error", "Horario no encontrado");
                return "redirect:/owner/horarios";
            }
            
            // Verificar que el horario pertenece a un food truck del dueño
            if (!horario.getFoodtruck().getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar este horario");
                return "redirect:/owner/horarios";
            }
            
            Integer foodtruckId = horario.getFoodtruck().getIdFoodtruck();
            horarioFoodtruckService.eliminarHorario(idHorario);
            redirectAttributes.addFlashAttribute("mensaje", "Horario eliminado exitosamente");
            return "redirect:/owner/horarios?idFoodtruck=" + foodtruckId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/horarios";
        }
    }

    @PostMapping("/{idHorario}/desactivar")
    public String desactivarHorario(@PathVariable Integer idHorario,
                                    RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            if (usuario == null) {
                return "redirect:/login";
            }
            
            HorarioFoodtruck horario = horarioFoodtruckService.obtenerHorarioPorId(idHorario);
            if (horario == null) {
                redirectAttributes.addFlashAttribute("error", "Horario no encontrado");
                return "redirect:/owner/horarios";
            }
            
            // Verificar que el horario pertenece a un food truck del dueño
            if (!horario.getFoodtruck().getDueno().getIdUsuario().equals(usuario.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para desactivar este horario");
                return "redirect:/owner/horarios";
            }
            
            Integer foodtruckId = horario.getFoodtruck().getIdFoodtruck();
            horarioFoodtruckService.desactivarHorario(idHorario);
            redirectAttributes.addFlashAttribute("mensaje", "Horario desactivado exitosamente");
            return "redirect:/owner/horarios?idFoodtruck=" + foodtruckId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/horarios";
        }
    }
}

