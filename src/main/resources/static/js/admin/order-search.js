/**
 * AJAX Search và Filter Orders
 * Tìm kiếm và lọc đơn hàng không reload trang
 */
(function() {
  // Elements
  const searchInput = document.getElementById('searchInput');
  const statusFilter = document.getElementById('statusFilter');
  const paymentMethodFilter = document.getElementById('paymentMethodFilter');
  const periodFilter = document.getElementById('periodFilter');
  const tableBody = document.getElementById('orderTableBody');
  const resultInfo = document.getElementById('resultInfo');
  const paginationContainer = document.getElementById('paginationContainer');
  
  if (!searchInput || !tableBody) {
    console.error('Required elements not found');
    return;
  }
  
  console.log('Order AJAX Search initialized!');
  
  let debounceTimer;
  let currentPage = 1;
  
  // Debounce function
  function debounce(func, delay) {
    return function(...args) {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => func.apply(this, args), delay);
    };
  }
  
  // Search function
  async function searchOrders() {
    const keyword = searchInput.value.trim();
    const status = statusFilter ? statusFilter.value : '';
    const paymentMethod = paymentMethodFilter ? paymentMethodFilter.value : '';
    const period = periodFilter ? periodFilter.value : '';
    
    const params = new URLSearchParams();
    if (keyword) params.append('keyword', keyword);
    if (status) params.append('status', status);
    if (paymentMethod) params.append('paymentMethod', paymentMethod);
    if (period) params.append('period', period);
    params.append('page', currentPage);
    
    // Show loading
    tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-4"><div class="spinner-border spinner-border-sm text-primary"></div> Đang tìm kiếm...</td></tr>';
    
    try {
      const response = await fetch('/admin/orders/api/search?' + params.toString());
      const data = await response.json();
      
      renderOrders(data.content);
      if (resultInfo) {
        resultInfo.innerHTML = 'Hiển thị <strong>' + data.numberOfElements + '</strong> trên tổng số <strong>' + data.totalElements +' </strong> đơn hàng';
      }
      renderPagination(data.number + 1, data.totalPages);
      
      // Update URL
      const newUrl = params.toString() ? window.location.pathname + '?' + params.toString() : window.location.pathname;
      window.history.replaceState({}, '', newUrl);
      
    } catch (error) {
      console.error('Search error:', error);
      tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-danger">Có lỗi xảy ra</td></tr>';
    }
  }
  
  // Helper function to format date
  function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${day}/${month}/${year}<br/><small class="text-muted">${hours}:${minutes}</small>`;
  }
  
  // Format currency
  function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount);
  }
  
  // Get status badge
  function getStatusBadge(status) {
    const statusMap = {
      'PENDING': { class: 'bg-warning-subtle text-warning', icon: 'bi-clock', text: 'Chờ xác nhận' },
      'CONFIRMED': { class: 'bg-info-subtle text-info', icon: 'bi-check-circle', text: 'Đã xác nhận' },
      'PROCESSING': { class: 'bg-primary-subtle text-primary', icon: 'bi-arrow-repeat', text: 'Đang xử lý' },
      'SHIPPED': { class: 'bg-secondary-subtle text-secondary', icon: 'bi-truck', text: 'Đang giao hàng' },
      'DELIVERED': { class: 'bg-success-subtle text-success', icon: 'bi-box-seam', text: 'Đã giao hàng' },
      'COMPLETED': { class: 'bg-success-subtle text-success', icon: 'bi-check-circle-fill', text: 'Hoàn thành' },
      'CANCELLED': { class: 'bg-danger-subtle text-danger', icon: 'bi-x-circle', text: 'Đã hủy' }
    };
    const statusInfo = statusMap[status] || { class: 'bg-secondary', icon: 'bi-question-circle', text: status };
    return `<span class="badge ${statusInfo.class}"><i class="bi ${statusInfo.icon} me-1"></i>${statusInfo.text}</span>`;
  }
  
  // Get payment method badge
  function getPaymentMethodBadge(method) {
    if (method === 'COD') {
      return '<span class="badge bg-warning-subtle text-warning"><i class="bi bi-cash me-1"></i>Thanh toán khi nhận hàng</span>';
    } else if (method === 'BANK_TRANSFER') {
      return '<span class="badge bg-info-subtle text-info"><i class="bi bi-bank me-1"></i>Chuyển khoản ngân hàng</span>';
    }
    return method;
  }
  
  // Render orders
  function renderOrders(orders) {
    if (!orders || orders.length === 0) {
      tableBody.innerHTML = `
        <tr>
          <td colspan="7" class="text-center py-5">
            <div class="text-muted">
              <i class="bi bi-inbox display-4 d-block mb-3 opacity-50"></i>
              <p class="mb-0">Không tìm thấy đơn hàng nào</p>
            </div>
          </td>
        </tr>
      `;
      return;
    }
    
    tableBody.innerHTML = orders.map(order => `
      <tr>
        <td class="ps-4 fw-semibold text-muted">#${order.id}</td>
        <td>
          <div class="d-flex align-items-center">
            <div class="me-3">
              <div class="bg-primary bg-opacity-10 rounded-circle d-flex align-items-center justify-content-center" style="width: 40px; height: 40px">
                <i class="bi bi-person-fill text-primary"></i>
              </div>
            </div>
            <div>
              <div class="fw-bold">${order.userFullName}</div>
              <small class="text-muted">${order.userEmail}</small>
            </div>
          </div>
        </td>
        <td class="fw-bold text-success">${formatCurrency(order.totalAmount)} ₫</td>
        <td>${getPaymentMethodBadge(order.paymentMethod)}</td>
        <td class="text-center">${getStatusBadge(order.orderStatus)}</td>
        <td>${formatDate(order.createdAt)}</td>
        <td class="text-center">
          <div class="dropdown">
            <button class="btn btn-sm btn-light border-0 rounded-circle d-inline-flex align-items-center justify-content-center" style="width: 34px; height: 34px" type="button" data-bs-toggle="dropdown">
              <i class="bi bi-three-dots-vertical"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 py-2" style="min-width: 160px">
              <li>
                <a class="dropdown-item d-flex align-items-center py-2 px-3" href="/admin/orders/${order.id}">
                  <i class="bi bi-eye text-info me-2"></i>Xem chi tiết
                </a>
              </li>
              <li>
                <a class="dropdown-item d-flex align-items-center py-2 px-3" href="/admin/orders/edit/${order.id}">
                  <i class="bi bi-pencil-square text-primary me-2"></i>Chỉnh sửa
                </a>
              </li>
              <li><hr class="dropdown-divider my-1" /></li>
              <li>
                <a class="dropdown-item d-flex align-items-center py-2 px-3 text-danger" href="/admin/orders/delete/${order.id}" onclick="return confirm('Bạn có chắc chắn muốn xóa?')">
                  <i class="bi bi-trash3 me-2"></i>Xóa
                </a>
              </li>
            </ul>
          </div>
        </td>
      </tr>
    `).join('');
  }
  
  // Render pagination
  function renderPagination(current, total) {
    if (!paginationContainer || total <= 1) {
      if (paginationContainer) paginationContainer.innerHTML = '';
      return;
    }
    
    let html = '<ul class="pagination pagination-sm mb-0">';
    
    // Previous button
    html += `
      <li class="page-item ${current === 1 ? 'disabled' : ''}">
        <a class="page-link" href="#" data-page="${current - 1}">Trước</a>
      </li>
    `;
    
    // Page numbers
    for (let i = 1; i <= total; i++) {
      html += `
        <li class="page-item ${i === current ? 'active' : ''}">
          <a class="page-link" href="#" data-page="${i}">${i}</a>
        </li>
      `;
    }
    
    // Next button
    html += `
      <li class="page-item ${current === total ? ' disabled' : ''}">
        <a class="page-link" href="#" data-page="${current + 1}">Sau</a>
      </li>
    `;
    
    html += '</ul>';
    paginationContainer.innerHTML = html;
  }
  
  // Event listeners
  searchInput.addEventListener('input', debounce(() => {
    currentPage = 1;
    searchOrders();
  }, 500));
  
  if (statusFilter) {
    statusFilter.addEventListener('change', () => {
      currentPage = 1;
      searchOrders();
    });
  }
  
  if (paymentMethodFilter) {
    paymentMethodFilter.addEventListener('change', () => {
      currentPage = 1;
      searchOrders();
    });
  }
  
  if (periodFilter) {
    periodFilter.addEventListener('change', () => {
      currentPage = 1;
      searchOrders();
    });
  }
  
  // Pagination clicks
  if (paginationContainer) {
    paginationContainer.addEventListener('click', (e) => {
      if (e.target.tagName === 'A' && e.target.dataset.page) {
        e.preventDefault();
        currentPage = parseInt(e.target.dataset.page);
        searchOrders();
      }
    });
  }
  
})();
