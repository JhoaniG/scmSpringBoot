package com.scm.scm.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String redirectUrl = "/home"; // fallback

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_Admin")) {
                redirectUrl = "/admin/index";
                break;
            } else if (role.equals("ROLE_Veterinario")) {
                redirectUrl = "/veterinario/index";
                break;
            } else if (role.equals("ROLE_duenoMascota")) {
                redirectUrl = "/dueno/index";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}