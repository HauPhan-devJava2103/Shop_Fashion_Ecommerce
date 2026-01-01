package vn.web.fashionshop.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.web.fashionshop.entity.Voucher;
import vn.web.fashionshop.repository.VoucherRepository;

@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    private final VoucherRepository voucherRepository;

    public AdminVoucherController(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @GetMapping
    public String index(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            Model model) {

        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Voucher> vouchers = voucherRepository.findAllByOrderByCreatedAtDesc(pageable);

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", Math.max(1, vouchers.getTotalPages()));
        model.addAttribute("size", size);
        return "admin/voucher/index";
    }
}
