package vn.web.fashionshop.controller.admin.voucher;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import vn.web.fashionshop.dto.AddVoucherDTO;
import vn.web.fashionshop.entity.Voucher;
import vn.web.fashionshop.service.VoucherService;

@Controller
@RequestMapping("/admin/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public String index(Model model,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {

        // Sử dụng search nâng cao với filter và sort
        Page<Voucher> vouchersPage = voucherService.searchVoucherAdvanced(keyword, status, page, sortBy, sortDir);

        model.addAttribute("totalVouchers", voucherService.countTotal());
        model.addAttribute("activeVouchers", voucherService.countActive());
        model.addAttribute("expiredVouchers", voucherService.countExpired());
        model.addAttribute("usedVouchers", voucherService.countUsed());

        // Pagination data
        model.addAttribute("vouchers", vouchersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", vouchersPage.getTotalPages());

        // Filter & search params
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "admin/voucher/index";
    }

    // Search vouchers using AJAX
    @GetMapping("/api/search")
    @ResponseBody
    public Page<Voucher> searchVouchersAjax(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {

        return voucherService.searchVoucherAdvanced(keyword, status, page, sortBy, sortDir);
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("voucher", new AddVoucherDTO());
        return "admin/voucher/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("voucher") AddVoucherDTO voucherDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Kiểm tra lỗi validation từ DTO
        if (bindingResult.hasErrors()) {
            return "admin/voucher/create";
        }

        if (voucherDTO.getEndAt() != null && voucherDTO.getStartAt() != null) {
            if (voucherDTO.getEndAt().isBefore(voucherDTO.getStartAt())) {
                model.addAttribute("errorMessage", "Thời gian kết thúc phải sau thời gian bắt đầu!");
                return "admin/voucher/create";
            }
        }

        // Tạo voucher
        try {
            Voucher createdVoucher = voucherService.create(voucherDTO);
            if (createdVoucher == null) {
                model.addAttribute("errorMessage", "Mã voucher đã tồn tại!");
                return "admin/voucher/create";
            }
            redirectAttributes.addFlashAttribute("successMessage", "Tạo voucher thành công!");
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "admin/voucher/create";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa voucher thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa voucher: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Voucher voucher = voucherService.findById(id);
            AddVoucherDTO voucherDTO = voucherService.voucherToDTO(voucher);
            model.addAttribute("voucher", voucherDTO);
            model.addAttribute("voucherId", id);
            return "admin/voucher/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy voucher!");
            return "redirect:/admin/vouchers";
        }
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("voucher") AddVoucherDTO voucherDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Kiểm tra lỗi validation từ DTO
        if (bindingResult.hasErrors()) {
            model.addAttribute("voucherId", id);
            return "admin/voucher/edit";
        }

        // Kiểm tra logic nghiệp vụ
        if (voucherDTO.getEndAt() != null && voucherDTO.getStartAt() != null) {
            if (voucherDTO.getEndAt().isBefore(voucherDTO.getStartAt())) {
                model.addAttribute("voucherId", id);
                model.addAttribute("errorMessage", "Thời gian kết thúc phải sau thời gian bắt đầu!");
                return "admin/voucher/edit";
            }
        }

        // Cập nhật voucher
        try {
            voucherService.update(id, voucherDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật voucher thành công!");
            return "redirect:/admin/vouchers";
        } catch (RuntimeException e) {
            model.addAttribute("voucherId", id);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/voucher/edit";
        } catch (Exception e) {
            model.addAttribute("voucherId", id);
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "admin/voucher/edit";
        }
    }

    @GetMapping("/view/{id}")
    public String viewDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Voucher voucher = voucherService.findById(id);
            model.addAttribute("voucher", voucher);
            return "admin/voucher/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy voucher!");
            return "redirect:/admin/vouchers";
        }
    }

}
