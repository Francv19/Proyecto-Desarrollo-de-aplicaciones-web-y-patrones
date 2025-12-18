// Funciones para el carrito de compras
function abrirModalEliminarProducto(button) {
    const detalleId = button.getAttribute('data-detalle-id');
    const productoNombre = button.getAttribute('data-producto-nombre');
    
    document.getElementById('nombreProductoCarrito').textContent = productoNombre;
    document.getElementById('idDetalleEliminar').value = detalleId;
}

// Variables globales que se inicializarán desde el HTML
let totalBruto = 0;
let saldoPuntos = 0;

function formatearMoneda(valor) {
    return '₡' + valor.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function calcularDescuento() {
    const puntosInput = document.getElementById('puntosACanjear');
    if (!puntosInput) return;
    
    let puntos = parseInt(puntosInput.value) || 0;
    
    if (puntos > saldoPuntos) {
        puntos = saldoPuntos;
        puntosInput.value = puntos;
    }
    
    if (puntos < 0) {
        puntos = 0;
        puntosInput.value = 0;
    }
    
    const descuento = puntos / 10;
    
    const descuentoFinal = Math.min(descuento, totalBruto);
    
    const puntosReales = Math.floor(descuentoFinal * 10);
    
    const descuentoAplicadoEl = document.getElementById('descuentoAplicado');
    if (descuentoAplicadoEl) {
        descuentoAplicadoEl.textContent = formatearMoneda(descuentoFinal);
    }
    
    const totalFinal = Math.max(0, totalBruto - descuentoFinal);
    const totalFinalEl = document.getElementById('totalFinal');
    if (totalFinalEl) {
        totalFinalEl.textContent = 'Total: ' + formatearMoneda(totalFinal);
    }
    
    const descuentoLine = document.getElementById('descuentoLine');
    const descuentoLineText = document.getElementById('descuentoLineText');
    if (descuentoLine && descuentoLineText) {
        if (descuentoFinal > 0) {
            descuentoLine.style.display = 'flex';
            descuentoLineText.textContent = '-' + formatearMoneda(descuentoFinal);
        } else {
            descuentoLine.style.display = 'none';
        }
    }
    
    const puntosHidden = document.getElementById('puntosACanjearHidden');
    if (puntosHidden) {
        puntosHidden.value = puntosReales;
    }
}

function usarTodosLosPuntos() {
    const puntosInput = document.getElementById('puntosACanjear');
    if (!puntosInput) return;
    
    const maxPuntosPorTotal = Math.floor(totalBruto * 10);
    const puntosAUsar = Math.min(saldoPuntos, maxPuntosPorTotal);
    
    puntosInput.value = puntosAUsar;
    calcularDescuento();
}

// Inicialización cuando el DOM está listo
document.addEventListener('DOMContentLoaded', function() {
    // Los valores de totalBruto y saldoPuntos se inicializarán desde el HTML usando Thymeleaf inline
    if (saldoPuntos > 0) {
        calcularDescuento();
    }
});

