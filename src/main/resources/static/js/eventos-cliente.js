// Funcionalidad para la página de eventos del cliente

// Filtrar eventos por estado
function filtrarEventos(estado) {
    const cards = document.querySelectorAll('.evento-card');
    const buttons = document.querySelectorAll('.filter-btn');
    
    // Actualizar botones activos
    buttons.forEach(btn => {
        if (btn.getAttribute('data-filter') === estado) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
    
    // Mostrar/ocultar cards según el filtro
    cards.forEach(card => {
        const cardEstado = card.getAttribute('data-estado');
        if (estado === 'todos' || cardEstado === estado) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}

// Abrir modal para rechazar cotización
function abrirModalRechazarCotizacion(button) {
    const eventoId = button.getAttribute('data-evento-id');
    const eventoNombre = button.getAttribute('data-evento-nombre');
    
    document.getElementById('nombreEventoRechazarCotizacion').textContent = eventoNombre;
    
    const form = document.getElementById('formRechazarCotizacion');
    form.action = '/eventos/' + eventoId + '/estado';
}

// Abrir modal para cancelar evento
function abrirModalCancelarEvento(button) {
    const eventoId = button.getAttribute('data-evento-id');
    const eventoNombre = button.getAttribute('data-evento-nombre');
    
    document.getElementById('nombreEventoCancelar').textContent = eventoNombre;
    
    const form = document.getElementById('formCancelarEvento');
    form.action = '/eventos/' + eventoId + '/estado';
}

// Abrir modal para eliminar evento
function abrirModalEliminarEvento(button) {
    const eventoId = button.getAttribute('data-evento-id');
    const eventoNombre = button.getAttribute('data-evento-nombre');
    
    document.getElementById('nombreEventoEliminar').textContent = eventoNombre;
    
    const form = document.getElementById('formEliminarEvento');
    form.action = '/eventos/' + eventoId + '/eliminar';
}

