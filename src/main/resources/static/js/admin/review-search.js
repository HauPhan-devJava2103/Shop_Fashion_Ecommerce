/**
 * AJAX Search và Filter Reviews
 * Tìm kiếm và lọc không reload trang
 */
(function() {
  // Elements
  const searchInput = document.getElementById('searchInput');
  const statusFilter = document.getElementById('statusFilter');
  const ratingFilter = document.getElementById('ratingFilter');
  const tableBody = document.getElementById('reviewTableBody');
  const resultInfo = document.getElementById('resultInfo');
  const paginationContainer = document.getElementById('paginationContainer');
  
  if (!searchInput || !tableBody) {
    console.error('Required elements not found');
    return;
  }
  
  console.log('Review AJAX Search initialized!');
  
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
  async function searchReviews() {
    const keyword = searchInput.value.trim();
    const status = statusFilter ? statusFilter.value : '';
    const rating = ratingFilter ? ratingFilter.value : '';
    
    const params = new URLSearchParams();
    if (keyword) params.append('keyword', keyword);
    if (status) params.append('status', status);
    if (rating) params.append('rating', rating);
    params.append('page', currentPage);
    
    // Show loading
    tableBody.innerHTML = '<tr><td colspan="8" class="text-center py-4"><div class="spinner-border spinner-border-sm text-primary"></div> Đang tìm kiếm...</td></tr>';
    
    try {
      const response = await fetch('/admin/reviews/api/search?' + params.toString());
      const data = await response.json();
      
      renderReviews(data.content);
      if (resultInfo) {
        resultInfo.innerHTML = 'Hiển thị <strong>' + data.numberOfElements + '</strong> trên tổng số <strong>' + data.totalElements + '</strong> đánh giá';
      }
      renderPagination(data.number + 1, data.totalPages);
      
      // Update URL
      const newUrl = params.toString() ? window.location.pathname + '?' + params.toString() : window.location.pathname;
      window.history.replaceState({}, '', newUrl);
      
    } catch (error) {
      console.error('Search error:', error);
      tableBody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-danger">Có lỗi xảy ra</td></tr>';
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
    return `${day}/${month}/${year} ${hours}:${minutes}`;
  }
  
  // Render reviews
  function renderReviews(reviews) {
    if (!reviews || reviews.length === 0) {
      tableBody.innerHTML = '<tr><td colspan="8" class="text-center py-5"><div class="text-muted"><i class="bi bi-inbox display-4 d-block mb-3"></i><p class="mb-0">Chưa có đánh giá nào</p></div></td></tr>';
      return;
    }
    
    let html = '';
    reviews.forEach(function(review) {
      const statusBadge = review.isApproved 
        ? '<span class="badge bg-success-subtle text-success"><i class="bi bi-check-circle me-1"></i>Đã duyệt</span>'
        : '<span class="badge bg-warning-subtle text-warning"><i class="bi bi-clock me-1"></i>Chờ duyệt</span>';
      
      const productName = review.productName || 'N/A';
      const productSku = review.productSku || 'N/A';
      const userName = review.userFullName || 'N/A';
      const userEmail = review.userEmail || '';
      
      // Render review image or placeholder
      const reviewImageHtml = review.imageUrl 
        ? '<div class="mt-2"><img src="' + review.imageUrl + '" alt="Review image" class="img-thumbnail" style="max-width: 100px; max-height: 100px; object-fit: cover; cursor: pointer" onclick="window.open(this.src, \'_blank\')" title="Click để xem ảnh đầy đủ"></div>'
        : '';
      
      html += '<tr>' +
        '<td class="ps-4 fw-semibold text-muted">' + review.id + '</td>' +
        '<td><div><div class="fw-semibold">' + productName + '</div><small class="text-muted">SKU: ' + productSku + '</small></div></td>' +
        '<td><div><div class="fw-semibold">' + userName + '</div><small class="text-muted">' + userEmail + '</small></div></td>' +
        '<td><div class="rating-stars"><span class="text-warning fw-bold">' + review.rating + ' ★</span></div></td>' +
        '<td><div class="text-truncate" style="max-width: 300px" title="' + (review.comment || '') + '">' + (review.comment || '') + '</div>' + reviewImageHtml + '</td>' +
        '<td class="text-center">' + statusBadge + '</td>' +
        '<td><small class="text-muted">' + formatDate(review.createdAt) + '</small></td>' +
        '<td class="text-center">' +
          '<div class="dropdown">' +
            '<button class="btn btn-sm btn-light border-0 rounded-circle d-inline-flex align-items-center justify-content-center" style="width: 34px; height: 34px" type="button" data-bs-toggle="dropdown"><i class="bi bi-three-dots-vertical"></i></button>' +
            '<ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 py-2" style="min-width: 160px">' +
              '<li><a class="dropdown-item d-flex align-items-center py-2 px-3" href="/admin/reviews/view/' + review.id + '"><i class="bi bi-eye text-info me-2"></i>Xem chi tiết</a></li>' +
              (review.isApproved 
                ? '<li><a class="dropdown-item d-flex align-items-center py-2 px-3" href="/admin/reviews/reject/' + review.id + '" onclick="return confirm(\'Bạn có chắc chắn muốn từ chối đánh giá này?\')"><i class="bi bi-x-circle text-warning me-2"></i>Từ chối</a></li>'
                : '<li><a class="dropdown-item d-flex align-items-center py-2 px-3" href="/admin/reviews/approve/' + review.id + '" onclick="return confirm(\'Bạn có chắc chắn muốn duyệt đánh giá này?\')"><i class="bi bi-check-circle text-success me-2"></i>Duyệt</a></li>'
              ) +
              '<li><hr class="dropdown-divider my-1"></li>' +
              '<li><a class="dropdown-item d-flex align-items-center py-2 px-3 text-danger" href="/admin/reviews/delete/' + review.id + '" onclick="return confirm(\'Bạn có chắc chắn muốn xóa đánh giá này?\')"><i class="bi bi-trash3 me-2"></i>Xóa</a></li>' +
            '</ul>' +
          '</div>' +
        '</td>' +
      '</tr>';
    });
    tableBody.innerHTML = html;
  }
  
  // Render pagination
  function renderPagination(current, totalPages) {
    if (!paginationContainer || totalPages <= 1) {
      if (paginationContainer) paginationContainer.innerHTML = '';
      return;
    }
    
    let html = '<nav><ul class="pagination pagination-sm mb-0">';
    html += '<li class="page-item ' + (current <= 1 ? 'disabled' : '') + '"><a class="page-link" href="#" data-page="' + (current - 1) + '">Trước</a></li>';
    for (let i = 1; i <= totalPages; i++) {
      html += '<li class="page-item ' + (current === i ? 'active' : '') + '"><a class="page-link" href="#" data-page="' + i + '">' + i + '</a></li>';
    }
    html += '<li class="page-item ' + (current >= totalPages ? 'disabled' : '') + '"><a class="page-link" href="#" data-page="' + (current + 1) + '">Sau</a></li>';
    html += '</ul></nav>';
    
    paginationContainer.innerHTML = html;
    
    // Add click handlers
    paginationContainer.querySelectorAll('.page-link').forEach(function(link) {
      link.addEventListener('click', function(e) {
        e.preventDefault();
        const page = parseInt(this.dataset.page);
        if (page >= 1 && page <= totalPages) {
          currentPage = page;
          searchReviews();
        }
      });
    });
  }
  
  // Event listeners
  const debouncedSearch = debounce(function() {
    currentPage = 1;
    searchReviews();
  }, 300);
  
  searchInput.addEventListener('input', debouncedSearch);
  
  searchInput.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
      e.preventDefault();
      currentPage = 1;
      searchReviews();
    }
  });
  
  if (statusFilter) {
    statusFilter.addEventListener('change', function() {
      currentPage = 1;
      searchReviews();
    });
  }
  
  if (ratingFilter) {
    ratingFilter.addEventListener('change', function() {
      currentPage = 1;
      searchReviews();
    });
  }
})();
