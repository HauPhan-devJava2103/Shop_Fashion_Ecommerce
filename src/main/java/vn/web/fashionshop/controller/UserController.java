package vn.web.fashionshop.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.web.fashionshop.dto.ChartResponse;
import vn.web.fashionshop.entity.Role;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.service.RoleService;
import vn.web.fashionshop.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping({ "", "/", "/index" })
    public String index(Model model,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "roleId", required = false) Long roleId,
            @RequestParam(name = "status", required = false) String status) {

        Page<User> usersPage = userService.searchUsers(keyword, roleId, status, page);
        List<Role> roles = roleService.getAllRoles();

        Long totalUser = userService.countTotalUser();
        Long activeUser = userService.countActiveUser();
        Long totalUserThisMonth = userService.countUserThisMonth();
        Long totalRoles = roleService.countTotalRoles();

        model.addAttribute("users", usersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());

        // Maintain filter state
        model.addAttribute("keyword", keyword);
        model.addAttribute("roleId", roleId);
        model.addAttribute("status", status);

        model.addAttribute("roles", roles);
        model.addAttribute("totalUser", totalUser);
        model.addAttribute("activeUser", activeUser);
        model.addAttribute("totalUserThisMonth", totalUserThisMonth);
        model.addAttribute("totalRoles", totalRoles);
        return "admin/user/index";
    }

    // API Endpoint lấy dữ liệu
    @GetMapping("/api/user/growth")
    @ResponseBody
    public ChartResponse getUserGrowth(@RequestParam(defaultValue = "7") int days) {
        List<Object[]> data = userService.countUserByDateRange(days);

        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (Object[] item : data) {
            labels.add(item[0].toString());
            counts.add((Long) item[1]);
        }

        return new ChartResponse(labels, counts);
    }

    // CHART ROLE USERS
    @GetMapping("/api/user/roles")
    @ResponseBody
    public ChartResponse getUserRoles() {
        List<Object[]> data = userService.countUsersByRole();

        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (Object[] item : data) {
            labels.add(item[0].toString());
            counts.add((Long) item[1]);
        }
        return new ChartResponse(labels, counts);
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/user/create";
    }

    @PostMapping("/create")
    public String save(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes, Model model) {
        User createdUser = userService.create(user);
        if (createdUser == null) {
            model.addAttribute("roles", roleService.getAllRoles());
            model.addAttribute("errorMessage", "Email already exists or invalid data!");
            return "admin/user/create";
        }
        redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
        return "redirect:/admin/users";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/user/edit";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute User user,
            @RequestParam(required = false) String newPassword,
            RedirectAttributes redirectAttributes, Model model) {

        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(newPassword);
        }
        User updatedUser = userService.update(user);
        if (updatedUser == null) {
            model.addAttribute("roles", roleService.getAllRoles());
            model.addAttribute("errorMessage", "Update failed! Email might exist or User ID invalid.");
            return "admin/user/edit";
        }
        redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        return "redirect:/admin/users";
    }

    @GetMapping("/view/{id}")
    public String viewDetailUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        return "admin/user/view";
    }
}
