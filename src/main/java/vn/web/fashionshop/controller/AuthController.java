package vn.web.fashionshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registerUser") RegisterDTO registerDTO, Model model) {
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
