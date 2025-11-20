package com.urbanbites.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Data
@Entity
@Table(name = "resenas")
public class Resena implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resena")
    private Integer idResena;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_foodtruck")
    private Foodtruck foodtruck;

    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @Column(columnDefinition = "TINYINT")
    private Integer calificacion;

    @Column(length = 500)
    private String comentario;

    @Enumerated(EnumType.STRING)
    private EstadoResena estado;

    @Column(name = "fecha_creacion")
    private java.time.LocalDateTime fechaCreacion;

    public enum EstadoResena {
        pendiente, aprobada, oculta
    }
}

