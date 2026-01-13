package vn.web.fashionshop.controller.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import vn.web.fashionshop.service.PaymentService;

@Controller
@RequestMapping("/admin/reports")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ReportsController {

    private final PaymentService paymentService;

    public ReportsController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public String showReportsIndex() {
        return "redirect:/admin/reports/revenue";
    }

    @GetMapping("/revenue")
    public String showRevenueReport(Model model) {
        // Stats Cards
        // 1. Doanh thu hôm nay
        BigDecimal todayRevenue = paymentService.getTodayPayment();
        model.addAttribute("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        // 2. Doanh thu tuần này (7 ngày gần nhất)
        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7).toLocalDate().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal weeklyRevenue = paymentService.getPaymentByDateRange(startOfWeek, now);
        model.addAttribute("weeklyRevenue", weeklyRevenue != null ? weeklyRevenue : BigDecimal.ZERO);

        // 3. Doanh thu tháng này
        BigDecimal thisMonthRevenue = paymentService.getThisMonthRevenue();
        model.addAttribute("thisMonthRevenue", thisMonthRevenue != null ? thisMonthRevenue : BigDecimal.ZERO);

        // 4. Tổng doanh thu
        BigDecimal totalRevenue = paymentService.getTotalPayment();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // 5. Tăng trưởng so tháng trước
        Double growthPercentage = paymentService.getGrowthPercentage();
        model.addAttribute("growthPercentage", growthPercentage != null ? growthPercentage : 0.0);

        // 6. Giá trị đơn trung bình
        BigDecimal avgOrderValue = paymentService.getAverageOrderValue();
        model.addAttribute("avgOrderValue", avgOrderValue != null ? avgOrderValue : BigDecimal.ZERO);

        // Chart Data - 30 days
        List<Object[]> monthlyData = paymentService.getMonthlyRevenueData();
        model.addAttribute("chartLabels", buildChartLabels(30));
        model.addAttribute("chartData", buildChartData(monthlyData, 30));

        // Recent Payments List
        var recentPayments = paymentService.getRecentPayments(0, 20);
        model.addAttribute("payments", recentPayments.getContent());

        return "admin/reports/revenue";
    }

    private List<String> buildChartLabels(int days) {
        List<String> labels = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            labels.add(date.format(formatter));
        }
        return labels;
    }

    private List<BigDecimal> buildChartData(List<Object[]> data, int days) {
        Map<LocalDate, BigDecimal> dataMap = new HashMap<>();
        for (Object[] row : data) {
            if (row[0] != null && row[1] != null) {
                LocalDate date;
                if (row[0] instanceof java.sql.Date) {
                    date = ((java.sql.Date) row[0]).toLocalDate();
                } else {
                    date = LocalDate.parse(row[0].toString());
                }
                BigDecimal revenue = new BigDecimal(row[1].toString());
                dataMap.put(date, revenue);
            }
        }

        List<BigDecimal> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            result.add(dataMap.getOrDefault(date, BigDecimal.ZERO));
        }
        return result;
    }
}
