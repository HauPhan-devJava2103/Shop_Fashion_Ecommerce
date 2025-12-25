package vn.web.fashionshop.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.BindingResult;

import vn.web.fashionshop.dto.RegisterDTO;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.enums.ERoleName;
import vn.web.fashionshop.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // LOGIN
    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    // REGISTER
    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("registerUser", new RegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerUser") RegisterDTO registerDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (registerDTO.getPassword() == null || !registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "register";
        }

        User user = userService.registerDTOtoUser(registerDTO);
        user.setRole(userService.getRoleByName(ERoleName.CUSTOMER));
        User createdUser = userService.create(user);

        if (createdUser == null) {
            model.addAttribute("error", "Email or Phone already exists!");
            return "register";
        }

        return "redirect:/login";
    }

}
