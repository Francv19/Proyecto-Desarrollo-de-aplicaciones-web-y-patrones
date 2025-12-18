// Funcionalidad para la página de menú
let selectedFoodtruckId = null;
let currentProductSearch = '';

// Inicializar con food truck seleccionado desde la URL (si existe)
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const foodtruckParam = urlParams.get('foodtruck');
    if (foodtruckParam) {
        selectFoodtruck(parseInt(foodtruckParam), false);
    }
});

function selectFoodtruck(id, updateUrl = true) {
    selectedFoodtruckId = id;
    
    // Actualizar selección visual en las tarjetas
    document.querySelectorAll('.foodtruck-card').forEach(card => {
        const cardId = parseInt(card.getAttribute('data-foodtruck-id'));
        if (cardId === id) {
            card.classList.add('selected');
        } else {
            card.classList.remove('selected');
        }
    });

    // Obtener nombre del food truck seleccionado
    const selectedCard = document.querySelector(`.foodtruck-card[data-foodtruck-id="${id}"]`);
    const foodtruckName = selectedCard?.querySelector('.foodtruck-name')?.textContent || 'Food Truck';
    
    // Actualizar badge y botón de limpiar
    const badge = document.getElementById('selectedFoodtruckBadge');
    const badgeName = document.getElementById('selectedFoodtruckName');
    const clearBtn = document.getElementById('clearFilterBtn');
    
    if (badge && badgeName && clearBtn) {
        badgeName.textContent = foodtruckName;
        badge.style.display = 'inline-block';
        clearBtn.style.display = 'inline-block';
    }

    // Filtrar productos
    filterProductosByFoodtruck(id);
    
    // Actualizar URL sin recargar
    if (updateUrl) {
        const newUrl = new URL(window.location);
        newUrl.searchParams.set('foodtruck', id);
        window.history.pushState({foodtruck: id}, '', newUrl);
    }

    // Scroll suave hacia la sección de productos
    document.getElementById('productosContainer')?.scrollIntoView({ 
        behavior: 'smooth', 
        block: 'start' 
    });
}

function clearFoodtruckFilter() {
    selectedFoodtruckId = null;
    
    // Remover selección visual
    document.querySelectorAll('.foodtruck-card').forEach(card => {
        card.classList.remove('selected');
    });

    // Ocultar badge y botón
    const badge = document.getElementById('selectedFoodtruckBadge');
    const clearBtn = document.getElementById('clearFilterBtn');
    if (badge) badge.style.display = 'none';
    if (clearBtn) clearBtn.style.display = 'none';

    // Mostrar todos los productos
    filterProductosByFoodtruck(null);
    
    // Actualizar URL
    const newUrl = new URL(window.location);
    newUrl.searchParams.delete('foodtruck');
    window.history.pushState({}, '', newUrl);
}

function filterProductosByFoodtruck(id) {
    const productos = document.querySelectorAll('.producto-item');
    const noProductsMessage = document.getElementById('noProductsMessage');
    let visibleCount = 0;

    productos.forEach(item => {
        const itemFoodtruckId = parseInt(item.getAttribute('data-foodtruck-id'));
        const productoCard = item.querySelector('.producto-card');
        
        // Aplicar filtro de food truck
        const matchesFoodtruck = id === null || itemFoodtruckId === id;
        
        // Aplicar filtro de búsqueda
        const matchesSearch = currentProductSearch === '' || 
            item.textContent.toLowerCase().includes(currentProductSearch.toLowerCase());
        
        const shouldShow = matchesFoodtruck && matchesSearch;
        
        if (shouldShow) {
            productoCard.classList.remove('fade-out', 'hidden');
            productoCard.classList.add('fade-in');
            item.style.display = '';
            visibleCount++;
        } else {
            productoCard.classList.add('fade-out');
            setTimeout(() => {
                productoCard.classList.add('hidden');
                item.style.display = 'none';
            }, 200);
        }
    });

    // Mostrar/ocultar mensaje de no productos
    if (noProductsMessage) {
        if (visibleCount === 0 && id !== null) {
            noProductsMessage.style.display = 'block';
        } else {
            noProductsMessage.style.display = 'none';
        }
    }
}

// Búsqueda de food trucks
document.getElementById('searchFoodtruck')?.addEventListener('input', function(e) {
    const searchTerm = e.target.value.toLowerCase();
    document.querySelectorAll('.foodtruck-card').forEach(card => {
        const name = card.querySelector('.foodtruck-name')?.textContent.toLowerCase() || '';
        const description = card.querySelector('.foodtruck-description')?.textContent.toLowerCase() || '';
        const parent = card.closest('.col-md-4, .col-lg-3');
        if (name.includes(searchTerm) || description.includes(searchTerm)) {
            parent.style.display = '';
        } else {
            parent.style.display = 'none';
        }
    });
});

// Búsqueda de productos
document.getElementById('searchProducto')?.addEventListener('input', function(e) {
    currentProductSearch = e.target.value;
    // Re-aplicar filtro de food truck con la nueva búsqueda
    filterProductosByFoodtruck(selectedFoodtruckId);
});

// Funciones para control de cantidad
function increaseQuantity(productId) {
    const input = document.getElementById('quantity-' + productId);
    const currentValue = parseInt(input.value) || 1;
    if (currentValue < 99) {
        input.value = currentValue + 1;
        updateQuantityButtons(productId);
    }
}

function decreaseQuantity(productId) {
    const input = document.getElementById('quantity-' + productId);
    const currentValue = parseInt(input.value) || 1;
    if (currentValue > 1) {
        input.value = currentValue - 1;
        updateQuantityButtons(productId);
    }
}

function updateQuantityButtons(productId) {
    const input = document.getElementById('quantity-' + productId);
    const value = parseInt(input.value) || 1;
    const decreaseBtn = document.getElementById('btn-decrease-' + productId);
    const increaseBtn = document.getElementById('btn-increase-' + productId);
    
    if (decreaseBtn) {
        decreaseBtn.disabled = value <= 1;
    }
    if (increaseBtn) {
        increaseBtn.disabled = value >= 99;
    }
}

// Inicializar botones de cantidad al cargar la página
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.quantity-input').forEach(input => {
        const productId = input.id.replace('quantity-', '');
        updateQuantityButtons(productId);
    });
});

