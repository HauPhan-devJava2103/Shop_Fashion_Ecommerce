package vn.web.fashionshop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import vn.web.fashionshop.dto.ChartResponse;
import vn.web.fashionshop.dto.OrderResponseDTO;
import vn.web.fashionshop.entity.Order;
import vn.web.fashionshop.entity.OrderAddress;
import vn.web.fashionshop.entity.OrderItem;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.Voucher;
import vn.web.fashionshop.enums.EOrderStatus;
import vn.web.fashionshop.enums.EPaymentMethod;
import vn.web.fashionshop.enums.EPaymentStatus;
import vn.web.fashionshop.repository.OrderRepository;
import vn.web.fashionshop.repository.ProductVariantRepository;
import vn.web.fashionshop.repository.UserRepository;
import vn.web.fashionshop.repository.VoucherRepository;
import vn.web.fashionshop.util.InventoryManager;
import vn.web.fashionshop.util.OrderCalculator;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VoucherRepository voucherRepository;

    public OrderService(OrderRepository orderRepository,
            UserRepository userRepository,
            ProductVariantRepository productVariantRepository,
            VoucherRepository voucherRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productVariantRepository = productVariantRepository;
        this.voucherRepository = voucherRepository;
    }

    // Get order by ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    /**
     * Update order (status, payment status, address)
     */
    @org.springframework.transaction.annotation.Transactional
    public Order updateOrder(Long id, EOrderStatus orderStatus,
            EPaymentStatus paymentStatus,
            OrderAddress address) {
        Order order = getOrderById(id);

        // Update order status
        if (orderStatus != null) {
            order.setOrderStatus(orderStatus);
        }

        // Update payment status if exists
        if (paymentStatus != null && order.getPayment() != null) {
            order.getPayment().setStatus(paymentStatus);

            // Set paid_at if status changed to PAID/SUCCESS
            if (paymentStatus.name().equals("PAID") || paymentStatus.name().equals("SUCCESS")) {
                if (order.getPayment().getPaidAt() == null) {
                    order.getPayment().setPaidAt(LocalDateTime.now());
                }
            }
        }

        // Update address if provided
        if (address != null && order.getOrderAddress() != null) {
            order.getOrderAddress().setRecipientName(address.getRecipientName());
            order.getOrderAddress().setPhone(address.getPhone());
            order.getOrderAddress().setAddressLine(address.getAddressLine());
            order.getOrderAddress().setWard(address.getWard());
            order.getOrderAddress().setDistrict(address.getDistrict());
            order.getOrderAddress().setCity(address.getCity());
            order.getOrderAddress().setNote(address.getNote());
        }

        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    // Tổng đơn hàng
    public Long countAll() {
        return orderRepository.countAll();
    }

    // Tổng đơn chờ Pending
    public Long countPending() {
        return orderRepository.countPending();
    }

    // Tổng đơn đang giao
    public Long countProcessing() {
        return orderRepository.countProcessing();
    }

    // Tổng doanh thu - Revenue
    public BigDecimal countRevenue() {
        BigDecimal revenue = orderRepository.countRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    // Count orders by status for Pie Chart
    public Long countByStatus(String status) {
        EOrderStatus enumStatus = EOrderStatus.valueOf(status);
        return orderRepository.countByStatus(enumStatus);
    }

    // Get Order Trends data for chart (số đơn hàng + doanh thu theo ngày)
    public ChartResponse getOrderTrendsData(int days) {
        List<String> labels = new ArrayList<>();
        List<Long> orderData = new ArrayList<>();
        List<Double> revenueData = new ArrayList<>();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels.add(date.format(formatter));

            // Số đơn hàng
            Long count = orderRepository.countByDate(date);
            orderData.add(count != null ? count : 0L);

            // Doanh thu
            java.math.BigDecimal revenue = orderRepository.getRevenueByDate(date);
            revenueData.add(revenue != null ? revenue.doubleValue() : 0.0);
        }

        return new ChartResponse(labels, orderData, revenueData);
    }

    public Page<Order> searchOrderAdvanced(String keyword, String status, String paymentMethod, String period,
            int pageNo) {
        int pageSize = 6;
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        // Calculate start and end dates based on period
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (period != null && !period.isEmpty()) {
            LocalDate now = LocalDate.now();
            switch (period) {
                case "today":
                    startDateTime = now.atStartOfDay();
                    endDateTime = now.atTime(23, 59, 59);
                    break;
                case "week":
                    // Start of current week (Monday)
                    LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
                    startDateTime = startOfWeek.atStartOfDay();
                    endDateTime = now.atTime(23, 59, 59);
                    break;
                case "month":
                    // Start of current month
                    LocalDate startOfMonth = now.withDayOfMonth(1);
                    startDateTime = startOfMonth.atStartOfDay();
                    endDateTime = now.atTime(23, 59, 59);
                    break;
            }
        }

        // Convert status string to enum
        EOrderStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = EOrderStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }

        // Convert payment method string to enum
        EPaymentMethod paymentMethodEnum = null;
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            try {
                paymentMethodEnum = EPaymentMethod.valueOf(paymentMethod);
            } catch (IllegalArgumentException e) {
                // Invalid payment method, ignore
            }
        }

        return orderRepository.searchOrders(keyword, statusEnum, paymentMethodEnum, startDateTime, endDateTime,
                pageable);
    }

    /**
     * Search orders and convert to DTO for AJAX response
     */
    public Map<String, Object> searchOrdersForAjax(String keyword, String status, String paymentMethod,
            String period, int page) {

        Page<Order> orderPage = searchOrderAdvanced(keyword, status, paymentMethod, period, page);

        // Convert to DTO to avoid circular reference
        List<OrderResponseDTO> orderDTOs = new ArrayList<>();
        for (Order order : orderPage.getContent()) {
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.setId(order.getId());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setOrderStatus(order.getOrderStatus().name());
            dto.setPaymentMethod(order.getPaymentMethod().name());
            dto.setCreatedAt(order.getCreatedAt());
            if (order.getUser() != null) {
                dto.setUserFullName(order.getUser().getFullName());
                dto.setUserEmail(order.getUser().getEmail());
            }
            orderDTOs.add(dto);
        }

        // Build response similar to Spring Page structure
        Map<String, Object> response = new HashMap<>();
        response.put("content", orderDTOs);
        response.put("number", orderPage.getNumber());
        response.put("numberOfElements", orderPage.getNumberOfElements());
        response.put("totalPages", orderPage.getTotalPages());
        response.put("totalElements", orderPage.getTotalElements());
        response.put("first", orderPage.isFirst());
        response.put("last", orderPage.isLast());

        return response;
    }

    // ORDER CALCULATION METHODS

    /**
     * Tính unit price từ Product (delegate to OrderCalculator)
     */
    public BigDecimal calculateUnitPrice(Product product) {
        return OrderCalculator.calculateUnitPriceFromProduct(product);
    }

    /**
     * Tính total price cho OrderItem (delegate to OrderCalculator)
     */
    public BigDecimal calculateItemTotal(BigDecimal unitPrice, Integer quantity) {
        return OrderCalculator.calculateOrderItemTotal(unitPrice, quantity);
    }

    /**
     * Tính sub total từ OrderItems (delegate to OrderCalculator)
     */
    public BigDecimal calculateSubTotal(List<OrderItem> orderItems) {
        return OrderCalculator.calculateSubTotal(orderItems);
    }

    /**
     * Tính voucher discount (delegate to OrderCalculator)
     */
    public BigDecimal calculateVoucherDiscount(BigDecimal subTotal, Voucher voucher) {
        return OrderCalculator.calculateVoucherDiscount(subTotal, voucher);
    }

    /**
     * Tính total amount (delegate to OrderCalculator)
     */
    public BigDecimal calculateTotalAmount(BigDecimal subTotal, BigDecimal discountAmount) {
        return OrderCalculator.calculateTotalAmount(subTotal, discountAmount);
    }

    /**
     * Validate voucher
     */
    public void validateVoucher(Voucher voucher, BigDecimal subTotal) {
        OrderCalculator.validateVoucher(voucher, subTotal);
    }

    /**
     * Update OrderItem quantity và recalculate totals
     */
    @Transactional
    public Order updateOrderItemQuantity(Long orderId, Long orderItemId, Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Order order = getOrderById(orderId);

        // Find order item
        OrderItem orderItem = order.getOrderItems().stream()
                .filter(item -> item.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        // Calculate stock delta and adjust inventory
        Integer oldQuantity = orderItem.getQuantity();
        Integer quantityDelta = newQuantity - oldQuantity;

        // Use InventoryManager to adjust stock
        InventoryManager.adjustStock(orderItem.getVariant(), quantityDelta);

        // Update quantity
        orderItem.setQuantity(newQuantity);

        // Recalculate total_price
        BigDecimal newTotalPrice = calculateItemTotal(orderItem.getUnitPrice(), newQuantity);
        orderItem.setTotalPrice(newTotalPrice);

        // Recalculate order sub_total
        BigDecimal newSubTotal = calculateSubTotal(order.getOrderItems());
        order.setSubTotal(newSubTotal);

        // Recalculate discount_amount
        BigDecimal newDiscountAmount = BigDecimal.ZERO;
        if (order.getVoucher() != null) {
            newDiscountAmount = calculateVoucherDiscount(newSubTotal, order.getVoucher());
        } else if (order.getVoucherDiscountPercent() != null) {
            newDiscountAmount = newSubTotal
                    .multiply(new BigDecimal(order.getVoucherDiscountPercent()))
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        order.setDiscountAmount(newDiscountAmount);

        // Recalculate total_amount
        BigDecimal newTotalAmount = calculateTotalAmount(newSubTotal, newDiscountAmount);
        order.setTotalAmount(newTotalAmount);

        // Update payment amount
        if (order.getPayment() != null) {
            order.getPayment().setAmount(newTotalAmount);
        }

        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

}
