// Validación de horas para formularios de horarios
document.addEventListener('DOMContentLoaded', function() {
    const horaApertura = document.getElementById('horaApertura');
    const horaCierre = document.getElementById('horaCierre');
    
    if (horaApertura && horaCierre) {
        horaApertura.addEventListener('change', validarHoras);
        horaCierre.addEventListener('change', validarHoras);
    }
    
    function validarHoras() {
        const apertura = horaApertura.value;
        const cierre = horaCierre.value;
        
        if (apertura && cierre) {
            const horaAperturaDate = new Date('2000-01-01T' + apertura);
            const horaCierreDate = new Date('2000-01-01T' + cierre);
            
            if (horaCierreDate <= horaAperturaDate) {
                horaCierre.setCustomValidity('La hora de cierre debe ser posterior a la hora de apertura');
            } else {
                horaCierre.setCustomValidity('');
            }
        }
    }
});

