package com.urbanbites.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "pedidos")
public class Pedido implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_foodtruck")
    private Foodtruck foodtruck;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoPedido estado;

    @Column(name = "eta_minutos")
    private Integer etaMinutos;

    @Column(name = "total_bruto", precision = 10, scale = 2)
    private BigDecimal totalBruto;

    @Column(precision = 10, scale = 2)
    private BigDecimal descuento;

    @Column(name = "total_neto", precision = 10, scale = 2)
    private BigDecimal totalNeto;

    @Column(length = 300)
    private String notas;

    @Column(name = "fecha_creacion")
    private java.time.LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "pedido")
    private List<DetallePedido> detalles;

    public enum EstadoPedido {
        recibido, en_preparacion, listo, entregado, cancelado
    }
}

