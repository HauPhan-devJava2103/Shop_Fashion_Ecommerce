/**
 * Category Analytics - Performance Chart
 * Horizontal Bar Chart showing product count per category
 */

let categoryPerformanceChart = null;

/**
 * Load và render Category Performance Chart
 * @param {number} limit - Số lượng categories hiển thị (default: 5)
 */
function loadCategoryPerformanceChart(limit = 5) {
  console.log("Loading Category Performance Chart with limit:", limit);
  
  fetch(`/admin/categories/api/stats/performance?limit=${limit}`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to fetch category performance data");
      }
      return response.json();
    })
    .then((data) => {
      console.log("Category Performance Data:", data);
      renderCategoryPerformanceChart(data);
    })
    .catch((error) => {
      console.error("Error loading category performance chart:", error);
    });
}

/**
 * Render chart với data đã fetch
 */
function renderCategoryPerformanceChart(data) {
  const canvas = document.getElementById("categoryPerformanceChart");
  if (!canvas) {
    console.error("Canvas element 'categoryPerformanceChart' not found");
    return;
  }

  const ctx = canvas.getContext("2d");

  // Destroy existing chart nếu có
  if (categoryPerformanceChart) {
    categoryPerformanceChart.destroy();
  }

  // Tạo màu gradient cho mỗi bar
  const colors = [
    'rgba(99, 102, 241, 0.8)',   // Indigo
    'rgba(236, 72, 153, 0.8)',   // Pink
    'rgba(59, 130, 246, 0.8)',   // Blue
    'rgba(245, 158, 11, 0.8)',   // Amber
    'rgba(16, 185, 129, 0.8)',   // Emerald
    'rgba(139, 92, 246, 0.8)',   // Purple
    'rgba(239, 68, 68, 0.8)',    // Red
    'rgba(6, 182, 212, 0.8)',    // Cyan
    'rgba(251, 146, 60, 0.8)',   // Orange
    'rgba(107, 114, 128, 0.8)'   // Gray
  ];

  categoryPerformanceChart = new Chart(ctx, {
    type: "bar",
    data: {
      labels: data.labels || [],
      datasets: [
        {
          label: "Số lượng sản phẩm",
          data: data.data || [],
          backgroundColor: colors,
          borderColor: colors.map(color => color.replace('0.8', '1')),
          borderWidth: 1,
          borderRadius: 6,
          barThickness: 30,
        },
      ],
    },
    options: {
      indexAxis: 'y', // Horizontal bar chart
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          backgroundColor: "rgba(0, 0, 0, 0.8)",
          padding: 12,
          titleFont: { size: 14, weight: 'bold' },
          bodyFont: { size: 13 },
          callbacks: {
            label: function(context) {
              return context.dataset.label + ': ' + context.parsed.x + ' sản phẩm';
            }
          }
        },
      },
      scales: {
        x: {
          beginAtZero: true,
          ticks: {
            stepSize: 1,
            font: { size: 11 }
          },
          grid: {
            color: 'rgba(0, 0, 0, 0.05)'
          },
          title: {
            display: true,
            text: 'Số lượng sản phẩm',
            font: { size: 12, weight: 'bold' }
          }
        },
        y: {
          grid: {
            display: false
          },
          ticks: {
            font: { size: 12 }
          }
        },
      },
    },
  });
}

// Initialize khi DOM ready
document.addEventListener("DOMContentLoaded", function () {
  console.log("Category Analytics Script loaded");
  
  // Load chart với limit mặc định (5)
  loadCategoryPerformanceChart(5);
  
  // Attach event listeners to limit filter buttons
  const limitButtons = document.querySelectorAll('input[name="chartLimit"]');
  limitButtons.forEach(button => {
    button.addEventListener('change', function() {
      if (this.checked) {
        const limit = parseInt(this.value);
        console.log(`Loading chart with limit: ${limit}`);
        loadCategoryPerformanceChart(limit);
      }
    });
  });
});
