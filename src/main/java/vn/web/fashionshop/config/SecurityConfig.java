package vn.web.fashionshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import vn.web.fashionshop.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;

        public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
                this.jwtAuthFilter = jwtAuthFilter;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new vn.web.fashionshop.security.BCryptOrPlaintextPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // CSRF disabled - JWT doesn't need CSRF
                                .csrf(csrf -> csrf.disable())
                                // Fully stateless - no sessions
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // Authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                // Static resources
                                                .requestMatchers("/css/**", "/js/**", "/fonts/**", "/img/**",
                                                                "/assets/**", "/images/**")
                                                .permitAll()
                                                // WebSocket endpoints
                                                .requestMatchers("/ws/**").permitAll()
                                                // Public chat API (for guests)
                                                .requestMatchers("/api/chat/rooms/guest", "/api/chat/rooms/*/messages")
                                                .permitAll()
                                                // Public pages
                                                .requestMatchers("/", "/home", "/login", "/register", "/verify-otp",
                                                                "/resend-otp", "/forgot-password", "/reset-password",
                                                                "/resend-reset-otp",
                                                                // Public browsing (no login required)
                                                                                                                                        "/shop", "/shop/**", "/collections/**", "/product/**",
                                                                                                                                        "/blog", "/single-blog", "/regular-page", "/contact", "/single-product-details",
                                                                                                                                        "/order-status", "/payment-options", "/shipping-delivery", "/guides", "/privacy-policy", "/terms-of-use",
                                                                                                                                        "/error")
                                                .permitAll()
                                                // Cart (guest can add/view cart)
                                                .requestMatchers("/cart", "/cart/**", "/api/cart/**")
                                                .permitAll()
                                                // Wishlist page: guest gets redirected to login by controller
                                                .requestMatchers("/wishlist", "/wishlist/**")
                                                .permitAll()
                                                // Checkout page: guest gets redirected to login by controller
                                                .requestMatchers("/checkout", "/checkout/**")
                                                .permitAll()
                                                // API auth endpoints
                                                .requestMatchers("/api/auth/**").permitAll()
                                                // Public product API
                                                .requestMatchers("/api/products/**").permitAll()
                                                // Newsletter subscribe
                                                .requestMatchers("/newsletter/**").permitAll()
                                                // Admin area - ADMIN và STAFF đều truy cập được
                                                .requestMatchers("/admin/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                                                .requestMatchers("/api/admin/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                                                // All other requests need authentication
                                                .anyRequest().authenticated())
                                // Add JWT filter
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
