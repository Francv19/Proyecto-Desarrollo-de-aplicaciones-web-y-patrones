package com.urbanbites.service;

import com.urbanbites.domain.Promocion;
import com.urbanbites.repository.PromocionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PromocionService {
    @Autowired
    private PromocionRepository promocionRepository;

    public List<Promocion> obtenerPromocionesVigentes() {
        return promocionRepository.findPromocionesVigentes(LocalDateTime.now());
    }

    public List<Promocion> obtenerPromocionesPorFoodtruck(Integer idFoodtruck) {
        return promocionRepository.findByFoodtruckIdFoodtruck(idFoodtruck);
    }
}

