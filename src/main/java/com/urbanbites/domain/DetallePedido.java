package com.urbanbites.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "detalle_pedido")
public class DetallePedido implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(name = "nombre_producto", length = 150)
    private String nombreProducto;

    private Integer cantidad;

    @Column(name = "precio_unit", precision = 10, scale = 2)
    private BigDecimal precioUnit;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;
}

