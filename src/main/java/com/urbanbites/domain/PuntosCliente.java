package com.urbanbites.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Data
@Entity
@Table(name = "puntos_cliente")
public class PuntosCliente implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mov")
    private Integer idMov;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_foodtruck")
    private Foodtruck foodtruck;

    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    private TipoPunto tipo;

    private Integer puntos;

    @Column(length = 200)
    private String motivo;
    
    @Column(name = "fecha_creacion")
    private java.time.LocalDateTime fechaCreacion;

    public enum TipoPunto {
        acumulados, redimidos
    }
}

