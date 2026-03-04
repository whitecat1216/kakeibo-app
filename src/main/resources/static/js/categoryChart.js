document.addEventListener('DOMContentLoaded', function () {
  const yen = new Intl.NumberFormat('ja-JP');
  const palette = [
    '#F43F5E', '#0EA5E9', '#22C55E', '#F59E0B', '#8B5CF6',
    '#14B8A6', '#EAB308', '#EC4899', '#6366F1', '#10B981'
  ];

  const tooltipCurrency = {
    callbacks: {
      label: (ctx) => `${ctx.label}: ${yen.format(ctx.raw)}円`
    }
  };

  // 支出カテゴリグラフ
  const categoryData = window.categoryData || {};
  const expenseEntries = Object.entries(categoryData).sort((a, b) => b[1] - a[1]);
  const expenseLabels = expenseEntries.map((e) => e[0]);
  const expenseValues = expenseEntries.map((e) => e[1]);

  const expenseCtx = document.getElementById('categoryChart');
  if (expenseCtx && expenseLabels.length > 0) {
    new Chart(expenseCtx, {
      type: 'bar',
      data: {
        labels: expenseLabels,
        datasets: [{
          label: 'カテゴリ別支出',
          data: expenseValues,
          backgroundColor: expenseLabels.map((_, i) => palette[i % palette.length]),
          borderRadius: 8,
          barThickness: 20
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: tooltipCurrency
        },
        scales: {
          x: {
            ticks: {
              callback: (value) => `${yen.format(value)}円`
            },
            grid: { color: '#E5E7EB' }
          },
          y: {
            grid: { display: false }
          }
        }
      }
    });
  }

  // 収入カテゴリグラフ
  const incomeData = window.incomeCategoryData || {};
  const incomeEntries = Object.entries(incomeData).sort((a, b) => b[1] - a[1]);
  const incomeLabels = incomeEntries.map((e) => e[0]);
  const incomeCategoryValues = incomeEntries.map((e) => e[1]);

  const incomeCtx = document.getElementById('incomeChart');
  if (incomeCtx && incomeLabels.length > 0) {
    new Chart(incomeCtx, {
      type: 'bar',
      data: {
        labels: incomeLabels,
        datasets: [{
          label: 'カテゴリ別収入',
          data: incomeCategoryValues,
          backgroundColor: incomeLabels.map((_, i) => palette[(i + 2) % palette.length]),
          borderRadius: 8,
          barThickness: 20
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: tooltipCurrency
        },
        scales: {
          x: {
            ticks: {
              callback: (value) => `${yen.format(value)}円`
            },
            grid: { color: '#E5E7EB' }
          },
          y: {
            grid: { display: false }
          }
        }
      }
    });
  }

  // 月別収支推移グラフ
  const monthlyIncome = window.monthlyIncome || {};
  const monthlyExpense = window.monthlyExpense || {};
  const monthlyBalance = window.monthlyBalance || {};

  const months = Array.from({ length: 12 }, (_, i) => (i + 1).toString());
  const monthlyLabels = months.map(m => m + '月');
  const monthlyIncomeValues = months.map(m => monthlyIncome[m] ?? 0);
  const monthlyExpenseValues = months.map(m => monthlyExpense[m] ?? 0);
  const monthlyBalanceValues = months.map(m => monthlyBalance[m] ?? 0);

  const monthlyCtx = document.getElementById('monthlyChart');
  if (monthlyCtx && monthlyLabels.length > 0) {
    new Chart(monthlyCtx, {
      type: 'line',
      data: {
        labels: monthlyLabels,
        datasets: [
          {
            label: '収入',
            data: monthlyIncomeValues,
            borderColor: '#10B981',
            backgroundColor: 'rgba(16, 185, 129, 0.2)',
            fill: false,
            tension: 0.35,
            pointRadius: 3
          },
          {
            label: '支出',
            data: monthlyExpenseValues,
            borderColor: '#EF4444',
            backgroundColor: 'rgba(239, 68, 68, 0.2)',
            fill: false,
            tension: 0.35,
            pointRadius: 3
          },
          {
            label: '残高',
            data: monthlyBalanceValues,
            borderColor: '#2563eb',
            backgroundColor: 'rgba(37, 99, 235, 0.15)',
            fill: true,
            tension: 0.35,
            pointRadius: 2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'top' },
          tooltip: tooltipCurrency
        },
        scales: {
          y: {
            ticks: {
              callback: (value) => `${yen.format(value)}円`
            },
            grid: { color: '#E5E7EB' }
          },
          x: {
            grid: { display: false }
          }
        }
      }
    });
  }
});
