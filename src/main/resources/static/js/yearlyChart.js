document.addEventListener('DOMContentLoaded', function () {
  const ctx = document.getElementById('yearlyChart');
  if (!ctx) return;

  const yen = new Intl.NumberFormat('ja-JP');
  const incomeRaw = window.yearlyIncome || {};
  const expenseRaw = window.yearlyExpense || {};
  const balanceRaw = window.yearlyBalance || {};
  const months = Array.from({ length: 12 }, (_, i) => (i + 1).toString());

  const labels = months.map((m) => `${m}月`);
  const income = months.map((m) => incomeRaw[m] ?? 0);
  const expense = months.map((m) => expenseRaw[m] ?? 0);
  const balance = months.map((m) => balanceRaw[m] ?? 0);

  new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: '収入',
          data: income,
          backgroundColor: '#10B981',
          borderRadius: 6
        },
        {
          label: '支出',
          data: expense,
          backgroundColor: '#EF4444',
          borderRadius: 6
        },
        {
          label: '累計差額',
          data: balance,
          type: 'line',
          borderColor: '#2563EB',
          backgroundColor: 'rgba(37, 99, 235, 0.15)',
          fill: false,
          tension: 0.3,
          pointRadius: 2
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      plugins: {
        legend: { position: 'top' },
        tooltip: {
          callbacks: {
            label: (ctx2) => `${ctx2.dataset.label}: ${yen.format(ctx2.raw)}円`
          }
        }
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
});
