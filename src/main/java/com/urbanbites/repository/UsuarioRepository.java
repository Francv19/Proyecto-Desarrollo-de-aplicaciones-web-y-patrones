package com.urbanbites.repository;

import com.urbanbites.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Usuario findByUsername(String username);
    Usuario findByCorreo(String correo);
    Usuario findByUsernameAndPassword(String username, String password);
    Usuario findByUsernameOrCorreo(String username, String correo);
    boolean existsByUsernameOrCorreo(String username, String correo);
}

