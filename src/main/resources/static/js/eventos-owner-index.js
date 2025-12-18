// Funcionalidad para la página de índice de eventos del owner

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

// Abrir modal para rechazar evento
function abrirModalRechazarEvento(button) {
    const eventoId = button.getAttribute('data-evento-id');
    const eventoNombre = button.getAttribute('data-evento-nombre');
    
    document.getElementById('nombreEventoRechazar').textContent = eventoNombre;
    
    const form = document.getElementById('formRechazarEvento');
    form.action = '/owner/eventos/' + eventoId + '/estado';
}

// Abrir modal para eliminar evento (Owner)
function abrirModalEliminarEventoOwner(button) {
    const eventoId = button.getAttribute('data-evento-id');
    const eventoNombre = button.getAttribute('data-evento-nombre');
    
    document.getElementById('nombreEventoEliminarOwner').textContent = eventoNombre;
    
    const form = document.getElementById('formEliminarEventoOwner');
    form.action = '/owner/eventos/' + eventoId + '/eliminar';
}

