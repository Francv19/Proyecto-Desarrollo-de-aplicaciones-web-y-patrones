// Filtros para la página de pedidos del owner
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            const filter = this.dataset.filter;
            filterOrders(filter);
        });
    });

    function filterOrders(filter) {
        // Implementar lógica de filtrado de pedidos
        console.log('Filtrar por:', filter);
    }
});

