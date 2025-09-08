package com.scm.scm.controller;

import com.scm.scm.dto.UsuarioDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(Model model) {
        if (!model.containsAttribute("usuarioDTO")) {
            model.addAttribute("usuarioDTO", new UsuarioDTO());
        }
        return "home";
    }
}