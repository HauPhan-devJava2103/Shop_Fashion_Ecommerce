package vn.web.fashionshop.controller.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import vn.web.fashionshop.service.OrderService;
import vn.web.fashionshop.service.PaymentService;
import vn.web.fashionshop.service.ProductService;
import vn.web.fashionshop.service.UserService;

@Controller
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class DashBoardController {

    private final ProductService productService;
    private final UserService userService;
    private final PaymentService paymentService;
    private final OrderService orderService;

    public DashBoardController(ProductService productService, UserService userService,
            PaymentService paymentService, OrderService orderService) {
        this.productService = productService;
        this.userService = userService;
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @GetMapping("/admin")
    public String getDashBoard(Model model) {
        // 1. Doanh thu hôm nay
        BigDecimal todayRevenue = paymentService.getTodayPayment();
        model.addAttribute("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        // 2. Doanh thu tháng này
        BigDecimal thisMonthRevenue = paymentService.getThisMonthRevenue();
        model.addAttribute("thisMonthRevenue", thisMonthRevenue != null ? thisMonthRevenue : BigDecimal.ZERO);

        // 3. Tăng trưởng so tháng trước (%)
        Double growthPercentage = paymentService.getGrowthPercentage();
        model.addAttribute("growthPercentage", growthPercentage != null ? growthPercentage : 0.0);

        // 4. Giá trị đơn trung bình
        BigDecimal avgOrderValue = paymentService.getAverageOrderValue();
        model.addAttribute("avgOrderValue", avgOrderValue != null ? avgOrderValue : BigDecimal.ZERO);

        // 5. Dữ liệu biểu đồ doanh thu tuần (7 ngày)
        List<Object[]> weeklyData = paymentService.getWeeklyRevenueData();
        model.addAttribute("weeklyChartLabels", buildChartLabels(weeklyData, 7));
        model.addAttribute("weeklyChartData", buildChartData(weeklyData, 7));

        // 6. Dữ liệu biểu đồ doanh thu tháng (30 ngày)
        List<Object[]> monthlyData = paymentService.getMonthlyRevenueData();
        model.addAttribute("monthlyChartLabels", buildChartLabels(monthlyData, 30));
        model.addAttribute("monthlyChartData", buildChartData(monthlyData, 30));

        // 7. Sản phẩm theo danh mục (cho Donut chart)
        List<Object[]> productsByCategory = productService.getProductCountByCategory();
        List<String> categoryLabels = new ArrayList<>();
        List<Long> categoryData = new ArrayList<>();
        for (Object[] row : productsByCategory) {
            if (row[0] != null && row[1] != null) {
                categoryLabels.add(row[0].toString());
                categoryData.add(Long.parseLong(row[1].toString()));
            }
        }
        model.addAttribute("categoryLabels", categoryLabels);
        model.addAttribute("categoryData", categoryData);

        return "admin/dashboard/show";
    }

    // Tạo labels cho chart (fill đủ các ngày)
    private List<String> buildChartLabels(List<Object[]> data, int days) {
        List<String> labels = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            labels.add(date.format(formatter));
        }
        return labels;
    }

    // Tạo data cho chart (fill đủ các ngày với 0 nếu không có dữ liệu)
    private List<BigDecimal> buildChartData(List<Object[]> data, int days) {
        // Map data by date
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

        // Build result with all days
        List<BigDecimal> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            result.add(dataMap.getOrDefault(date, BigDecimal.ZERO));
        }
        return result;
    }
}
