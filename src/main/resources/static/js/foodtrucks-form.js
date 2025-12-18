// Validación del porcentaje de puntos para formularios de food trucks
document.addEventListener('DOMContentLoaded', function() {
    const porcentajeInput = document.getElementById('porcentajePuntos');
    if (porcentajeInput) {
        porcentajeInput.addEventListener('input', function(e) {
            const value = parseInt(e.target.value);
            if (value < 0 || value > 100) {
                e.target.setCustomValidity('El porcentaje debe estar entre 0 y 100');
            } else {
                e.target.setCustomValidity('');
            }
        });
    }
});

