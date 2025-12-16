/**
 * User Analytics Chart Logic
 * Handles the User Growth Chart rendering and interactions
 */

let userGrowthChart = null;

/**
 * Fetches user growth data and renders the chart
 * @param {number} days - Number of days to look back
 */
function loadChart(days) {
  console.log("Fetching data for days:", days);
  fetch("/admin/users/api/user/growth?days=" + days)
    .then((response) => response.json())
    .then((data) => {
      console.log("Data received:", data);

      const canvas = document.getElementById("userGrowthChart");
      if (!canvas) {
        console.error("Canvas element 'userGrowthChart' not found");
        return;
      }

      const ctx = canvas.getContext("2d");

      if (userGrowthChart) {
        userGrowthChart.destroy();
      }

      // Handle null/empty data safely
      const labels = data && data.labels ? data.labels : [];
      const values = data && data.data ? data.data : [];

      userGrowthChart = new Chart(ctx, {
        type: "line",
        data: {
          labels: labels,
          datasets: [
            {
              label: "Người dùng mới",
              data: values,
              borderColor: "#6366f1",
              backgroundColor: "rgba(99, 102, 241, 0.1)",
              borderWidth: 2,
              fill: true,
              tension: 0.4,
              pointRadius: 4,
              pointHoverRadius: 6,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false },
            tooltip: {
                backgroundColor: "rgba(0,0,0,0.8)",
                padding: 12,
                titleFont: { size: 14 },
                bodyFont: { size: 13 },
            }
          },
          scales: {
            y: { beginAtZero: true, ticks: { stepSize: 1 } },
            x: { grid: { display: false } },
          },
        },
      });
    })
    .catch((err) => console.error("Error loading chart data:", err));
}

// Initialize when DOM is ready
document.addEventListener("DOMContentLoaded", function () {
  console.log("User Analytics Script loaded");
  
  // Load default 7D view
  loadChart(7);
  loadRoleChart();

  // Attach event listeners to filters
  const btn7d = document.getElementById("growth7d");
  const btn30d = document.getElementById("growth30d");
  const btn90d = document.getElementById("growth90d");

  if (btn7d) btn7d.addEventListener("change", () => loadChart(7));
  if (btn30d) btn30d.addEventListener("change", () => loadChart(30));
  if (btn90d) btn90d.addEventListener("change", () => loadChart(90));
});

/**
 * Loads and renders the Role Distribution Pie Chart
 */
function loadRoleChart() {
  fetch("/admin/users/api/user/roles")
    .then((response) => response.json())
    .then((data) => {
      const canvas = document.getElementById("roleDistributionChart");
      if (!canvas) return;

      const ctx = canvas.getContext("2d");

      new Chart(ctx, {
        type: "doughnut",
        data: {
          labels: data.labels,
          datasets: [
            {
              data: data.data,
              backgroundColor: [
                "#4e73df",
                "#1cc88a",
                "#36b9cc",
                "#f6c23e",
                "#e74a3b",
                "#858796",
              ],
              hoverOffset: 4,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: "bottom",
              labels: {
                usePointStyle: true,
                padding: 20,
              },
            },
          },
          cutout: "70%",
        },
      });
    })
    .catch((err) => console.error("Error loading role chart:", err));
}
