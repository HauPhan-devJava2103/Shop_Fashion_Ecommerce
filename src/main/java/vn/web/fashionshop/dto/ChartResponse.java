package vn.web.fashionshop.dto;

import java.util.List;

import lombok.Data;

@Data
public class ChartResponse {

    private List<String> labels;
    private List<Long> data; // Số đơn hàng
    private List<Double> revenueData; // Doanh thu (optional)

    // Constructor for order/user count only
    public ChartResponse(List<String> labels, List<Long> data) {
        this.labels = labels;
        this.data = data;
        this.revenueData = null;
    }

    // Constructor with revenue data
    public ChartResponse(List<String> labels, List<Long> data, List<Double> revenueData) {
        this.labels = labels;
        this.data = data;
        this.revenueData = revenueData;
    }
}
