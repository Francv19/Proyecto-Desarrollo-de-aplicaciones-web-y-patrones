// Funcionalidad para la página de índice de productos
function filtrarProductos() {
    const foodtruckId = document.getElementById('selectFoodtruck').value;
    const url = new URL(window.location.href);
    if (foodtruckId) {
        url.searchParams.set('foodtruckId', foodtruckId);
    } else {
        url.searchParams.delete('foodtruckId');
    }
    window.location.href = url.toString();
}

function abrirModalEliminar(button) {
    const productoId = button.getAttribute('data-producto-id');
    const productoNombre = button.getAttribute('data-producto-nombre');
    
    // Actualizar el nombre del producto en el modal
    document.getElementById('nombreProductoEliminar').textContent = productoNombre;
    
    // Actualizar la acción del formulario
    const form = document.getElementById('formEliminarProducto');
    form.action = '/owner/productos/' + productoId + '/eliminar';
}

