// Filtros para la página de reseñas del owner
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            document.querySelectorAll('.filter-btn').forEach(b => {
                b.classList.remove('active');
            });
            this.classList.add('active');
            const filter = this.dataset.filter;
            filterReviews(filter);
        });
    });

    function filterReviews(filter) {
        const cards = document.querySelectorAll('.review-card-owner');
        cards.forEach(card => {
            if (filter === 'todos') {
                card.style.display = 'block';
            } else {
                const badge = card.querySelector('.badge');
                const estado = badge ? badge.textContent.trim().toLowerCase() : '';
                if (estado === filter) {
                    card.style.display = 'block';
                } else {
                    card.style.display = 'none';
                }
            }
        });
    }
});

