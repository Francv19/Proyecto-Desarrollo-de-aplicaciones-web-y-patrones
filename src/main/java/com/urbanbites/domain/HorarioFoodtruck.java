package com.urbanbites.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "horarios_foodtruck")
public class HorarioFoodtruck implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private Integer idHorario;

    @ManyToOne
    @JoinColumn(name = "id_foodtruck")
    private Foodtruck foodtruck;

    @Column(name = "dia_semana", columnDefinition = "TINYINT")
    private Integer diaSemana; // 1=Lunes, 7=Domingo

    @Column(length = 250)
    private String direccion;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitud;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitud;

    @Column(name = "hora_apertura")
    private LocalTime horaApertura;

    @Column(name = "hora_cierre")
    private LocalTime horaCierre;

    private Boolean activo;
    
    public String getNombreDia() {
        String[] dias = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        return diaSemana != null && diaSemana >= 1 && diaSemana <= 7 ? dias[diaSemana] : "Desconocido";
    }
}

