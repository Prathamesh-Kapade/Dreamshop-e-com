package com.demo.dreamshops.security.config;

import com.demo.dreamshops.model.Role;
import com.demo.dreamshops.model.User;
import com.demo.dreamshops.data.RoleRepository;
import com.demo.dreamshops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private boolean alreadySetup = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) return;

        log.info(" DataInitializer running...");

        createRolesIfNotExist();
        createDefaultAdminIfNotExist();
        createDefaultUserIfNotExist();

        alreadySetup = true;
    }


        private void createRolesIfNotExist() {
        List.of("ROLE_ADMIN", "ROLE_USER").forEach(roleName -> {
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = new Role();
                role.setName(roleName);
                log.info(" Creating role: {}", roleName);
                return roleRepository.save(role);
            });
        });
    }


    private void createDefaultAdminIfNotExist() {
        String adminEmail = "admin@dreamshops.com";

        userRepository.findByEmail(adminEmail).ifPresentOrElse(
                user -> log.info(" Admin already exists: {}", adminEmail),
                () -> {
                    Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                            .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

                    User admin = new User();
                    admin.setFirstName("Admin");
                    admin.setLastName("DreamShops");
                    admin.setEmail(adminEmail);
                    admin.setPassword(passwordEncoder.encode("Admin@123"));

                    admin.setRoles(new HashSet<>(List.of(adminRole))); // ✅ SAFE

                    userRepository.save(admin);
                    log.info("Default admin created: {}", adminEmail);
                }
        );
    }


    private void createDefaultUserIfNotExist() {
        String userEmail = "user@dreamshops.com";

        userRepository.findByEmail(userEmail).ifPresentOrElse(
                user -> log.info(" Default user already exists: {}", userEmail),
                () -> {
                    Role userRole = roleRepository.findByName("ROLE_USER")
                            .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

                    User user = new User();
                    user.setFirstName("Default");
                    user.setLastName("User");
                    user.setEmail(userEmail);
                    user.setPassword(passwordEncoder.encode("User@123"));

                    user.setRoles(new HashSet<>(List.of(userRole))); // ✅ SAFE

                    userRepository.save(user);
                    log.info(" Default user created: {}", userEmail);
                }
        );
    }
}