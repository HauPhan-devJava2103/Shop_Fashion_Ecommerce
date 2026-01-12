package vn.web.fashionshop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.web.fashionshop.entity.Payment;
import vn.web.fashionshop.enums.EPaymentStatus;
import vn.web.fashionshop.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Tổng doanh thu SUCCESS
    public BigDecimal getTotalPayment() {
        return paymentRepository.calculateTotalRevenue(EPaymentStatus.SUCCESS);
    }

    // Doanh thu hôm nay
    public BigDecimal getTodayPayment() {
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        return paymentRepository.calculateTodayRevenue(EPaymentStatus.SUCCESS, startOfToday);
    }

    // Doanh thu theo khoảng thời gian
    public BigDecimal getPaymentByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.calculateRevenueByDateRange(EPaymentStatus.SUCCESS, startDate, endDate);
    }

    // Đếm số đơn thanh toán thành công hôm nay
    public Long getTodayPaymentCount() {
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        return paymentRepository.countTodayPayments(EPaymentStatus.SUCCESS, startOfToday);
    }

    // Doanh thu 7 ngày gần nhất (cho biểu đồ tuần)
    public java.util.List<Object[]> getWeeklyRevenueData() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7).toLocalDate().atStartOfDay();
        return paymentRepository.getDailyRevenue(startDate);
    }

    // Doanh thu 30 ngày gần nhất (cho biểu đồ tháng)
    public java.util.List<Object[]> getMonthlyRevenueData() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30).toLocalDate().atStartOfDay();
        return paymentRepository.getDailyRevenue(startDate);
    }

    // Doanh thu tháng này
    public BigDecimal getThisMonthRevenue() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);
        return paymentRepository.calculateMonthlyRevenue(EPaymentStatus.SUCCESS, startOfMonth, startOfNextMonth);
    }

    // Doanh thu tháng trước
    public BigDecimal getLastMonthRevenue() {
        LocalDateTime startOfLastMonth = LocalDateTime.now().withDayOfMonth(1).minusMonths(1).toLocalDate()
                .atStartOfDay();
        LocalDateTime startOfThisMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        return paymentRepository.calculateMonthlyRevenue(EPaymentStatus.SUCCESS, startOfLastMonth, startOfThisMonth);
    }

    // Tính % tăng trưởng so tháng trước
    public Double getGrowthPercentage() {
        BigDecimal thisMonth = getThisMonthRevenue();
        BigDecimal lastMonth = getLastMonthRevenue();

        if (lastMonth == null || lastMonth.compareTo(BigDecimal.ZERO) == 0) {
            return thisMonth != null && thisMonth.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }

        return thisMonth.subtract(lastMonth)
                .divide(lastMonth, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    // Giá trị đơn trung bình (tháng này)
    public BigDecimal getAverageOrderValue() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        BigDecimal totalRevenue = paymentRepository.calculateMonthlyRevenue(EPaymentStatus.SUCCESS, startOfMonth,
                startOfNextMonth);
        Long orderCount = paymentRepository.countPaymentsByDateRange(EPaymentStatus.SUCCESS, startOfMonth,
                startOfNextMonth);

        if (orderCount == null || orderCount == 0) {
            return BigDecimal.ZERO;
        }

        return totalRevenue.divide(BigDecimal.valueOf(orderCount), 0, RoundingMode.HALF_UP);
    }

    // Lấy danh sách thanh toán gần đây
    public Page<Payment> getRecentPayments(int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findRecentPayments(EPaymentStatus.SUCCESS, pageable);
    }
}
