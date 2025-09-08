package com.scm.scm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomSuccessHandler customSuccessHandler;

    // BCrypt para encriptar contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ⚡ Deshabilitar CSRF para APIs o formularios simples (modal)
                .csrf(csrf -> csrf.disable())

                // Configuración de rutas
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos y públicos
                        .requestMatchers(
                                "/", "/home",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/public/**"
                        ).permitAll()

                        // Registro de usuario
                        .requestMatchers(HttpMethod.GET, "/usuarios/create").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios/create").permitAll()

                        // Login
                        .requestMatchers("/login").permitAll()

                        // Roles protegidos
                        .requestMatchers("/admin/**").hasRole("Admin")
                        .requestMatchers("/veterinario/**").hasRole("Veterinario")
                        .requestMatchers("/dueno/**").hasRole("duenoMascota") // mayúscula inicial

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )

                // Login personalizado
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customSuccessHandler)
                        .permitAll()
                )

                // Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    // AuthenticationManager para login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
