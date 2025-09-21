console.log("categoryChart.js loaded");


window.addEventListener('DOMContentLoaded', function () {
    const categoryData = window.categoryData || {};
    const labels = Object.keys(categoryData);
    const data = Object.values(categoryData);

    const ctx = document.getElementById('categoryChart');
    if (ctx) {
        new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    label: 'カテゴリ別支出',
                    data: data,
                    backgroundColor: [
                        '#FF6384', '#36A2EB', '#FFCE56', '#8BC34A', '#FF9800', '#9C27B0'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }
});
console.log("categoryData:", window.categoryData);