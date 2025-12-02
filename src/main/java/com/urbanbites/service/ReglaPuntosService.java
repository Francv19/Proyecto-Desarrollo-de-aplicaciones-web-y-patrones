package com.urbanbites.service;

import com.urbanbites.domain.Foodtruck;
import com.urbanbites.domain.ReglaPuntos;
import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.repository.ReglaPuntosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReglaPuntosService {
    @Autowired
    private ReglaPuntosRepository reglaPuntosRepository;
    
    @Autowired
    private FoodtruckRepository foodtruckRepository;

    public ReglaPuntos crearRegla(Integer idFoodtruck, Integer porcentaje, 
                                  LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (porcentaje == null || porcentaje < 0 || porcentaje > 100) {
            throw new RuntimeException("El porcentaje debe estar entre 0 y 100");
        }
        
        if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
            throw new RuntimeException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        
        Foodtruck foodtruck = foodtruckRepository.findById(idFoodtruck)
            .orElseThrow(() -> new RuntimeException("Food truck no encontrado"));
        
        ReglaPuntos regla = new ReglaPuntos();
        regla.setFoodtruck(foodtruck);
        regla.setPorcentaje(porcentaje);
        regla.setFechaInicio(fechaInicio);
        regla.setFechaFin(fechaFin);
        regla.setActivo(true);
        
        return reglaPuntosRepository.save(regla);
    }

    public ReglaPuntos actualizarRegla(Integer idRegla, Integer porcentaje, 
                                       LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        ReglaPuntos regla = reglaPuntosRepository.findById(idRegla)
            .orElseThrow(() -> new RuntimeException("Regla no encontrada"));
        
        if (porcentaje != null && (porcentaje < 0 || porcentaje > 100)) {
            throw new RuntimeException("El porcentaje debe estar entre 0 y 100");
        }
        
        if (fechaFin != null && fechaInicio != null) {
            if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
                throw new RuntimeException("La fecha de fin debe ser posterior a la fecha de inicio");
            }
        }
        
        if (porcentaje != null) regla.setPorcentaje(porcentaje);
        if (fechaInicio != null) regla.setFechaInicio(fechaInicio);
        if (fechaFin != null) regla.setFechaFin(fechaFin);
        
        return reglaPuntosRepository.save(regla);
    }

    public void eliminarRegla(Integer idRegla) {
        reglaPuntosRepository.deleteById(idRegla);
    }

    public void desactivarRegla(Integer idRegla) {
        ReglaPuntos regla = reglaPuntosRepository.findById(idRegla)
            .orElseThrow(() -> new RuntimeException("Regla no encontrada"));
        regla.setActivo(false);
        reglaPuntosRepository.save(regla);
    }

    public List<ReglaPuntos> obtenerReglasPorFoodtruck(Integer idFoodtruck) {
        return reglaPuntosRepository.findByFoodtruckIdFoodtruck(idFoodtruck);
    }

    public ReglaPuntos obtenerReglaVigente(Integer idFoodtruck) {
        List<ReglaPuntos> reglas = reglaPuntosRepository.findReglaVigentePorFoodtruck(
            idFoodtruck, LocalDateTime.now());
        return reglas.isEmpty() ? null : reglas.get(0);
    }
}

