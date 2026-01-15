package vn.web.fashionshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PagesController {

    @GetMapping("/blog")
    public String blog() {
        return "blog";
    }

    @GetMapping("/single-blog")
    public String singleBlog() {
        return "single-blog";
    }

    @GetMapping("/regular-page")
    public String regularPage(Model model) {
        model.addAttribute("pageTitle", "Regular Page");
        return "regular-page";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/order-status")
    public String orderStatus(Model model) {
        model.addAttribute("pageTitle", "Order Status");
        return "regular-page";
    }

    @GetMapping("/payment-options")
    public String paymentOptions(Model model) {
        model.addAttribute("pageTitle", "Payment Options");
        return "regular-page";
    }

    @GetMapping("/shipping-delivery")
    public String shippingDelivery(Model model) {
        model.addAttribute("pageTitle", "Shipping and Delivery");
        return "regular-page";
    }

    @GetMapping("/guides")
    public String guides(Model model) {
        model.addAttribute("pageTitle", "Guides");
        return "regular-page";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy(Model model) {
        model.addAttribute("pageTitle", "Privacy Policy");
        return "regular-page";
    }

    @GetMapping("/terms-of-use")
    public String termsOfUse(Model model) {
        model.addAttribute("pageTitle", "Terms of Use");
        return "regular-page";
    }

    @GetMapping("/single-product-details")
    public String legacyProductDetails() {
        // This project uses /product/{id} for product details
        return "redirect:/shop";
    }
}
