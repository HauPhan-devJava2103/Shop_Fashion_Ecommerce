/**
 * AJAX Search và Filter Users
 * Tìm kiếm và lọc không reload trang
 */
(function() {
  // Elements
  const searchInput = document.getElementById('searchInput');
  const statusFilter = document.getElementById('statusFilter');
  const roleFilter = document.getElementById('roleFilter');
  const tableBody = document.getElementById('userTableBody');
  const resultInfo = document.getElementById('resultInfo');
  const paginationContainer = document.getElementById('paginationContainer');
  
  if (!searchInput || !tableBody) {
    console.error('Required elements not found');
    return;
  }
  
  console.log('AJAX Search initialized!');
  
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
  async function searchUsers() {
    const keyword = searchInput.value.trim();
    const status = statusFilter ? statusFilter.value : '';
    const roleId = roleFilter ? roleFilter.value : '';
    
    const params = new URLSearchParams();
    if (keyword) params.append('keyword', keyword);
    if (status) params.append('status', status);
    if (roleId) params.append('roleId', roleId);
    params.append('page', currentPage);
    
    // Show loading
    tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-4"><div class="spinner-border spinner-border-sm text-primary"></div> Đang tìm kiếm...</td></tr>';
    
    try {
      const response = await fetch('/api/admin/users/search?' + params.toString());
      const data = await response.json();
      
      renderUsers(data.users);
      if (resultInfo) {
        resultInfo.innerHTML = 'Hiển thị <strong>' + data.numberOfElements + '</strong> trên tổng số <strong>' + data.totalElements + '</strong> người dùng';
      }
      renderPagination(data.currentPage, data.totalPages);
      
      // Update URL
      const newUrl = params.toString() ? window.location.pathname + '?' + params.toString() : window.location.pathname;
      window.history.replaceState({}, '', newUrl);
      
    } catch (error) {
      console.error('Search error:', error);
      tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-danger">Có lỗi xảy ra</td></tr>';
    }
  }
  
  // Render users
  function renderUsers(users) {
    if (!users || users.length === 0) {
      tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-5"><div class="text-muted"><i class="bi bi-inbox display-4 d-block mb-3"></i><p class="mb-0">Không tìm thấy người dùng nào</p></div></td></tr>';
      return;
    }
    
    let html = '';
    users.forEach(function(user) {
      const roleBadge = user.roleName === 'ADMIN' ? 'bg-danger' : (user.roleName === 'STAFF' ? 'bg-warning text-dark' : 'bg-info');
      const statusBadge = user.isActive 
        ? '<span class="badge bg-success-subtle text-success"><i class="bi bi-check-circle me-1"></i>Hoạt động</span>'
        : '<span class="badge bg-secondary-subtle text-secondary"><i class="bi bi-x-circle me-1"></i>Vô hiệu</span>';
      
      html += '<tr>' +
        '<td class="ps-4 fw-semibold text-muted">' + user.id + '</td>' +
        '<td><div class="d-flex align-items-center">' +
          '<div class="avatar-sm bg-primary bg-opacity-10 rounded-circle d-flex align-items-center justify-content-center me-3" style="width: 40px; height: 40px"><i class="bi bi-person text-primary"></i></div>' +
          '<div><div class="fw-semibold">' + user.fullName + '</div><small class="text-muted">' + (user.gender || '') + '</small></div>' +
        '</div></td>' +
        '<td><i class="bi bi-envelope text-muted me-1"></i>' + user.email + '</td>' +
        '<td><i class="bi bi-telephone text-muted me-1"></i>' + user.phone + '</td>' +
        '<td><span class="badge rounded-pill ' + roleBadge + '">' + (user.roleDisplayName || 'N/A') + '</span></td>' +
        '<td class="text-center">' + statusBadge + '</td>' +
        '<td class="text-center">' +
          '<div class="dropdown">' +
            '<button class="btn btn-sm btn-light border-0 rounded-circle" style="width: 34px; height: 34px" data-bs-toggle="dropdown"><i class="bi bi-three-dots-vertical"></i></button>' +
            '<ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 py-2">' +
              '<li><a class="dropdown-item py-2 px-3" href="/admin/users/view/' + user.id + '"><i class="bi bi-eye text-info me-2"></i>Xem chi tiết</a></li>' +
              '<li><a class="dropdown-item py-2 px-3" href="/admin/users/edit/' + user.id + '"><i class="bi bi-pencil-square text-primary me-2"></i>Chỉnh sửa</a></li>' +
              '<li><hr class="dropdown-divider my-1"></li>' +
              '<li><a class="dropdown-item py-2 px-3 text-danger" href="/admin/users/delete/' + user.id + '" onclick="return confirm(\'Bạn có chắc chắn muốn xóa?\')"><i class="bi bi-trash3 me-2"></i>Xóa</a></li>' +
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
          searchUsers();
        }
      });
    });
  }
  
  // Event listeners
  const debouncedSearch = debounce(function() {
    currentPage = 1;
    searchUsers();
  }, 300);
  
  searchInput.addEventListener('input', debouncedSearch);
  
  searchInput.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
      e.preventDefault();
      currentPage = 1;
      searchUsers();
    }
  });
  
  if (statusFilter) {
    statusFilter.addEventListener('change', function() {
      currentPage = 1;
      searchUsers();
    });
  }
  
  if (roleFilter) {
    roleFilter.addEventListener('change', function() {
      currentPage = 1;
      searchUsers();
    });
  }
})();
