package com.urbanbites.service;

import com.urbanbites.domain.Rol;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.RolRepository;
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
    
    @Autowired
    private RolRepository rolRepository;

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
        if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
            for (Rol rol : usuario.getRoles()) {
                String roleName = "ROLE_" + rol.getNombre();
                roles.add(new SimpleGrantedAuthority(roleName));
            }
        } else {
            // Si no tiene roles, asignar rol de cliente por defecto
            Rol rolCliente = rolRepository.findByNombre("cliente");
            if (rolCliente != null) {
                // Asignar el rol al usuario en la base de datos
                if (usuario.getRoles() == null) {
                    usuario.setRoles(new ArrayList<>());
                }
                usuario.getRoles().add(rolCliente);
                usuarioRepository.save(usuario);
                usuarioRepository.flush();
                // Agregar el rol a la lista de authorities
                roles.add(new SimpleGrantedAuthority("ROLE_cliente"));
            } else {
                // Si no existe el rol cliente, lanzar excepción
                throw new UsernameNotFoundException("Rol cliente no encontrado en la base de datos");
            }
        }

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                roles
        );
    }
}


