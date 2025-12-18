package vn.web.fashionshop.security;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.repository.UserRepository;

/**
 * DEV helper: if the DB already contains plaintext passwords (e.g. seeded from data.sql),
 * this will migrate them to BCrypt once on startup.
 *
 * Safe guard: it only encodes when the password does NOT look like a BCrypt hash.
 */
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();
        boolean changed = false;

        for (User u : users) {
            if (u == null) {
                continue;
            }
            String pw = u.getPassword();
            if (pw == null || pw.isBlank()) {
                continue;
            }

            // BCrypt hashes typically start with $2a$, $2b$ or $2y$ and have length around 60.
            boolean looksLikeBcrypt = (pw.startsWith("$2a$") || pw.startsWith("$2b$") || pw.startsWith("$2y$"))
                    && pw.length() >= 55;

            if (!looksLikeBcrypt) {
                u.setPassword(passwordEncoder.encode(pw));
                changed = true;
            }
        }

        if (changed) {
            userRepository.saveAll(users);
        }
    }
}
