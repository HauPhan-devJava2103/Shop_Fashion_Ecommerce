package vn.web.fashionshop.controller.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import vn.web.fashionshop.repository.OrderRepository;
import vn.web.fashionshop.repository.UserRepository;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public AdminReportController(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("totalOrders", orderRepository.count());
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalRevenue", orderRepository.sumTotalRevenue());

        // last 7 days revenue (simple)
        LocalDate today = LocalDate.now();
        LocalDate startDay = today.minusDays(6);
        LocalDateTime start = startDay.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        BigDecimal revenue7d = orderRepository.sumRevenueBetween(start, end);
        model.addAttribute("revenue7d", revenue7d);

        return "admin/report/index";
    }

    @GetMapping("/revenue")
    public String revenue(Model model) {
        model.addAttribute("totalRevenue", orderRepository.sumTotalRevenue());
        return "admin/report/revenue";
    }
}
