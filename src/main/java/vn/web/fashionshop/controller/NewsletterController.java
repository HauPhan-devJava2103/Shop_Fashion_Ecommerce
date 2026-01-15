package vn.web.fashionshop.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import vn.web.fashionshop.entity.NewsletterSubscription;
import vn.web.fashionshop.repository.NewsletterSubscriptionRepository;

@Controller
public class NewsletterController {

    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;

    public NewsletterController(NewsletterSubscriptionRepository newsletterSubscriptionRepository) {
        this.newsletterSubscriptionRepository = newsletterSubscriptionRepository;
    }

    @PostMapping("/newsletter/subscribe")
    public String subscribe(
            @RequestParam(value = "mail", required = false) String mail,
            HttpServletRequest request) {

        String email = mail != null ? mail.trim() : "";
        if (!email.isBlank() && email.length() <= 255) {
            // minimal email validation
            if (email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                NewsletterSubscription sub = newsletterSubscriptionRepository.findByEmail(email).orElse(null);
                if (sub == null) {
                    sub = new NewsletterSubscription();
                    sub.setEmail(email);
                    sub.setIsActive(true);
                    sub.setCreatedAt(LocalDateTime.now());
                    newsletterSubscriptionRepository.save(sub);
                } else {
                    sub.setIsActive(true);
                    newsletterSubscriptionRepository.save(sub);
                }
            }
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }
}
