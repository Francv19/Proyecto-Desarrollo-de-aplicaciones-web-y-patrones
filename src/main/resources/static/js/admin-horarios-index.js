// Funcionalidad para la página de índice de horarios del admin
function abrirModalEliminarHorario(button) {
    const horarioId = button.getAttribute('data-horario-id');
    const horarioDia = button.getAttribute('data-horario-dia');
    
    document.getElementById('diaHorarioAdminEliminar').textContent = horarioDia;
    
    const form = document.getElementById('formEliminarHorarioAdmin');
    form.action = '/admin/horarios/' + horarioId + '/eliminar';
}

