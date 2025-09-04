package com.scm.scm.utils;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordGenerator implements CommandLineRunner {
    @Override
    public void run(String... args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "1234"; // contraseña en texto plano
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Hash generado para 1234: " + encodedPassword);
    }
}
