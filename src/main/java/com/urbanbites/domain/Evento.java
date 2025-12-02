package com.urbanbites.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "eventos")
public class Evento implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Integer idEvento;

    @ManyToOne
    @JoinColumn(name = "id_foodtruck")
    private Foodtruck foodtruck;

    @ManyToOne
    @JoinColumn(name = "id_solicitante")
    private Usuario solicitante;

    @ManyToOne
    @JoinColumn(name = "id_dueno_cotizador")
    private Usuario duenoCotizador;

    @Enumerated(EnumType.STRING)
    private EstadoEvento estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio")
    private TipoServicio tipoServicio;

    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 250)
    private String direccion;

    private Integer invitados;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitud;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitud;

    @Column(name = "monto_cotizado", precision = 12, scale = 2)
    private BigDecimal montoCotizado;

    @Column(name = "detalles_cotizacion", length = 600)
    private String detallesCotizacion;

    @Column(name = "fecha_cotizacion")
    private LocalDateTime fechaCotizacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    public enum EstadoEvento {
        pendiente, cotizado, aceptado, rechazado, cancelado
    }

    public enum TipoServicio {
        catering, delivery, otro
    }
}

