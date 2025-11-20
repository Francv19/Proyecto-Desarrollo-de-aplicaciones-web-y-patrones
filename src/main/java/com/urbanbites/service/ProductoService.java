package com.urbanbites.service;

import com.urbanbites.domain.Producto;
import com.urbanbites.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> obtenerProductosDisponiblesPorFoodtruck(Integer idFoodtruck) {
        return productoRepository.findByFoodtruckIdFoodtruckAndDisponibleTrue(idFoodtruck);
    }

    public List<Producto> obtenerTodosProductosPorFoodtruck(Integer idFoodtruck) {
        return productoRepository.findByFoodtruckIdFoodtruck(idFoodtruck);
    }

    public List<Producto> obtenerTodosProductosDisponibles() {
        return productoRepository.findByDisponibleTrue();
    }
}

