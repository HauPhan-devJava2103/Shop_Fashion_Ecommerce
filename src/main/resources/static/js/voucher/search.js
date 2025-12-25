

(function () {
  "use strict";

  // DOM Elements
  const searchForm = document.querySelector('form[action*="/admin/vouchers"]');
  const tableBody = document.querySelector("#vouchersTable tbody");
  const paginationContainer = document.querySelector(".pagination");
  const resultsInfo = document.querySelector(".card-footer small");

  if (!searchForm || !tableBody) return;

  // Prevent default form submission
  searchForm.addEventListener("submit", function (e) {
    e.preventDefault();
    performSearch(1); // Reset to page 1 on new search
  });

  // Handle input changes (optional: search while typing với debounce)
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

  /**
   * Perform AJAX search
   */
  function performSearch(page = 1) {
    // Get form data
    const formData = new FormData(searchForm);
    const keyword = formData.get("keyword") || "";
    const status = formData.get("status") || "";

    // Build URL parameters
    const params = new URLSearchParams({
      page: page,
      keyword: keyword,
      status: status,
    });

    // Show loading state
    showLoading();

    // Fetch data
    fetch(`/admin/vouchers/api/search?${params.toString()}`, {
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
        updatePagination(data, keyword, status);
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
          <td colspan="9" class="text-center py-5">
            <div class="text-muted">
              <i class="bi bi-ticket-perforated display-4 d-block mb-3 opacity-50"></i>
              <p class="mb-0">Không tìm thấy voucher nào</p>
            </div>
          </td>
        </tr>
      `;
      return;
    }

    tableBody.innerHTML = data.content
      .map((voucher) => createVoucherRow(voucher))
      .join("");
  }

  /**
   * Create voucher table row HTML
   */
  function createVoucherRow(voucher) {
    const formatNumber = (num) => (num ? num.toLocaleString("vi-VN") : "---");
    const formatDate = (date) => {
      if (!date) return "";
      const d = new Date(date);
      return `${d.getDate().toString().padStart(2, "0")}/${(d.getMonth() + 1)
        .toString()
        .padStart(2, "0")}/${d.getFullYear()}`;
    };

    const statusBadge =
      voucher.isActive && voucher.valid
        ? '<span class="badge bg-success-subtle text-success"><i class="bi bi-check-circle me-1"></i>Còn hiệu lực</span>'
        : voucher.isActive && !voucher.valid
        ? '<span class="badge bg-warning-subtle text-warning"><i class="bi bi-exclamation-circle me-1"></i>Hết hạn/Đã dùng hết</span>'
        : '<span class="badge bg-secondary-subtle text-secondary"><i class="bi bi-x-circle me-1"></i>Vô hiệu</span>';

    return `
      <tr>
        <td class="ps-4 fw-semibold text-muted">${voucher.id}</td>
        <td>
          <div class="d-flex align-items-center">
            <div class="bg-success bg-opacity-10 rounded-2 p-2 me-2">
              <i class="bi bi-ticket-perforated-fill text-success"></i>
            </div>
            <div>
              <div class="fw-bold text-uppercase">${voucher.code}</div>
              ${
                voucher.endAt
                  ? `<small class="text-muted">HSD: ${formatDate(
                      voucher.endAt
                    )}</small>`
                  : ""
              }
            </div>
          </div>
        </td>
        <td>
          <span class="text-truncate d-inline-block" style="max-width: 200px" title="${
            voucher.description || ""
          }">${voucher.description || ""}</span>
        </td>
        <td class="text-center">
          <span class="badge bg-success fs-6">${voucher.discountPercent}%</span>
        </td>
        <td class="text-end">${
          voucher.maxDiscountAmount
            ? formatNumber(voucher.maxDiscountAmount) + "đ"
            : '<span class="text-muted">---</span>'
        }</td>
        <td class="text-end">${
          voucher.minOrderValue
            ? formatNumber(voucher.minOrderValue) + "đ"
            : '<span class="text-muted">---</span>'
        }</td>
        <td class="text-center">
          <span>${voucher.usedCount || 0}</span>
          ${
            voucher.usageLimit
              ? `/ <span>${voucher.usageLimit}</span>`
              : '/ <span class="text-muted">∞</span>'
          }
        </td>
        <td class="text-center">${statusBadge}</td>
        <td class="text-center">
          <div class="dropdown">
            <button class="btn btn-sm btn-light border-0 rounded-circle d-inline-flex align-items-center justify-content-center" 
                    style="width: 34px; height: 34px" type="button" data-bs-toggle="dropdown">
              <i class="bi bi-three-dots-vertical"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 py-2" style="min-width: 160px">
              <li><a class="dropdown-item d-flex align-items-center py-2 px-3" href="/admin/vouchers/view/${
                voucher.id
              }">
                <i class="bi bi-eye text-info me-2"></i>Xem chi tiết</a></li>
              <li><hr class="dropdown-divider my-1" /></li>
              <li><a class="dropdown-item d-flex align-items-center py-2 px-3" href="/admin/vouchers/edit/${
                voucher.id
              }">
                <i class="bi bi-pencil-square text-primary me-2"></i>Chỉnh sửa</a></li>
              <li><hr class="dropdown-divider my-1" /></li>
              <li><a class="dropdown-item d-flex align-items-center py-2 px-3 text-danger" 
                     href="/admin/vouchers/delete/${voucher.id}" 
                     onclick="return confirm('Bạn có chắc chắn muốn xóa voucher này?')">
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
  function updatePagination(data, keyword, status) {
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
        Hiển thị <strong>${data.numberOfElements}</strong> trên tổng số <strong>${data.totalElements}</strong> voucher
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
          <td colspan="9" class="text-center py-5">
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
          <td colspan="9" class="text-center py-5">
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
