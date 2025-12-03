package com.urbanbites.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "promociones")
public class Promocion implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion")
    private Integer idPromocion;

    @ManyToOne
    @JoinColumn(name = "id_foodtruck")
    private Foodtruck foodtruck;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento")
    private TipoDescuento tipoDescuento;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "fecha_inicio")
    private java.time.LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private java.time.LocalDateTime fechaFin;

    @Column(name = "activo")
    private Boolean activo;

    public enum TipoDescuento {
        porcentaje, monto_fijo
    }
}

