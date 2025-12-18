package com.urbanbites.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Data
@Entity
@Table(name = "fotos_productos")
public class FotoProducto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    private Integer idFoto;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(name = "alt_text", length = 150)
    private String altText;

    @Enumerated(EnumType.STRING)
    private FormatoFoto formato;

    private Integer bytes;

    @Column(name = "activo")
    private Boolean activo;

    public enum FormatoFoto {
        jpg, jpeg, png, webp
    }
}

