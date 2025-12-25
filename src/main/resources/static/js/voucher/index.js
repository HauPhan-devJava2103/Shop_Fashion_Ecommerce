// Voucher Table Sorting (Client-side - No page reload)
document.addEventListener('DOMContentLoaded', function() {
    const table = document.getElementById('vouchersTable');
    if (!table) return;

    const tbody = table.querySelector('tbody');
    const headers = table.querySelectorAll('th[data-sort]');
    let currentSort = { field: null, dir: 'asc' };

    headers.forEach(header => {
        header.style.cursor = 'pointer';
        header.addEventListener('click', function() {
            const field = this.dataset.sort;
            
            // Toggle direction
            if (currentSort.field === field) {
                currentSort.dir = currentSort.dir === 'asc' ? 'desc' : 'asc';
            } else {
                currentSort.field = field;
                currentSort.dir = 'asc';
            }

            sortTable(field, currentSort.dir);
            updateSortIcons(headers, field, currentSort.dir);
        });
    });

    function sortTable(field, direction) {
        const rows = Array.from(tbody.querySelectorAll('tr:not(.empty-state)'));
        
        rows.sort((a, b) => {
            const aCell = a.querySelector(`[data-value="${field}"]`);
            const bCell = b.querySelector(`[data-value="${field}"]`);
            
            if (!aCell || !bCell) return 0;

            let aVal = parseFloat(aCell.dataset.raw) || 0;
            let bVal = parseFloat(bCell.dataset.raw) || 0;

            if (direction === 'asc') {
                return aVal - bVal;
            } else {
                return bVal - aVal;
            }
        });

        // Re-append rows in sorted order
        rows.forEach(row => tbody.appendChild(row));
    }

    function updateSortIcons(headers, activeField, direction) {
        headers.forEach(header => {
            const icon = header.querySelector('.sort-icon');
            if (!icon) return;

            if (header.dataset.sort === activeField) {
                icon.className = direction === 'asc' 
                    ? 'bi bi-caret-up-fill sort-icon' 
                    : 'bi bi-caret-down-fill sort-icon';
            } else {
                icon.className = 'bi bi-arrow-down-up text-muted sort-icon';
            }
        });
    }
});
