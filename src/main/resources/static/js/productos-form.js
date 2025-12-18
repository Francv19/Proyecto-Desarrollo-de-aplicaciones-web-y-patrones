// Funcionalidad para el formulario de productos
document.addEventListener('DOMContentLoaded', function() {
    // Mapa de menús principales por food truck (se inicializará desde el HTML)
    let menusPorFoodtruck = {};
    
    // Función para actualizar el menú principal cuando cambia el food truck
    function actualizarMenuPrincipal() {
        const foodtruckSelect = document.getElementById('idFoodtruck');
        const menuInput = document.getElementById('idMenu');
        
        if (foodtruckSelect && menuInput) {
            const foodtruckId = parseInt(foodtruckSelect.value);
            
            if (foodtruckId && menusPorFoodtruck[foodtruckId]) {
                menuInput.value = menusPorFoodtruck[foodtruckId];
            } else {
                menuInput.value = '';
            }
        }
    }
    
    const foodtruckSelect = document.getElementById('idFoodtruck');
    if (foodtruckSelect) {
        // Event listener para el selector de food truck
        foodtruckSelect.addEventListener('change', actualizarMenuPrincipal);
        // Inicializar menú al cargar la página
        actualizarMenuPrincipal();
    }
    
    // Validación del precio
    const precioInput = document.getElementById('precio');
    if (precioInput) {
        precioInput.addEventListener('input', function(e) {
            const value = parseFloat(e.target.value);
            if (value <= 0) {
                e.target.setCustomValidity('El precio debe ser mayor a 0');
            } else {
                e.target.setCustomValidity('');
            }
        });
    }
    
    // Exponer función para inicializar desde el HTML
    window.initProductosForm = function(menus) {
        menusPorFoodtruck = menus || {};
        actualizarMenuPrincipal();
    };
});

