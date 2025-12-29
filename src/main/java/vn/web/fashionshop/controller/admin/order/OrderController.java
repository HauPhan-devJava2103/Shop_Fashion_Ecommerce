package vn.web.fashionshop.controller.admin.order;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.web.fashionshop.entity.Order;
import vn.web.fashionshop.entity.OrderAddress;
import vn.web.fashionshop.enums.EOrderStatus;
import vn.web.fashionshop.enums.EPaymentMethod;
import vn.web.fashionshop.enums.EPaymentStatus;
import vn.web.fashionshop.service.OrderService;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/admin/orders")
    public String listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String period,
            Model model) {

        // Stats
        model.addAttribute("totalOrder", orderService.countAll());
        model.addAttribute("totalPending", orderService.countPending());
        model.addAttribute("totalProcessing", orderService.countProcessing());
        model.addAttribute("totalRevenue", orderService.countRevenue());

        // Order Status Distribution for Pie Chart
        model.addAttribute("statusPending", orderService.countByStatus("PENDING"));
        model.addAttribute("statusConfirmed", orderService.countByStatus("CONFIRMED"));
        model.addAttribute("statusProcessing", orderService.countByStatus("PROCESSING"));
        model.addAttribute("statusShipped", orderService.countByStatus("SHIPPED"));
        model.addAttribute("statusDelivered", orderService.countByStatus("DELIVERED"));
        model.addAttribute("statusCompleted", orderService.countByStatus("COMPLETED"));
        model.addAttribute("statusCancelled", orderService.countByStatus("CANCELLED"));

        // Order List with Pagination
        Page<Order> orderPage = orderService
                .searchOrderAdvanced(keyword, status, paymentMethod, period, page);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());

        // Filter values for form
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("filterStatus", status != null ? status : "");
        model.addAttribute("filterPaymentMethod", paymentMethod != null ? paymentMethod : "");
        model.addAttribute("filterPeriod", period != null ? period : "");

        // Enums for dropdowns
        model.addAttribute("orderStatuses", EOrderStatus.values());
        model.addAttribute("paymentMethods", EPaymentMethod.values());

        return "admin/order/index";
    }

    // AJAX Search Orders
    @GetMapping("/admin/orders/api/search")
    @ResponseBody
    public Map<String, Object> searchOrdersAjax(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String period) {

        return orderService.searchOrdersForAjax(keyword, status, paymentMethod, period, page);
    }

    // API endpoint for Order Trends Chart
    @GetMapping("/admin/orders/api/trends")
    @ResponseBody
    public vn.web.fashionshop.dto.ChartResponse getOrderTrendsData(
            @RequestParam(defaultValue = "7") int days) {
        return orderService.getOrderTrendsData(days);
    }

    // View Order Details
    @GetMapping("/admin/orders/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "admin/order/view";
    }

    // Edit Order Form
    @GetMapping("/admin/orders/edit/{id}")
    public String editOrderForm(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);

        // Add enums for dropdown
        model.addAttribute("orderStatuses", EOrderStatus.values());
        model.addAttribute("paymentStatuses", EPaymentStatus.values());

        return "admin/order/edit";
    }

    // Update Order
    @PostMapping("/admin/orders/edit/{id}")
    public String updateOrder(
            @PathVariable Long id,
            @RequestParam("orderStatus") String orderStatusStr,
            @RequestParam(value = "paymentStatus", required = false) String paymentStatusStr,
            @RequestParam("recipientName") String recipientName,
            @RequestParam("phone") String phone,
            @RequestParam("addressLine") String addressLine,
            @RequestParam("ward") String ward,
            @RequestParam("district") String district,
            @RequestParam("city") String city,
            @RequestParam(value = "note", required = false) String note,
            RedirectAttributes redirectAttributes) {

        try {
            // Parse enums
            EOrderStatus orderStatus = EOrderStatus.valueOf(orderStatusStr);
            EPaymentStatus paymentStatus = null;
            if (paymentStatusStr != null && !paymentStatusStr.isEmpty()) {
                paymentStatus = EPaymentStatus.valueOf(paymentStatusStr);
            }

            // Build address
            OrderAddress address = new OrderAddress();
            address.setRecipientName(recipientName);
            address.setPhone(phone);
            address.setAddressLine(addressLine);
            address.setWard(ward);
            address.setDistrict(district);
            address.setCity(city);
            address.setNote(note);

            // Update order
            orderService.updateOrder(id, orderStatus, paymentStatus, address);

            redirectAttributes.addFlashAttribute("success", "Cập nhật đơn hàng thành công!");
            return "redirect:/admin/orders/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/orders/edit/" + id;
        }
    }

    // Update Order Item Quantity (AJAX)
    @PostMapping("/admin/orders/{orderId}/items/{itemId}/quantity")
    @ResponseBody
    public Map<String, Object> updateItemQuantity(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {

        Map<String, Object> response = new HashMap<>();

        try {
            Order order = orderService.updateOrderItemQuantity(orderId, itemId, quantity);

            response.put("success", true);
            response.put("message", "Cập nhật số lượng thành công!");
            response.put("subTotal", order.getSubTotal());
            response.put("discountAmount", order.getDiscountAmount());
            response.put("totalAmount", order.getTotalAmount());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

}
