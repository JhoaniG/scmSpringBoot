package com.scm.scm.controller;

import com.scm.scm.dto.*;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.service.*;
import jakarta.servlet.http.HttpSession;
import org.modelmapper.ModelMapper; // Importa esta clase
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class DuenoMascotaController {

    private final MascotaService mascotaService;
    private final DietaService dietaService;
     private final ActividadFisicaService actividadFisicaService;
     private final CitaService citaService;
    private final PdfGenerationService pdfService;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private ModelMapper modelMapper; // Inyecta ModelMapper

    public DuenoMascotaController(MascotaService mascotaService, DietaService dietaService, ActividadFisicaService actividadFisicaService, CitaService citaService, PdfGenerationService pdfService) {
        this.mascotaService = mascotaService;
        this.dietaService = dietaService;
        this.actividadFisicaService = actividadFisicaService;
        this.citaService = citaService;
        this.pdfService = pdfService;
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


    @GetMapping("/dueno/mascotas/{idMascota}/ficha/pdf")
    public ResponseEntity<byte[]> exportarFichaPdf(@PathVariable Long idMascota) {
        // 1. Obtenemos los datos (esto no cambia)
        Map<String, Object> datos = citaService.obtenerDatosHistorialClinico(idMascota);

        // --- INICIO DE LA CORRECCIÓN ---
        // 2. Extraemos el objeto del mapa y lo "casteamos" a MascotaDTO
        MascotaDTO mascotaDTO = (MascotaDTO) datos.get("mascota");

        // 3. Ahora sí, usamos el objeto con su tipo correcto para crear el nombre del archivo
        String nombreArchivo = "ficha_" + mascotaDTO.getNombre().toLowerCase().replace(" ", "_") + ".pdf";
        // --- FIN DE LA CORRECCIÓN ---

        // 4. Generamos el PDF (esto no cambia)
        byte[] pdfBytes = pdfService.generarPdfDesdeHtml("reports/ficha-mascota-template", datos);

        // 5. Preparamos la respuesta para la descarga (esto no cambia)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", nombreArchivo);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}