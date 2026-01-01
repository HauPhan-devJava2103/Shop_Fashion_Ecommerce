package vn.web.fashionshop.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Static resources
                        .requestMatchers("/css/**", "/js/**", "/fonts/**", "/img/**", "/assets/**", "/images/**").permitAll()
                        // Public pages
                        .requestMatchers("/", "/home", "/login", "/register").permitAll()
                        // Admin area
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        // Spring Security expects POST /login with parameters username/password
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(this::handleLoginSuccess)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                // Keep CSRF enabled for browser forms.
                .csrf(Customizer.withDefaults());

        return http.build();
    }

        private void handleLoginSuccess(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Authentication authentication) throws IOException {

                boolean isAdmin = authentication != null
                                && authentication.getAuthorities() != null
                                && authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

                response.sendRedirect(isAdmin ? "/admin" : "/");
        }
}
