package com.urbanbites.service;

import com.urbanbites.domain.Ruta;
import com.urbanbites.domain.Rol;
import com.urbanbites.repository.RutaRepository;
import com.urbanbites.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RutaService {
    @Autowired
    private RutaRepository rutaRepository;
    
    @Autowired
    private RolRepository rolRepository;

    @Transactional(readOnly = true)
    public List<String> obtenerRutasPublicas() {
        List<Ruta> rutas = rutaRepository.findRutasPublicas();
        return rutas.stream()
            .map(Ruta::getRuta)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> obtenerRutasPorRol(String nombreRol) {
        Rol rol = rolRepository.findByNombre(nombreRol);
        if (rol == null) {
            return new ArrayList<>();
        }
        List<Ruta> rutas = rutaRepository.findRutasPorRol(rol.getIdRol());
        return rutas.stream()
            .map(Ruta::getRuta)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> obtenerRutasAutenticadas() {
        List<Ruta> todasLasRutas = rutaRepository.findAllRutas();
        
        return todasLasRutas.stream()
            .filter(r -> r.getRequiereRol() && r.getRol() == null)
            .map(Ruta::getRuta)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Ruta> obtenerTodasLasRutas() {
        return rutaRepository.findAllRutas();
    }

    @Transactional(readOnly = true)
    public List<Ruta> getRutas() {
        return rutaRepository.findAllRutas();
    }
}

