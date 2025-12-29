/**
 * AJAX Search và Filter Products
 * Tìm kiếm và lọc sản phẩm không reload trang
 */
(function() {
  // Elements
  const searchInput = document.getElementById('searchInput');
  const categoryFilter = document.getElementById('categoryFilter');
  const stockFilter = document.getElementById('stockFilter');
  const statusFilter = document.getElementById('statusFilter');
  const tableBody = document.getElementById('productTableBody');
  const resultInfo = document.getElementById('resultInfo');
  const paginationContainer = document.getElementById('paginationContainer');
  
  if (!searchInput || !tableBody) {
    console.error('Required elements not found');
    return;
  }
  
  console.log('Product AJAX Search initialized!');
  
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
  async function searchProducts() {
    const keyword = searchInput.value.trim();
    const categoryId = categoryFilter ? categoryFilter.value : '';
    const stock = stockFilter ? stockFilter.value : '';
    const isActive = statusFilter ? statusFilter.value : '';
    
    const params = new URLSearchParams();
    if (keyword) params.append('keyword', keyword);
    if (categoryId) params.append('categoryId', categoryId);
    if (stock) params.append('stock', stock);
    if (isActive) params.append('isActive', isActive);
    params.append('page', currentPage);
    
    // Show loading
    tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-4"><div class="spinner-border spinner-border-sm text-primary"></div> Đang tìm kiếm...</td></tr>';
    
    try {
      const response = await fetch('/admin/products/api/search?' + params.toString());
      const data = await response.json();
      
      renderProducts(data.content);
      if (resultInfo) {
        resultInfo.innerHTML = 'Hiển thị <strong>' + data.numberOfElements + '</strong> trong tổng số <strong>' + data.totalElements +' </strong> sản phẩm';
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
  
  // Format currency
  function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount);
  }
  
  // Get stock badge
  function getStockBadge(stock) {
    if (stock > 0) {
      return `<span class="badge bg-success">${stock} sp</span>`;
    } else {
      return '<span class="badge bg-danger">Hết hàng</span>';
    }
  }
  
  // Render products
  function renderProducts(products) {
    if (!products || products.length === 0) {
      tableBody.innerHTML = `
        <tr>
          <td colspan="7" class="text-center py-5">
            <div class="text-muted">
              <i class="bi bi-inbox display-4 d-block mb-3 opacity-50"></i>
              <p class="mb-0">Không tìm thấy sản phẩm nào</p>
            </div>
          </td>
        </tr>
      `;
      return;
    }
    
    tableBody.innerHTML = products.map(product => `
      <tr>
        <td class="ps-4">
          <img src="${product.mainImageUrl}" alt="${product.productName}" class="rounded" style="width: 50px; height: 50px; object-fit: cover" />
        </td>
        <td>
          <div class="fw-semibold">${product.productName}</div>
          <small class="text-muted">
            <i class="bi bi-upc-scan me-1"></i>
            <code>${product.sku}</code>
          </small>
        </td>
        <td>
          <span class="badge bg-info-subtle text-info">${product.categoryName || 'N/A'}</span>
        </td>
        <td>
          <span class="fw-semibold text-success">${formatCurrency(product.price)} ₫</span>
        </td>
        <td class="text-center">
          ${getStockBadge(product.stock)}
        </td>
        <td class="text-center">
          ${product.isActive 
            ? '<span class="badge bg-success"><i class="bi bi-check-circle me-1"></i>Hoạt động</span>'
            : '<span class="badge bg-danger"><i class="bi bi-x-circle me-1"></i>Ngưng bán</span>'}
        </td>
        <td class="text-center">
          <div class="dropdown">
            <button class="btn btn-sm btn-light border-0 rounded-circle d-inline-flex align-items-center justify-content-center" style="width: 34px; height: 34px" type="button" data-bs-toggle="dropdown">
              <i class="bi bi-three-dots-vertical"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 py-2" style="min-width: 160px">
              <li>
                <a class="dropdown-item d-flex align-items-center py-2 px-3" href="/admin/products/${product.id}">
                  <i class="bi bi-eye text-info me-2"></i>Xem chi tiết
                </a>
              </li>
              <li>
                <a class="dropdown-item d-flex align-items-center py-2 px-3" href="/admin/products/edit/${product.id}">
                  <i class="bi bi-pencil-square text-primary me-2"></i>Chỉnh sửa
                </a>
              </li>
              <li><hr class="dropdown-divider my-1" /></li>
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
    
    let html = '<ul class="pagination justify-content-end mb-0">';
    
    // Previous button
    html += `
      <li class="page-item ${current === 1 ? 'disabled' : ''}">
        <a class="page-link" href="#" data-page="${current - 1}">
          <i class="bi bi-chevron-left"></i>
        </a>
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
      <li class="page-item ${current === total ? 'disabled' : ''}">
        <a class="page-link" href="#" data-page="${current + 1}">
          <i class="bi bi-chevron-right"></i>
        </a>
      </li>
    `;
    
    html += '</ul>';
    paginationContainer.innerHTML = html;
  }
  
  // Event listeners
  searchInput.addEventListener('input', debounce(() => {
    currentPage = 1;
    searchProducts();
  }, 500));
  
  if (categoryFilter) {
    categoryFilter.addEventListener('change', () => {
      currentPage = 1;
      searchProducts();
    });
  }
  
  if (stockFilter) {
    stockFilter.addEventListener('change', () => {
      currentPage = 1;
      searchProducts();
    });
  }
  
  if (statusFilter) {
    statusFilter.addEventListener('change', () => {
      currentPage = 1;
      searchProducts();
    });
  }
  
  // Pagination clicks
  if (paginationContainer) {
    paginationContainer.addEventListener('click', (e) => {
      if (e.target.tagName === 'A' && e.target.dataset.page) {
        e.preventDefault();
        currentPage = parseInt(e.target.dataset.page);
        searchProducts();
      }
    });
  }
  
})();
