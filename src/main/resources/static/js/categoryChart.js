console.log("categoryChart.js loaded");

document.addEventListener('DOMContentLoaded', function () {
  // 支出カテゴリグラフ
  const categoryData = window.categoryData || {};
  console.log("categoryData:", categoryData);
  const expenseLabels = Object.keys(categoryData);
  const expenseValues = Object.values(categoryData);

  const expenseCtx = document.getElementById('categoryChart');
  if (expenseCtx && expenseLabels.length > 0) {
    new Chart(expenseCtx, {
      type: 'pie',
      data: {
        labels: expenseLabels,
        datasets: [{
          label: 'カテゴリ別支出',
          data: expenseValues,
          backgroundColor: [
            '#FF6384', '#36A2EB', '#FFCE56', '#8BC34A', '#FF9800', '#9C27B0'
          ],
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { position: 'bottom' }
        }
      }
    });
  }

  // 収入カテゴリグラフ
  const incomeData = window.incomeCategoryData || {};
  const incomeLabels = Object.keys(incomeData);
  const incomeValues = Object.values(incomeData);

  const incomeCtx = document.getElementById('incomeChart');
  if (incomeCtx && incomeLabels.length > 0) {
    new Chart(incomeCtx, {
      type: 'pie',
      data: {
        labels: incomeLabels,
        datasets: [{
          data: incomeValues,
          backgroundColor: [
            '#4caf50', '#81c784', '#a5d6a7', '#c8e6c9',
            '#66bb6a', '#388e3c', '#2e7d32', '#1b5e20'
          ]
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { position: 'bottom' },
          title: {
            display: true,
            text: 'カテゴリ別収入'
          }
        }
      }
    });
  }
});