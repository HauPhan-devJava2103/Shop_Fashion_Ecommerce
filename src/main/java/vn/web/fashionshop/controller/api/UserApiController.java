package vn.web.fashionshop.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.web.fashionshop.dto.UserListDTO;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    /**
     * API tìm kiếm và filter users
     * URL: GET /api/admin/users/search?keyword=abc&roleId=1&status=active&page=1
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page) {

        // Gọi service tìm kiếm
        Page<User> usersPage = userService.searchUsers(keyword, roleId, status, page);

        // Convert sang DTO
        List<UserListDTO> userDTOs = usersPage.getContent().stream()
                .map(userService::userToListDTO)
                .collect(Collectors.toList());

        // Tạo response
        Map<String, Object> response = new HashMap<>();
        response.put("users", userDTOs);
        response.put("currentPage", page);
        response.put("totalPages", usersPage.getTotalPages());
        response.put("totalElements", usersPage.getTotalElements());
        response.put("numberOfElements", usersPage.getNumberOfElements());

        return ResponseEntity.ok(response);
    }
}
