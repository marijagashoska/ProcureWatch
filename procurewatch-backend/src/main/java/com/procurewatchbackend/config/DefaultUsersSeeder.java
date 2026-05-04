package com.procurewatchbackend.config;

import com.procurewatchbackend.model.entity.AppUser;
import com.procurewatchbackend.model.enums.UserRole;
import com.procurewatchbackend.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultUsersSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!appUserRepository.existsByUsername("admin")) {
            appUserRepository.save(AppUser.builder()
                    .username("admin")
                    .email("admin@procurewatch.local")
                    .password(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .build());
        }

        if (!appUserRepository.existsByUsername("user")) {
            appUserRepository.save(AppUser.builder()
                    .username("user")
                    .email("user@procurewatch.local")
                    .password(passwordEncoder.encode("user123"))
                    .role(UserRole.USER)
                    .enabled(true)
                    .build());
        }
    }
}