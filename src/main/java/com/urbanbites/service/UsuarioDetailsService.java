package com.urbanbites.service;

import com.urbanbites.domain.Rol;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.UsuarioRepository;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userDetailsService")
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByUsername(username);

        if (usuario == null) {
            throw new UsernameNotFoundException("No existe el usuario: " + username);
        }

        // Convertir roles a roles (con prefijo ROLE_)
        var roles = new ArrayList<GrantedAuthority>();
        if (usuario.getRoles() != null) {
            for (Rol rol : usuario.getRoles()) {
                roles.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombre()));
            }
        }

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                roles
        );
    }
}

