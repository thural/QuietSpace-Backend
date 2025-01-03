package dev.thural.quietspace.bootstrap;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static dev.thural.quietspace.enums.Role.ADMIN;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminLoader implements CommandLineRunner {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.custom.admin-password}")
    String adminPassword;

    @Override
    public void run(String... args) {
        loadAdmin();
    }

    @Transactional
    private void loadAdmin() {
        if (!repository.existsByUsernameIgnoreCase("admin")) {
            var admin = User.builder()
                    .password(passwordEncoder.encode(adminPassword))
                    .email("admin@email.com")
                    .username("admin")
                    .enabled(true)
                    .role(ADMIN).build();
            repository.save(admin);
        }
    }
}
