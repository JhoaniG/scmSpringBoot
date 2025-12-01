package com.scm.scm.controller;

import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.dto.VeterinarioDTO; // Importar
import com.scm.scm.service.VeterinarioService; // Importar
import org.springframework.beans.factory.annotation.Autowired; // Importar
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List; // Importar

@Controller
public class HomeController {

    @Autowired
    private VeterinarioService veterinarioService; // Inyectar servicio

    @GetMapping("/home") // Y también para la raíz "/"
    public String home(Model model) {

        // 1. Obtener lista de veterinarios
        List<VeterinarioDTO> profesionales = veterinarioService.getAllVeterinarios();

        // (Opcional: Mostrar solo los primeros 3 para que no se rompa el diseño)
        if(profesionales.size() > 3) {
            profesionales = profesionales.subList(0, 3);
        }

        // 2. Pasar la lista a la vista
        model.addAttribute("profesionales", profesionales);

        // Para el modal de registro
        if (!model.containsAttribute("usuarioDTO")) {
            model.addAttribute("usuarioDTO", new UsuarioDTO());
        }

        return "home";
    }

    // Asegúrate de mapear también la raíz "/"
    @GetMapping("/")
    public String raiz(Model model) {
        return home(model);
    }
}