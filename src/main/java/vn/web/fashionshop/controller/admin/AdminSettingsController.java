package vn.web.fashionshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/settings")
public class AdminSettingsController {

    @GetMapping
    public String index() {
        return "admin/settings/index";
    }
}
