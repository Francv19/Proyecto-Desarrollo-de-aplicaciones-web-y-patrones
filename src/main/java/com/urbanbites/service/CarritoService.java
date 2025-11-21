package com.urbanbites.service;

import com.urbanbites.domain.Carrito;
import com.urbanbites.domain.DetalleCarrito;
import com.urbanbites.domain.Pedido;
import com.urbanbites.domain.Producto;
import com.urbanbites.domain.Usuario;
import com.urbanbites.repository.CarritoRepository;
import com.urbanbites.repository.DetalleCarritoRepository;
import com.urbanbites.repository.DetallePedidoRepository;
import com.urbanbites.repository.PedidoRepository;
import com.urbanbites.repository.ProductoRepository;
import com.urbanbites.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CarritoService {
    @Autowired
    private CarritoRepository carritoRepository;
    
    @Autowired
    private DetalleCarritoRepository detalleCarritoRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    public Carrito obtenerCarritoAbierto(Integer idUsuario) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        
        // Buscar carrito existente abierto
        Optional<Carrito> carritoOpt = carritoRepository.findByUsuarioIdUsuarioAndEstado(
            idUsuario, Carrito.EstadoCarrito.abierto);
        
        if (carritoOpt.isPresent()) {
            Carrito carrito = carritoOpt.get();
            try {
                List<DetalleCarrito> detalles = detalleCarritoRepository.findByCarritoIdCarrito(carrito.getIdCarrito());
                carrito.setDetalles(detalles != null ? detalles : new java.util.ArrayList<>());
            } catch (Exception e) {
                carrito.setDetalles(new java.util.ArrayList<>());
            }
            return carrito;
        }
        
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUsuario));
        
        List<Carrito> carritosAbiertos = carritoRepository.findByUsuarioIdUsuario(idUsuario);
        for (Carrito c : carritosAbiertos) {
            if (c.getEstado() == Carrito.EstadoCarrito.abierto) {
                c.setEstado(Carrito.EstadoCarrito.cancelado);
                carritoRepository.save(c);
            }
        }
        
        Carrito nuevoCarrito = new Carrito();
        nuevoCarrito.setUsuario(usuario);
        nuevoCarrito.setEstado(Carrito.EstadoCarrito.abierto);
        nuevoCarrito.setFechaCreacion(LocalDateTime.now());
        nuevoCarrito.setDetalles(new java.util.ArrayList<>());
        
        Carrito carritoGuardado = carritoRepository.save(nuevoCarrito);
        carritoGuardado.setDetalles(new java.util.ArrayList<>());
        
        return carritoGuardado;
    }

    public void agregarProducto(Integer idUsuario, Integer idProducto, Integer cantidad) {
        // Validaciones iniciales
        if (idUsuario == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        if (idProducto == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        
        Carrito carrito = obtenerCarritoAbierto(idUsuario);
        
        if (carrito.getIdCarrito() == null) {
            carrito = carritoRepository.save(carrito);
        }
        
        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + idProducto));
        
        if (producto.getDisponible() == null || !producto.getDisponible()) {
            throw new RuntimeException("El producto '" + (producto.getNombre() != null ? producto.getNombre() : "sin nombre") + "' no está disponible");
        }
        
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El producto '" + (producto.getNombre() != null ? producto.getNombre() : "sin nombre") + "' no tiene un precio válido");
        }
        
        List<DetalleCarrito> detalles = detalleCarritoRepository.findByCarritoIdCarrito(carrito.getIdCarrito());
        if (detalles == null) {
            detalles = new java.util.ArrayList<>();
        }
        
        for (DetalleCarrito detalle : detalles) {
            if (detalle != null && detalle.getProducto() != null && 
                detalle.getProducto().getIdProducto() != null &&
                detalle.getProducto().getIdProducto().equals(idProducto)) {
                int nuevaCantidad = detalle.getCantidad() + cantidad;
                if (nuevaCantidad <= 0) {
                    throw new IllegalArgumentException("La cantidad resultante no puede ser menor o igual a 0");
                }
                detalle.setCantidad(nuevaCantidad);
                detalle.setPrecioUnit(producto.getPrecio());
                detalleCarritoRepository.save(detalle);
                return;
            }
        }
        
        DetalleCarrito nuevoDetalle = new DetalleCarrito();
        nuevoDetalle.setCarrito(carrito);
        nuevoDetalle.setProducto(producto);
        nuevoDetalle.setCantidad(cantidad);
        nuevoDetalle.setPrecioUnit(producto.getPrecio());
        
        detalleCarritoRepository.save(nuevoDetalle);
    }

    public void actualizarCantidad(Integer idDetalle, Integer nuevaCantidad) {
        if (nuevaCantidad == null || nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        
        DetalleCarrito detalle = detalleCarritoRepository.findById(idDetalle)
            .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));
        
        detalle.setCantidad(nuevaCantidad);
        detalleCarritoRepository.save(detalle);
    }

    public void eliminarProducto(Integer idDetalle) {
        detalleCarritoRepository.deleteById(idDetalle);
    }

    public Pedido confirmarPedido(Integer idUsuario) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        
        Carrito carrito = obtenerCarritoAbierto(idUsuario);
        
        // Asegurar que los detalles estén cargados
        if (carrito.getDetalles() == null || carrito.getDetalles().isEmpty()) {
            // Intentar cargar los detalles manualmente
            if (carrito.getIdCarrito() != null) {
                List<DetalleCarrito> detalles = detalleCarritoRepository.findByCarritoIdCarrito(carrito.getIdCarrito());
                if (detalles != null && !detalles.isEmpty()) {
                    carrito.setDetalles(detalles);
                }
            }
        }
        
        if (carrito.getDetalles() == null || carrito.getDetalles().isEmpty()) {
            throw new RuntimeException("El carrito está vacío. Agrega productos antes de confirmar.");
        }
        
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Asegurar que el primer producto esté cargado
        DetalleCarrito primerDetalle = carrito.getDetalles().get(0);
        if (primerDetalle.getProducto() == null) {
            throw new RuntimeException("Error al cargar los productos del carrito");
        }
        
        Producto primerProducto = primerDetalle.getProducto();
        com.urbanbites.domain.Foodtruck foodtruck = primerProducto.getFoodtruck();
        
        if (foodtruck == null) {
            throw new RuntimeException("Error al cargar la información del food truck");
        }
        
        for (DetalleCarrito detalle : carrito.getDetalles()) {
            if (!detalle.getProducto().getFoodtruck().getIdFoodtruck().equals(foodtruck.getIdFoodtruck())) {
                throw new RuntimeException("Todos los productos deben ser del mismo food truck");
            }
        }
        
        BigDecimal totalBruto = BigDecimal.ZERO;
        for (DetalleCarrito detalle : carrito.getDetalles()) {
            BigDecimal subtotal = detalle.getPrecioUnit().multiply(new BigDecimal(detalle.getCantidad()));
            totalBruto = totalBruto.add(subtotal);
        }
        
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFoodtruck(foodtruck);
        pedido.setEstado(Pedido.EstadoPedido.recibido);
        pedido.setTotalBruto(totalBruto);
        pedido.setDescuento(BigDecimal.ZERO);
        pedido.setTotalNeto(totalBruto);
        pedido.setFechaCreacion(LocalDateTime.now());
        
        pedido = pedidoRepository.save(pedido);
        
        List<com.urbanbites.domain.DetallePedido> detallesPedido = new java.util.ArrayList<>();
        for (DetalleCarrito detalle : carrito.getDetalles()) {
            com.urbanbites.domain.DetallePedido detallePedido = new com.urbanbites.domain.DetallePedido();
            detallePedido.setPedido(pedido);
            detallePedido.setProducto(detalle.getProducto());
            detallePedido.setNombreProducto(detalle.getProducto().getNombre());
            detallePedido.setCantidad(detalle.getCantidad());
            detallePedido.setPrecioUnit(detalle.getPrecioUnit());
            detallePedido.setSubtotal(detalle.getPrecioUnit().multiply(new BigDecimal(detalle.getCantidad())));
            
            detallePedido = detallePedidoRepository.save(detallePedido);
            detallesPedido.add(detallePedido);
        }
        
        pedido.setDetalles(detallesPedido);
        
        carrito.setEstado(Carrito.EstadoCarrito.confirmado);
        carritoRepository.save(carrito);
        
        return pedido;
    }

    public void vaciarCarrito(Integer idUsuario) {
        Carrito carrito = obtenerCarritoAbierto(idUsuario);
        if (carrito.getDetalles() != null) {
            detalleCarritoRepository.deleteAll(carrito.getDetalles());
        }
    }
}

