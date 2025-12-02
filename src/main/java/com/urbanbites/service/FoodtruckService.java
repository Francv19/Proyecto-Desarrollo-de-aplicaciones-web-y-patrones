package com.urbanbites.service;

import com.urbanbites.domain.Foodtruck;
import com.urbanbites.domain.Menu;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.repository.MenuRepository;
import com.urbanbites.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FoodtruckService {
    @Autowired
    private FoodtruckRepository foodtruckRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private MenuRepository menuRepository;
    
    @Transactional(readOnly = true)
    public List<Foodtruck> obtenerFoodtrucksPorDueno(Integer idDueno) {
        return foodtruckRepository.findByDuenoIdUsuario(idDueno);
    }
    
    @Transactional(readOnly = true)
    public Foodtruck obtenerFoodtruckPorId(Integer idFoodtruck) {
        return foodtruckRepository.findById(idFoodtruck).orElse(null);
    }
    
    @Transactional
    public Foodtruck crearFoodtruck(Integer idDueno, String nombre, String descripcion,
                                    String telefono, String email, Integer porcentajePuntos,
                                    Boolean activo, String rutaImagen) {
        Usuario dueno = usuarioRepository.findById(idDueno)
            .orElseThrow(() -> new RuntimeException("Dueño no encontrado"));
        
        if (porcentajePuntos != null && (porcentajePuntos < 0 || porcentajePuntos > 100)) {
            throw new IllegalArgumentException("El porcentaje de puntos debe estar entre 0 y 100");
        }
        
        Foodtruck foodtruck = new Foodtruck();
        foodtruck.setDueno(dueno);
        foodtruck.setNombre(nombre);
        foodtruck.setDescripcion(descripcion);
        foodtruck.setTelefono(telefono);
        foodtruck.setEmail(email);
        foodtruck.setPorcentajePuntos(porcentajePuntos != null ? porcentajePuntos : 0);
        foodtruck.setRutaImagen(rutaImagen);
        foodtruck.setActivo(activo != null ? activo : true);
        
        foodtruck = foodtruckRepository.save(foodtruck);
        
        Menu menuDefault = new Menu();
        menuDefault.setFoodtruck(foodtruck);
        menuDefault.setNombre("Menú Principal");
        menuDefault.setDescripcion("Menú principal del food truck");
        menuDefault.setOrden(1);
        menuDefault.setActivo(true);
        menuRepository.save(menuDefault);
        
        return foodtruck;
    }
    
    @Transactional
    public Foodtruck actualizarFoodtruck(Integer idFoodtruck, String nombre, String descripcion,
                                        String telefono, String email, Integer porcentajePuntos,
                                        Boolean activo, String rutaImagen) {
        Foodtruck foodtruck = foodtruckRepository.findById(idFoodtruck)
            .orElseThrow(() -> new RuntimeException("Food truck no encontrado"));
        
        if (porcentajePuntos != null && (porcentajePuntos < 0 || porcentajePuntos > 100)) {
            throw new IllegalArgumentException("El porcentaje de puntos debe estar entre 0 y 100");
        }
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            foodtruck.setNombre(nombre);
        }
        if (descripcion != null) {
            foodtruck.setDescripcion(descripcion);
        }
        if (telefono != null) {
            foodtruck.setTelefono(telefono);
        }
        if (email != null) {
            foodtruck.setEmail(email);
        }
        if (porcentajePuntos != null) {
            foodtruck.setPorcentajePuntos(porcentajePuntos);
        }
        if (rutaImagen != null && !rutaImagen.trim().isEmpty()) {
            foodtruck.setRutaImagen(rutaImagen);
        }
        if (activo != null) {
            foodtruck.setActivo(activo);
        }
        
        return foodtruckRepository.save(foodtruck);
    }
    
    @Transactional
    public Foodtruck actualizarFoodtruckConDueno(Integer idFoodtruck, Integer idDueno, String nombre, String descripcion,
                                                String telefono, String email, Integer porcentajePuntos,
                                                Boolean activo, String rutaImagen) {
        Foodtruck foodtruck = foodtruckRepository.findById(idFoodtruck)
            .orElseThrow(() -> new RuntimeException("Food truck no encontrado"));
        
        if (idDueno != null) {
            Usuario nuevoDueno = usuarioRepository.findById(idDueno)
                .orElseThrow(() -> new RuntimeException("Dueño no encontrado"));
            foodtruck.setDueno(nuevoDueno);
        }
        
        return actualizarFoodtruck(idFoodtruck, nombre, descripcion, telefono, email, porcentajePuntos, activo, rutaImagen);
    }
    
    @Transactional
    public void eliminarFoodtruck(Integer idFoodtruck) {
        Foodtruck foodtruck = foodtruckRepository.findById(idFoodtruck)
            .orElseThrow(() -> new RuntimeException("Food truck no encontrado"));
        foodtruckRepository.delete(foodtruck);
    }
    
    @Transactional
    public void desactivarFoodtruck(Integer idFoodtruck) {
        Foodtruck foodtruck = foodtruckRepository.findById(idFoodtruck)
            .orElseThrow(() -> new RuntimeException("Food truck no encontrado"));
        foodtruck.setActivo(false);
        foodtruckRepository.save(foodtruck);
    }
    
    @Transactional(readOnly = true)
    public List<Foodtruck> obtenerTodosFoodtrucks() {
        return foodtruckRepository.findAll();
    }
}

