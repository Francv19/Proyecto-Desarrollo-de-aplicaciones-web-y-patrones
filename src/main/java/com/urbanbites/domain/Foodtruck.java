package com.urbanbites.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "foodtrucks")
public class Foodtruck implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foodtruck")
    private Integer idFoodtruck;

    @ManyToOne
    @JoinColumn(name = "id_dueno")
    private Usuario dueno;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(length = 25)
    private String telefono;

    @Column(length = 120)
    private String email;

    @Column(name = "porcentaje_puntos", columnDefinition = "TINYINT UNSIGNED")
    private Integer porcentajePuntos;

    @Column(name = "ruta_imagen", columnDefinition = "TEXT")
    private String rutaImagen;

    @Column(name = "activo")
    private Boolean activo;

    @OneToMany(mappedBy = "foodtruck")
    private List<Menu> menus;

    @OneToMany(mappedBy = "foodtruck")
    private List<Producto> productos;
}

