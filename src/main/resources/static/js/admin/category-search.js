(function () {
  "use strict";

  // DOM Elements
  const searchForm = document.querySelector('form[action*="/admin/categories"]');
  const tableBody = document.querySelector("#categoriesTable tbody");
  const paginationContainer = document.querySelector(".pagination");
  const resultsInfo = document.querySelector(".card-footer small");

  if (!searchForm || !tableBody) return;

  // Prevent default form submission
  searchForm.addEventListener("submit", function (e) {
    e.preventDefault();
    performSearch(1); // Reset to page 1 on new search
  });

  // Handle keyword input changes with debounce
  const keywordInput = searchForm.querySelector('input[name="keyword"]');
  if (keywordInput) {
    let debounceTimer;
    keywordInput.addEventListener("input", function () {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        performSearch(1);
      }, 500); // Wait 500ms after user stops typing
    });
  }

  // Handle status filter change
  const statusSelect = searchForm.querySelector('select[name="status"]');
  if (statusSelect) {
    statusSelect.addEventListener("change", function () {
      performSearch(1);
    });
  }

  // Handle parent category filter change
  const parentSlugSelect = searchForm.querySelector('select[name="parentSlug"]');
  if (parentSlugSelect) {
    parentSlugSelect.addEventListener("change", function () {
      performSearch(1);
    });
  }

  /**
   * Perform AJAX search
   */
  function performSearch(page = 1) {
    // Get form data
    const formData = new FormData(searchForm);
    const keyword = formData.get("keyword") || "";
    const status = formData.get("status") || "";
    const parentSlug = formData.get("parentSlug") || "";

    // Build URL parameters
    const params = new URLSearchParams({
      page: page,
      keyword: keyword,
      status: status,
      parentSlug: parentSlug,
    });

    // Show loading state
    showLoading();

    // Fetch data
    fetch(`/admin/categories/api/search?${params.toString()}`, {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Network response was not ok");
        }
        return response.json();
      })
      .then((data) => {
        updateTable(data);
        updatePagination(data, keyword, status, parentSlug);
        updateResultsInfo(data);
        hideLoading();
      })
      .catch((error) => {
        console.error("Error:", error);
        showError("Có lỗi xảy ra khi tải dữ liệu");
        hideLoading();
      });
  }

  /**
   * Update table with new data
   */
  function updateTable(data) {
    if (!data.content || data.content.length === 0) {
      tableBody.innerHTML = `
        <tr>
          <td colspan="6" class="text-center py-5">
            <div class="text-muted">
              <i class="bi bi-folder2-open display-4 d-block mb-3 opacity-50"></i>
              <p class="mb-0">Không tìm thấy danh mục nào</p>
            </div>
          </td>
        </tr>
      `;
      return;
    }

    tableBody.innerHTML = data.content
      .map((category) => createCategoryRow(category))
      .join("");
  }

  /**
   * Create category table row HTML
   */
  function createCategoryRow(category) {
    // Image or placeholder
    const imageHTML = category.imageUrl
      ? `<img src="${category.imageUrl}" alt="Category image" class="rounded" style="width: 50px; height: 50px; object-fit: cover;">`
      : `<div class="bg-primary bg-opacity-10 rounded d-flex align-items-center justify-content-center" style="width: 50px; height: 50px">
           <i class="bi bi-folder-fill text-primary fs-5"></i>
         </div>`;

    // Products count - products array is ignored in JSON, so will be 0 or undefined
    const productsCount = 0; // Since @JsonIgnore on products field

    // Parent category badge
    const parentBadge = category.parentCategory
      ? `<span class="badge bg-info-subtle text-info">${category.parentCategory.categoryName}</span>`
      : `<span class="text-muted small"><i class="bi bi-dash-circle"></i> Root</span>`;

    // Status badge
    const statusBadge = category.isActive
      ? `<span class="badge bg-success-subtle text-success"><i class="bi bi-check-circle me-1"></i>Hoạt động</span>`
      : `<span class="badge bg-secondary-subtle text-secondary"><i class="bi bi-x-circle me-1"></i>Vô hiệu</span>`;

    return `
      <tr>
        <td class="ps-4 fw-semibold text-muted">${category.id}</td>
        <td>
          <div class="d-flex align-items-center">
            <div class="me-3">
              ${imageHTML}
            </div>
            <div>
              <div class="fw-bold">${category.categoryName}</div>
              <small class="text-muted">
                <i class="bi bi-box-seam me-1"></i>${productsCount} sản phẩm
              </small>
            </div>
          </div>
        </td>
        <td>
          <code class="text-muted small">${category.slug}</code>
        </td>
        <td>${parentBadge}</td>
        <td class="text-center">${statusBadge}</td>
        <td class="text-center">
          <div class="dropdown">
            <button class="btn btn-sm btn-light border-0 rounded-circle d-inline-flex align-items-center justify-content-center" 
                    style="width: 34px; height: 34px" type="button" data-bs-toggle="dropdown">
              <i class="bi bi-three-dots-vertical"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 py-2" style="min-width: 160px">
              <li><a class="dropdown-item d-flex align-items-center py-2 px-3" href="/admin/categories/view/${category.id}">
                <i class="bi bi-eye text-info me-2"></i>Xem chi tiết</a></li>
              <li><a class="dropdown-item d-flex align-items-center py-2 px-3" href="/admin/categories/edit/${category.id}">
                <i class="bi bi-pencil-square text-primary me-2"></i>Chỉnh sửa</a></li>
              <li><hr class="dropdown-divider my-1" /></li>
              <li><a class="dropdown-item d-flex align-items-center py-2 px-3 text-danger" 
                     href="/admin/categories/delete/${category.id}" 
                     onclick="return confirm('Bạn có chắc chắn muốn xóa danh mục này?')">
                <i class="bi bi-trash3 me-2"></i>Xóa</a></li>
            </ul>
          </div>
        </td>
      </tr>
    `;
  }

  /**
   * Update pagination
   */
  function updatePagination(data, keyword, status, parentSlug) {
    if (!paginationContainer || data.totalPages <= 1) {
      if (paginationContainer) paginationContainer.innerHTML = "";
      return;
    }

    const currentPage = data.number + 1; // Spring Data JPA uses 0-based index
    const totalPages = data.totalPages;

    let paginationHTML = `
      <li class="page-item ${currentPage === 1 ? "disabled" : ""}">
        <a class="page-link" href="#" data-page="${currentPage - 1}">Trước</a>
      </li>
    `;

    for (let i = 1; i <= totalPages; i++) {
      paginationHTML += `
        <li class="page-item ${currentPage === i ? "active" : ""}">
          <a class="page-link" href="#" data-page="${i}">${i}</a>
        </li>
      `;
    }

    paginationHTML += `
      <li class="page-item ${currentPage === totalPages ? "disabled" : ""}">
        <a class="page-link" href="#" data-page="${currentPage + 1}">Sau</a>
      </li>
    `;

    paginationContainer.innerHTML = paginationHTML;

    // Add click handlers to pagination links
    paginationContainer.querySelectorAll(".page-link").forEach((link) => {
      link.addEventListener("click", function (e) {
        e.preventDefault();
        if (
          !this.parentElement.classList.contains("disabled") &&
          !this.parentElement.classList.contains("active")
        ) {
          const page = parseInt(this.dataset.page);
          performSearch(page);
        }
      });
    });
  }

  /**
   * Update results info
   */
  function updateResultsInfo(data) {
    if (resultsInfo) {
      resultsInfo.innerHTML = `
        Hiển thị <strong>${data.numberOfElements}</strong> trên tổng số <strong>${data.totalElements}</strong> danh mục
      `;
    }
  }

  /**
   * Show loading state
   */
  function showLoading() {
    if (tableBody) {
      tableBody.innerHTML = `
        <tr>
          <td colspan="6" class="text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-2 text-muted">Đang tải dữ liệu...</p>
          </td>
        </tr>
      `;
    }
  }

  /**
   * Hide loading state
   */
  function hideLoading() {
    // Loading is hidden when data is updated
  }

  /**
   * Show error message
   */
  function showError(message) {
    if (tableBody) {
      tableBody.innerHTML = `
        <tr>
          <td colspan="6" class="text-center py-5">
            <div class="text-danger">
              <i class="bi bi-exclamation-triangle display-4 d-block mb-3"></i>
              <p class="mb-0">${message}</p>
            </div>
          </td>
        </tr>
      `;
    }
  }
})();
