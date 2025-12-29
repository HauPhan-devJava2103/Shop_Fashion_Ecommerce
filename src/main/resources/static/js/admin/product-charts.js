// Product Category Distribution Pie Chart
document.addEventListener("DOMContentLoaded", function () {
  console.log("Product charts script loaded");
  
  const chartCanvas = document.getElementById("categoryPieChart");
  
  if (!chartCanvas) {
    console.error("Canvas element 'categoryPieChart' not found!");
    return;
  }
  
  console.log("Canvas element found, fetching data...");

  // Fetch category distribution data
  fetch("/admin/products/api/stats/category-distribution", {
    credentials: 'same-origin'  // Include cookies for authentication
  })
    .then((response) => {
      console.log("API Response status:", response.status);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then((data) => {
      console.log("Data received:", data);
      
      if (!data.labels || !data.data) {
        console.error("Invalid data format:", data);
        throw new Error("Invalid data format");
      }
      
      if (data.labels.length === 0) {
        chartCanvas.parentElement.innerHTML = 
          '<div class="text-center text-muted p-4"><i class="bi bi-inbox fs-1 d-block mb-2"></i><p>Chưa có dữ liệu danh mục</p></div>';
        return;
      }

      const ctx = chartCanvas.getContext("2d");

      // Generate vibrant colors for each category
      const colors = [
        "rgba(54, 162, 235, 0.8)",   // Blue
        "rgba(255, 99, 132, 0.8)",   // Red
        "rgba(255, 206, 86, 0.8)",   // Yellow
        "rgba(75, 192, 192, 0.8)",   // Teal
        "rgba(153, 102, 255, 0.8)",  // Purple
        "rgba(255, 159, 64, 0.8)",   // Orange
        "rgba(46, 204, 113, 0.8)",   // Green
        "rgba(231, 76, 60, 0.8)",    // Dark Red
        "rgba(52, 152, 219, 0.8)",   // Ocean Blue
        "rgba(241, 196, 15, 0.8)",   // Gold
      ];

      const borderColors = colors.map((color) => color.replace("0.8", "1"));

      console.log("Creating chart...");
      
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
                "#5a5c69",
                "#8e44ad",
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
                font: {
                  size: 12,
                  family: "'Inter', sans-serif",
                },
              },
            },
            tooltip: {
              backgroundColor: "rgba(0, 0, 0, 0.8)",
              padding: 12,
              titleFont: {
                size: 14,
                weight: "bold",
              },
              bodyFont: {
                size: 13,
              },
              callbacks: {
                label: function (context) {
                  const label = context.label || "";
                  const value = context.parsed || 0;
                  const total = context.dataset.data.reduce(
                    (a, b) => a + b,
                    0
                  );
                  const percentage = ((value / total) * 100).toFixed(1);
                  return label + ": " + value + " sản phẩm (" + percentage + "%)";
                },
              },
            },
          },
          cutout: "60%",
        },
      });
      
      console.log("Chart created successfully!");
    })
    .catch((error) => {
      console.error("Error loading chart data:", error);
      chartCanvas.parentElement.innerHTML =
        '<div class="text-center text-danger p-4"><i class="bi bi-exclamation-triangle fs-1 d-block mb-2"></i><p>Không thể tải dữ liệu biểu đồ</p><small>' + error.message + '</small></div>';
    });
});
