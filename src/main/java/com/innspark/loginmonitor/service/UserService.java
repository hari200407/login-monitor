package com.innspark.loginmonitor.service;

import com.innspark.loginmonitor.entity.Role;
import com.innspark.loginmonitor.entity.User;
import com.innspark.loginmonitor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        createOrUpdateUser(
                "user",
                "user123",
                "YOUR_EMAIL@gmail.com",
                Role.USER
        );

        createOrUpdateUser(
                "admin",
                "admin123",
                "YOUR_EMAIL@gmail.com",
                Role.ADMIN
        );

        createOrUpdateUser(
                "superadmin",
                "superadmin123",
                "YOUR_EMAIL@gmail.com",
                Role.SUPERADMIN
        );
    }

    private void createOrUpdateUser(
            String username,
            String password,
            String email,
            Role role) {

        User user = userRepository
                .findByUsername(username)
                .orElse(null);

        if (user == null) {

            user = new User();

            user.setUsername(username);
            user.setPassword(
                    passwordEncoder.encode(password)
            );
            user.setRole(role);
            user.setEmail(email);
            user.setEnabled(true);

            userRepository.save(user);

        } else if (user.getEmail() == null ||
                user.getEmail().isBlank()) {

            user.setEmail(email);

            userRepository.save(user);
        }
    }
}