package com.urbanbites.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    private String username;
    private String password;
    private String nombre;
    private String apellidos;
    private String correo;
    private String telefono;
    private boolean activo;

    @ManyToMany
    @JoinTable(
        name = "usuario_rol",
        joinColumns = @JoinColumn(name = "id_usuario"),
        inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    private List<Rol> roles;
}

