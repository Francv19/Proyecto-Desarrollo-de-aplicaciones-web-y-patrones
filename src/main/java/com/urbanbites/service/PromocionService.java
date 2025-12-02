package com.urbanbites.service;

import com.urbanbites.domain.Promocion;
import com.urbanbites.repository.PromocionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PromocionService {
    @Autowired
    private PromocionRepository promocionRepository;
    
    @Autowired
    private com.urbanbites.repository.FoodtruckRepository foodtruckRepository;

    @Transactional(readOnly = true)
    public List<Promocion> obtenerPromocionesVigentes() {
        return promocionRepository.findPromocionesVigentes(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Promocion> obtenerPromocionesPorFoodtruck(Integer idFoodtruck) {
        return promocionRepository.findByFoodtruckIdFoodtruck(idFoodtruck);
    }
    
    public Promocion crearPromocion(Integer idFoodtruck, Promocion.TipoDescuento tipoDescuento,
                                   BigDecimal valor, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El valor debe ser mayor a 0");
        }
        
        if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
            throw new RuntimeException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        
        com.urbanbites.domain.Foodtruck foodtruck = foodtruckRepository.findById(idFoodtruck)
            .orElseThrow(() -> new RuntimeException("Food truck no encontrado"));
        
        Promocion promocion = new Promocion();
        promocion.setFoodtruck(foodtruck);
        promocion.setTipoDescuento(tipoDescuento);
        promocion.setValor(valor);
        promocion.setFechaInicio(fechaInicio);
        promocion.setFechaFin(fechaFin);
        promocion.setActivo(true);
        
        return promocionRepository.save(promocion);
    }
    
    public Promocion actualizarPromocion(Integer idPromocion, Promocion.TipoDescuento tipoDescuento,
                                        BigDecimal valor, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        Promocion promocion = promocionRepository.findById(idPromocion)
            .orElseThrow(() -> new RuntimeException("Promoción no encontrada"));
        
        if (valor != null && valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El valor debe ser mayor a 0");
        }
        
        if (fechaFin != null && fechaInicio != null) {
            if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
                throw new RuntimeException("La fecha de fin debe ser posterior a la fecha de inicio");
            }
        }
        
        if (tipoDescuento != null) promocion.setTipoDescuento(tipoDescuento);
        if (valor != null) promocion.setValor(valor);
        if (fechaInicio != null) promocion.setFechaInicio(fechaInicio);
        if (fechaFin != null) promocion.setFechaFin(fechaFin);
        
        return promocionRepository.save(promocion);
    }
    
    public void eliminarPromocion(Integer idPromocion) {
        promocionRepository.deleteById(idPromocion);
    }
    
    public void desactivarPromocion(Integer idPromocion) {
        Promocion promocion = promocionRepository.findById(idPromocion)
            .orElseThrow(() -> new RuntimeException("Promoción no encontrada"));
        promocion.setActivo(false);
        promocionRepository.save(promocion);
    }
    
    public Promocion obtenerPromocionPorId(Integer idPromocion) {
        return promocionRepository.findById(idPromocion).orElse(null);
    }
}

