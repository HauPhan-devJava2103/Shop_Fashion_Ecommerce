package vn.web.fashionshop.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import vn.web.fashionshop.dto.AddVoucherDTO;
import vn.web.fashionshop.entity.Voucher;
import vn.web.fashionshop.repository.VoucherRepository;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    public List<Voucher> getAll() {
        return voucherRepository.findAll();
    }

    public List<Voucher> getAllWithSort(String sortBy, String sortDir) {
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isEmpty()) {
            sort = sortDir != null && sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
        }
        return voucherRepository.findAll(sort);
    }

    public Long countTotal() {
        return voucherRepository.count();
    }

    public Long countActive() {
        return voucherRepository.countByIsActiveTrue();
    }

    public Long countExpired() {
        return voucherRepository.countByExpired(LocalDateTime.now());
    }

    public Long countUsed() {
        return voucherRepository.countTotalUsed();
    }

    public Voucher voucherDTOToVoucher(AddVoucherDTO voucherDTO) {
        Voucher voucher = new Voucher();
        voucher.setCode(voucherDTO.getCode());
        voucher.setDiscountPercent(voucherDTO.getDiscountPercent());
        voucher.setMaxDiscountAmount(voucherDTO.getMaxDiscountAmount());
        voucher.setMinOrderValue(voucherDTO.getMinOrderValue());
        voucher.setDescription(voucherDTO.getDescription());
        voucher.setUsageLimit(voucherDTO.getUsageLimit());
        voucher.setIsActive(voucherDTO.getIsActive());

        // Set thời gian hiệu lực
        voucher.setStartAt(voucherDTO.getStartAt());
        voucher.setEndAt(voucherDTO.getEndAt());

        // Set default values
        voucher.setUsedCount(0);
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setUpdatedAt(LocalDateTime.now());

        return voucher;
    }

    public Voucher create(AddVoucherDTO voucherDTO) {
        Voucher voucher = voucherDTOToVoucher(voucherDTO);
        if (voucherRepository.existsByCode(voucher.getCode())) {
            return null;
        }
        return voucherRepository.save(voucher);
    }

    public void delete(Long id) {
        if (voucherRepository.findById(id).isPresent()) {
            voucherRepository.deleteById(id);
        } else {
            throw new RuntimeException("Voucher not found");
        }
    }

    public Page<Voucher> searchVoucherAdvanced(String keyword, String status, int pageNo, String sortBy,
            String sortDir) {
        int pageSize = 6;

        // Xử lý sorting
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = (sortDir != null && sortDir.equalsIgnoreCase("desc"))
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            sort = Sort.by(direction, sortBy);
        } else {
            // Default sort by createdAt DESC
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

        return voucherRepository.searchVoucherAdvanced(keyword, status, LocalDateTime.now(), pageable);
    }

    public Voucher findById(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found with id: " + id));
    }

    public AddVoucherDTO voucherToDTO(Voucher voucher) {
        AddVoucherDTO dto = new AddVoucherDTO();
        dto.setCode(voucher.getCode());
        dto.setDescription(voucher.getDescription());
        dto.setDiscountPercent(voucher.getDiscountPercent());
        dto.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        dto.setMinOrderValue(voucher.getMinOrderValue());
        dto.setUsageLimit(voucher.getUsageLimit());
        dto.setIsActive(voucher.getIsActive());
        dto.setStartAt(voucher.getStartAt());
        dto.setEndAt(voucher.getEndAt());
        return dto;
    }

    public Voucher update(Long id, AddVoucherDTO voucherDTO) {
        Voucher existingVoucher = findById(id);

        // Kiểm tra nếu code bị trùng với voucher khác (không phải chính nó)
        if (!existingVoucher.getCode().equals(voucherDTO.getCode())
                && voucherRepository.existsByCode(voucherDTO.getCode())) {
            throw new RuntimeException("Mã voucher đã tồn tại");
        }

        // Cập nhật thông tin
        existingVoucher.setCode(voucherDTO.getCode());
        existingVoucher.setDescription(voucherDTO.getDescription());
        existingVoucher.setDiscountPercent(voucherDTO.getDiscountPercent());
        existingVoucher.setMaxDiscountAmount(voucherDTO.getMaxDiscountAmount());
        existingVoucher.setMinOrderValue(voucherDTO.getMinOrderValue());
        existingVoucher.setUsageLimit(voucherDTO.getUsageLimit());
        existingVoucher.setIsActive(voucherDTO.getIsActive());
        existingVoucher.setStartAt(voucherDTO.getStartAt());
        existingVoucher.setEndAt(voucherDTO.getEndAt());
        existingVoucher.setUpdatedAt(LocalDateTime.now());

        return voucherRepository.save(existingVoucher);
    }
}
