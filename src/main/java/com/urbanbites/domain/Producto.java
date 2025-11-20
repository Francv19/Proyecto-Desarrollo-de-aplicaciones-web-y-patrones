package com.urbanbites.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "productos")
public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @ManyToOne
    @JoinColumn(name = "id_foodtruck")
    private Foodtruck foodtruck;

    @ManyToOne
    @JoinColumn(name = "id_menu")
    private Menu menu;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 400)
    private String descripcion;

    @Column(precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "disponible")
    private Boolean disponible;

    @OneToMany(mappedBy = "producto")
    private List<FotoProducto> fotos;
}

