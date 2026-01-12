/**
 * Order Management Charts
 * - Pie Chart: Order Status Distribution
 * - Line Chart: Order Trends (7D/30D/90D)
 */

let orderTrendsChart = null;

document.addEventListener("DOMContentLoaded", function () {
  initOrderStatusChart();
  initOrderTrendsChart(7);
  initTrendsPeriodToggle();
});

// Order Status Pie Chart
function initOrderStatusChart() {
  const ctx = document.getElementById("orderStatusChart");
  if (!ctx) return;

  const statusPending = parseInt(ctx.dataset.pending || 0);
  const statusConfirmed = parseInt(ctx.dataset.confirmed || 0);
  const statusProcessing = parseInt(ctx.dataset.processing || 0);
  const statusShipped = parseInt(ctx.dataset.shipped || 0);
  const statusDelivered = parseInt(ctx.dataset.delivered || 0);
  const statusCompleted = parseInt(ctx.dataset.completed || 0);
  const statusCancelled = parseInt(ctx.dataset.cancelled || 0);

  new Chart(ctx, {
    type: "doughnut",
    data: {
      labels: [
        "Chờ xác nhận",
        "Đã xác nhận",
        "Đang xử lý",
        "Đang giao hàng",
        "Đã giao hàng",
        "Hoàn thành",
        "Đã hủy",
      ],
      datasets: [
        {
          label: "Số đơn hàng",
          data: [
            statusPending,
            statusConfirmed,
            statusProcessing,
            statusShipped,
            statusDelivered,
            statusCompleted,
            statusCancelled,
          ],
          backgroundColor: [
            "rgba(255, 193, 7, 0.8)",
            "rgba(13, 202, 240, 0.8)",
            "rgba(13, 110, 253, 0.8)",
            "rgba(255, 159, 64, 0.8)",
            "rgba(75, 192, 192, 0.8)",
            "rgba(25, 135, 84, 0.8)",
            "rgba(220, 53, 69, 0.8)",
          ],
          borderColor: [
            "rgba(255, 193, 7, 1)",
            "rgba(13, 202, 240, 1)",
            "rgba(13, 110, 253, 1)",
            "rgba(255, 159, 64, 1)",
            "rgba(75, 192, 192, 1)",
            "rgba(25, 135, 84, 1)",
            "rgba(220, 53, 69, 1)",
          ],
          borderWidth: 2,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: "right",
          labels: {
            padding: 15,
            font: { size: 12 },
            generateLabels: function (chart) {
              const data = chart.data;
              const total = data.datasets[0].data.reduce((a, b) => a + b, 0);
              return data.labels.map((label, i) => {
                const value = data.datasets[0].data[i];
                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
                return {
                  text: `${label}: ${value} (${percentage}%)`,
                  fillStyle: data.datasets[0].backgroundColor[i],
                  hidden: false,
                  index: i,
                };
              });
            },
          },
        },
        tooltip: {
          callbacks: {
            label: function (context) {
              const label = context.label || "";
              const value = context.parsed;
              const total = context.dataset.data.reduce((a, b) => a + b, 0);
              const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
              return `${label}: ${value} đơn (${percentage}%)`;
            },
          },
        },
      },
    },
  });
}

// Order Trends Line Chart (fetch from API)
function initOrderTrendsChart(days) {
  console.log("Fetching order trends for days:", days);
  
  fetch("/admin/orders/api/trends?days=" + days)
    .then((response) => response.json())
    .then((data) => {
      console.log("Order trends data received:", data);

      const canvas = document.getElementById("orderTrendsChart");
      if (!canvas) {
        console.error("Canvas element 'orderTrendsChart' not found");
        return;
      }

      const ctx = canvas.getContext("2d");

      if (orderTrendsChart) {
        orderTrendsChart.destroy();
      }

      const labels = data && data.labels ? data.labels : [];
      const orderValues = data && data.data ? data.data : [];
      const revenueValues = data && data.revenueData ? data.revenueData : [];

      orderTrendsChart = new Chart(ctx, {
        type: "line",
        data: {
          labels: labels,
          datasets: [
            {
              label: "Đơn hàng",
              data: orderValues,
              borderColor: "#6366f1",
              backgroundColor: "rgba(99, 102, 241, 0.1)",
              borderWidth: 2,
              fill: true,
              tension: 0.4,
              pointRadius: 4,
              pointHoverRadius: 6,
              yAxisID: 'y',
            },
            {
              label: "Doanh thu",
              data: revenueValues,
              borderColor: "#10b981",
              backgroundColor: "rgba(16, 185, 129, 0.1)",
              borderWidth: 2,
              fill: true,
              tension: 0.4,
              pointRadius: 4,
              pointHoverRadius: 6,
              yAxisID: 'y1',
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          interaction: {
            mode: 'index',
            intersect: false,
          },
          plugins: {
            legend: { 
              display: true,
              position: 'top',
            },
            tooltip: {
              backgroundColor: "rgba(0,0,0,0.8)",
              padding: 12,
              titleFont: { size: 14 },
              bodyFont: { size: 13 },
              callbacks: {
                label: function(context) {
                  let label = context.dataset.label || '';
                  if (label) {
                    label += ': ';
                  }
                  if (context.dataset.yAxisID === 'y1') {
                    // Format revenue
                    label += new Intl.NumberFormat('vi-VN').format(context.parsed.y) + ' ₫';
                  } else {
                    // Format order count
                    label += context.parsed.y + ' đơn';
                  }
                  return label;
                }
              }
            },
          },
          scales: {
            y: {
              type: 'linear',
              display: true,
              position: 'left',
              beginAtZero: true,
              ticks: { stepSize: 1 },
              title: {
                display: true,
                text: 'Đơn hàng'
              }
            },
            y1: {
              type: 'linear',
              display: true,
              position: 'right',
              beginAtZero: true,
              grid: {
                drawOnChartArea: false,
              },
              ticks: {
                callback: function(value) {
                  if (value >= 1000000) {
                    return (value / 1000000).toFixed(1) + 'M';
                  } else if (value >= 1000) {
                    return (value / 1000).toFixed(0) + 'K';
                  }
                  return value;
                }
              },
              title: {
                display: true,
                text: 'Doanh thu (₫)'
              }
            },
            x: { grid: { display: false } },
          },
        },
      });
    })
    .catch((err) => console.error("Error loading order trends:", err));
}

// Period Toggle for Order Trends (7D, 30D, 90D buttons)
function initTrendsPeriodToggle() {
  const btn7d = document.getElementById("trends7d");
  const btn30d = document.getElementById("trends30d");
  const btn90d = document.getElementById("trends90d");

  if (btn7d) btn7d.addEventListener("change", () => initOrderTrendsChart(7));
  if (btn30d) btn30d.addEventListener("change", () => initOrderTrendsChart(30));
  if (btn90d) btn90d.addEventListener("change", () => initOrderTrendsChart(90));
}