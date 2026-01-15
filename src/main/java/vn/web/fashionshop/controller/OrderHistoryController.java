package vn.web.fashionshop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.web.fashionshop.entity.Order;
import vn.web.fashionshop.enums.EOrderCancelReason;
import vn.web.fashionshop.repository.OrderRepository;
import vn.web.fashionshop.service.CartService;
import vn.web.fashionshop.service.CheckoutService;
import vn.web.fashionshop.service.OrderService;

@Controller
public class OrderHistoryController {

    private final OrderRepository orderRepository;
    private final CheckoutService checkoutService;
    private final OrderService orderService;

    public OrderHistoryController(OrderRepository orderRepository, CheckoutService checkoutService, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.checkoutService = checkoutService;
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String myOrders(Model model) {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }
        List<Order> orders = orderRepository.findMyOrdersWithAddress(email);
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String myOrderDetail(@PathVariable("id") Long id, Model model) {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        Order order = checkoutService.getMyOrderForSuccessPage(email, id);
        if (order == null) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("cancelReasons", EOrderCancelReason.values());
        return "order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelMyOrder(
            @PathVariable("id") Long id,
            @RequestParam("reason") String reason,
            @RequestParam(value = "note", required = false) String note,
            RedirectAttributes redirectAttributes) {

        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        try {
            EOrderCancelReason enumReason = EOrderCancelReason.valueOf(reason);
            orderService.cancelMyOrder(email, id, enumReason, note);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công!");
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg == null || msg.isBlank() || "ORDER_NOT_FOUND".equals(msg) || "MISSING_CANCEL_REASON".equals(msg)
                    || "INVALID_REQUEST".equals(msg)) {
                msg = "Không thể hủy đơn hàng.";
            }
            redirectAttributes.addFlashAttribute("errorMessage", msg);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng không thể hủy ở trạng thái hiện tại.");
        }

        return "redirect:/orders/" + id;
    }
}
