package com.scm.scm.controller;

import com.scm.scm.dto.*;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.service.ActividadFisicaService;
import com.scm.scm.service.DietaService;
import com.scm.scm.service.MascotaService;
import jakarta.servlet.http.HttpSession;
import org.modelmapper.ModelMapper; // Importa esta clase
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DuenoMascotaController {

    private final MascotaService mascotaService;
    private final DietaService dietaService;
     private final ActividadFisicaService actividadFisicaService;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private ModelMapper modelMapper; // Inyecta ModelMapper

    public DuenoMascotaController(MascotaService mascotaService, DietaService dietaService, ActividadFisicaService actividadFisicaService) {
        this.mascotaService = mascotaService;
        this.dietaService = dietaService;
        this.actividadFisicaService = actividadFisicaService;
    }

    @ModelAttribute
    public void agregarAtributosGlobales(Model model, Authentication auth) {
        // Siempre agregamos un objeto vacío para que el fragmento no falle
        model.addAttribute("diagnosticoDTO", new DiagnosticoDuenoDTO());

        // Si hay usuario autenticado
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
            model.addAttribute("usuario", usuario);
            model.addAttribute("logueado", true);
        } else {
            model.addAttribute("logueado", false);
        }

        // Inicializamos listas vacías por defecto
        model.addAttribute("mascotas", List.of());
        model.addAttribute("veterinarios", List.of());
    }

    @GetMapping("/dueno/index")
    public String duenoIndex(Model model, Authentication authentication, HttpSession session) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("mascotas", mascotaService.getAllMascotas()
                .stream()
                .filter(m -> m.getUsuarioId().equals(usuario.getIdUsuario()))
                .toList());

        List<Usuario> veterinarios = usuarioRepositorio.findByRolIdRol(3L);
        model.addAttribute("veterinarios", veterinarios);

        model.addAttribute("usuario", usuario);
        model.addAttribute("logueado", true);
        model.addAttribute("diagnosticoDTO", new DiagnosticoDuenoDTO());

        // Guardar el objeto Usuario en la sesión, no el DTO
        session.setAttribute("usuarioSesion", usuario);

        return "duenoMascota/index";
    }

    @GetMapping("/dueno/mascotas")
    public String listarMascotas(Model model, HttpSession session) {
        // Recuperar el objeto Usuario y convertirlo a DTO
        Object sessionUser = session.getAttribute("usuarioSesion");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        Usuario dueno = (Usuario) sessionUser;
        UsuarioDTO duenoDTO = modelMapper.map(dueno, UsuarioDTO.class);

        List<MascotaDTO> listaMascotas = mascotaService.obtenerMascotasPorDuenoId(duenoDTO.getIdUsuario());
        model.addAttribute("listaMascotas", listaMascotas);
        return "duenoMascota/mascotas";
    }

    @GetMapping("/duenoMascota/dietas")
    public String consultarDietas(@RequestParam("idMascota") Long idMascota, Model model) {
        List<DietaVistaDTO> dietas = dietaService.obtenerDietasPorMascotaId(idMascota);
        model.addAttribute("dietas", dietas);
        return "duenoMascota/dietasPorMascota";
    }

    @GetMapping("/dueno/mascotas/actividad")
    public String listarMascotasAc(Model model, HttpSession session) {
        // Recuperar el objeto Usuario y convertirlo a DTO
        Object sessionUser = session.getAttribute("usuarioSesion");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        Usuario dueno = (Usuario) sessionUser;
        UsuarioDTO duenoDTO = modelMapper.map(dueno, UsuarioDTO.class);

        List<MascotaDTO> listaMascotas = mascotaService.obtenerMascotasPorDuenoId(duenoDTO.getIdUsuario());
        model.addAttribute("listaMascotas", listaMascotas);
        return "duenoMascota/mascotasActividad";
    }


    @GetMapping("/duenoMascota/actividades")
    public String consultarActividades(@RequestParam("idMascota") Long idMascota, Model model) {
        List<ActividadVistaDTO> actividades = actividadFisicaService.obtenerActividadesPorMascotaId(idMascota);
        model.addAttribute("actividades", actividades);
        return "duenoMascota/actividadesPorMascota";
    }
}