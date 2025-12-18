package vn.web.fashionshop.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartResponse {

    private List<String> labels;
    private List<Long> data;
}
