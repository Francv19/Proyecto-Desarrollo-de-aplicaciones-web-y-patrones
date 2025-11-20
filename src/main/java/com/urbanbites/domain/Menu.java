package com.urbanbites.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "menu")
public class Menu implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_menu")
    private Integer idMenu;

    @ManyToOne
    @JoinColumn(name = "id_foodtruck")
    private Foodtruck foodtruck;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    private Integer orden;

    @Column(name = "activo")
    private Boolean activo;

    @OneToMany(mappedBy = "menu")
    private List<Producto> productos;
}

