package com.urbanbites.service;

import com.urbanbites.domain.Rol;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.RolRepository;
import com.urbanbites.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario registrarCliente(String nombre, String apellidos, String correo, 
                                   String password, String telefono) {
        if (usuarioRepository.existsByUsernameOrCorreo(correo, correo)) {
            throw new RuntimeException("El correo ya está registrado");
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setUsername(correo); // Usar correo como username
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setTelefono(telefono);
        usuario.setActivo(true);
        
        Rol rolCliente = rolRepository.findByNombre("cliente");
        if (rolCliente == null) {
            throw new RuntimeException("Rol cliente no encontrado");
        }
        if (usuario.getRoles() == null) {
            usuario.setRoles(new java.util.ArrayList<>());
        }
        usuario.getRoles().add(rolCliente);
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        usuarioRepository.flush();
        
        return usuarioGuardado;
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }
    
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
    
    public Usuario registrarAdmin(String nombre, String apellidos, String correo, 
                                  String password, String telefono) {
        if (usuarioRepository.existsByUsernameOrCorreo(correo, correo)) {
            throw new RuntimeException("El correo ya está registrado");
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setUsername(correo);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setTelefono(telefono);
        usuario.setActivo(true);
        
        Rol rolAdmin = rolRepository.findByNombre("admin");
        if (rolAdmin == null) {
            throw new RuntimeException("Rol admin no encontrado");
        }
        if (usuario.getRoles() == null) {
            usuario.setRoles(new java.util.ArrayList<>());
        }
        usuario.getRoles().add(rolAdmin);
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        usuarioRepository.flush();
        
        return usuarioGuardado;
    }
    
    public void actualizarPerfil(Integer idUsuario, String nombre, String apellidos, 
                                 String correo, String telefono, String password) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setUsername(correo); // Actualizar username también
        usuario.setTelefono(telefono);
        
        // Actualizar contraseña solo si se proporciona
        if (password != null && !password.isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }
        
        usuarioRepository.save(usuario);
        usuarioRepository.flush();
    }
}

