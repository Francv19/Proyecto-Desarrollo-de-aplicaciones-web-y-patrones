package com.urbanbites.service;

import com.urbanbites.domain.HorarioFoodtruck;
import com.urbanbites.repository.FoodtruckRepository;
import com.urbanbites.repository.HorarioFoodtruckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class HorarioFoodtruckService {
    @Autowired
    private HorarioFoodtruckRepository horarioFoodtruckRepository;
    
    @Autowired
    private FoodtruckRepository foodtruckRepository;

    @Transactional(readOnly = true)
    public List<HorarioFoodtruck> obtenerHorariosPorFoodtruck(Integer idFoodtruck) {
        return horarioFoodtruckRepository.findHorariosActivosPorFoodtruck(idFoodtruck);
    }
    
    @Transactional(readOnly = true)
    public List<HorarioFoodtruck> obtenerTodosHorariosPorFoodtruck(Integer idFoodtruck) {
        return horarioFoodtruckRepository.findByFoodtruckIdFoodtruck(idFoodtruck);
    }
    
    @Transactional(readOnly = true)
    public HorarioFoodtruck obtenerHorarioPorId(Integer idHorario) {
        return horarioFoodtruckRepository.findById(idHorario).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public Map<Integer, List<HorarioFoodtruck>> obtenerHorariosAgrupadosPorFoodtruck() {
        List<HorarioFoodtruck> horarios = horarioFoodtruckRepository.findAllHorariosActivos();
        return horarios.stream()
            .collect(Collectors.groupingBy(h -> h.getFoodtruck().getIdFoodtruck()));
    }
    
    @Transactional(readOnly = true)
    public List<HorarioFoodtruck> obtenerTodosHorariosActivos() {
        return horarioFoodtruckRepository.findAllHorariosActivos();
    }
    
    public HorarioFoodtruck crearHorario(Integer idFoodtruck, Integer diaSemana, String direccion,
                                         BigDecimal latitud, BigDecimal longitud,
                                         LocalTime horaApertura, LocalTime horaCierre) {
        if (diaSemana == null || diaSemana < 1 || diaSemana > 7) {
            throw new RuntimeException("El día de la semana debe estar entre 1 (Lunes) y 7 (Domingo)");
        }
        
        if (horaCierre.isBefore(horaApertura) || horaCierre.equals(horaApertura)) {
            throw new RuntimeException("La hora de cierre debe ser posterior a la hora de apertura");
        }
        
        com.urbanbites.domain.Foodtruck foodtruck = foodtruckRepository.findById(idFoodtruck)
            .orElseThrow(() -> new RuntimeException("Food truck no encontrado"));
        
        HorarioFoodtruck horario = new HorarioFoodtruck();
        horario.setFoodtruck(foodtruck);
        horario.setDiaSemana(diaSemana);
        horario.setDireccion(direccion);
        horario.setLatitud(latitud);
        horario.setLongitud(longitud);
        horario.setHoraApertura(horaApertura);
        horario.setHoraCierre(horaCierre);
        horario.setActivo(true);
        
        return horarioFoodtruckRepository.save(horario);
    }
    
    public HorarioFoodtruck actualizarHorario(Integer idHorario, Integer diaSemana, String direccion,
                                              BigDecimal latitud, BigDecimal longitud,
                                              LocalTime horaApertura, LocalTime horaCierre) {
        HorarioFoodtruck horario = horarioFoodtruckRepository.findById(idHorario)
            .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        
        if (diaSemana != null && (diaSemana < 1 || diaSemana > 7)) {
            throw new RuntimeException("El día de la semana debe estar entre 1 (Lunes) y 7 (Domingo)");
        }
        
        if (horaCierre != null && horaApertura != null) {
            if (horaCierre.isBefore(horaApertura) || horaCierre.equals(horaApertura)) {
                throw new RuntimeException("La hora de cierre debe ser posterior a la hora de apertura");
            }
        }
        
        if (diaSemana != null) horario.setDiaSemana(diaSemana);
        if (direccion != null) horario.setDireccion(direccion);
        if (latitud != null) horario.setLatitud(latitud);
        if (longitud != null) horario.setLongitud(longitud);
        if (horaApertura != null) horario.setHoraApertura(horaApertura);
        if (horaCierre != null) horario.setHoraCierre(horaCierre);
        
        return horarioFoodtruckRepository.save(horario);
    }
    
    public void eliminarHorario(Integer idHorario) {
        horarioFoodtruckRepository.deleteById(idHorario);
    }
    
    public void desactivarHorario(Integer idHorario) {
        HorarioFoodtruck horario = horarioFoodtruckRepository.findById(idHorario)
            .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        horario.setActivo(false);
        horarioFoodtruckRepository.save(horario);
    }
}

