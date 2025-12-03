package com.urbanbites.service;

import com.urbanbites.domain.Foodtruck;
import com.urbanbites.domain.Menu;
import com.urbanbites.domain.Producto;
import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.repository.MenuRepository;
import com.urbanbites.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private FoodtruckRepository foodtruckRepository;
    
    @Autowired
    private MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosDisponiblesPorFoodtruck(Integer idFoodtruck) {
        return productoRepository.findByFoodtruckIdFoodtruckAndDisponibleTrue(idFoodtruck);
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerTodosProductosPorFoodtruck(Integer idFoodtruck) {
        return productoRepository.findByFoodtruckIdFoodtruck(idFoodtruck);
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerTodosProductosDisponibles() {
        return productoRepository.findByDisponibleTrue();
    }
    
    @Transactional(readOnly = true)
    public Producto obtenerProductoPorId(Integer idProducto) {
        return productoRepository.findById(idProducto).orElse(null);
    }
    
    @Transactional
    public Producto crearProducto(Integer idFoodtruck, Integer idMenu, String nombre, 
                                  String descripcion, BigDecimal precio, Boolean disponible) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        
        Foodtruck foodtruck = foodtruckRepository.findById(idFoodtruck)
            .orElseThrow(() -> new RuntimeException("Food truck no encontrado"));
        
        Menu menu = menuRepository.findById(idMenu)
            .orElseThrow(() -> new RuntimeException("Menú no encontrado"));
        
        // Verificar que el menú pertenece al food truck
        if (!menu.getFoodtruck().getIdFoodtruck().equals(idFoodtruck)) {
            throw new IllegalArgumentException("El menú no pertenece al food truck especificado");
        }
        
        Producto producto = new Producto();
        producto.setFoodtruck(foodtruck);
        producto.setMenu(menu);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setDisponible(disponible != null ? disponible : true);
        
        return productoRepository.save(producto);
    }
    
    @Transactional
    public Producto actualizarProducto(Integer idProducto, Integer idFoodtruck, Integer idMenu, String nombre,
                                       String descripcion, BigDecimal precio, Boolean disponible) {
        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        if (precio != null && precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        
        // Si se cambia el food truck, actualizarlo
        if (idFoodtruck != null && !producto.getFoodtruck().getIdFoodtruck().equals(idFoodtruck)) {
            Foodtruck nuevoFoodtruck = foodtruckRepository.findById(idFoodtruck)
                .orElseThrow(() -> new RuntimeException("Food truck no encontrado"));
            producto.setFoodtruck(nuevoFoodtruck);
        }
        
        if (idMenu != null) {
            Menu menu = menuRepository.findById(idMenu)
                .orElseThrow(() -> new RuntimeException("Menú no encontrado"));
            
            // Verificar que el menú pertenece al food truck (actualizado o existente)
            Integer foodtruckId = idFoodtruck != null ? idFoodtruck : producto.getFoodtruck().getIdFoodtruck();
            if (!menu.getFoodtruck().getIdFoodtruck().equals(foodtruckId)) {
                throw new IllegalArgumentException("El menú no pertenece al food truck especificado");
            }
            producto.setMenu(menu);
        }
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            producto.setNombre(nombre);
        }
        if (descripcion != null) {
            producto.setDescripcion(descripcion);
        }
        if (precio != null) {
            producto.setPrecio(precio);
        }
        if (disponible != null) {
            producto.setDisponible(disponible);
        }
        
        return productoRepository.save(producto);
    }
    
    @Transactional
    public void eliminarProducto(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        productoRepository.delete(producto);
    }
    
    @Transactional
    public void desactivarProducto(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setDisponible(false);
        productoRepository.save(producto);
    }
}

