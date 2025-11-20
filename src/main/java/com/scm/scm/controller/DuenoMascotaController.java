package com.scm.scm.controller;

import com.scm.scm.dto.*;
import com.scm.scm.model.Usuario;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.*;
import jakarta.servlet.http.HttpSession;
import org.modelmapper.ModelMapper;
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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
public class DuenoMascotaController {

    // --- Inyecciones ---
    private final MascotaService mascotaService;
    private final DietaService dietaService;
    private final ActividadFisicaService actividadFisicaService;
    private final CitaService citaService;
    private final PdfGenerationService pdfService;
    private final DiagnosticoDuenoService diagnosticoDuenoService; // Movido arriba para orden

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private VeterinarioRepositorio veterinarioRepositorio;

    // --- Constructor ---
    public DuenoMascotaController(MascotaService mascotaService, DietaService dietaService,
                                  ActividadFisicaService actividadFisicaService, CitaService citaService,
                                  PdfGenerationService pdfService, VeterinarioRepositorio veterinarioRepositorio,
                                  DiagnosticoDuenoService diagnosticoDuenoService) {
        this.mascotaService = mascotaService;
        this.dietaService = dietaService;
        this.actividadFisicaService = actividadFisicaService;
        this.citaService = citaService;
        this.pdfService = pdfService;
        this.veterinarioRepositorio = veterinarioRepositorio;
        this.diagnosticoDuenoService = diagnosticoDuenoService;
    }

    @ModelAttribute
    public void agregarAtributosGlobales(Model model, Authentication auth) {

        DiagnosticoDuenoDTO dto = new DiagnosticoDuenoDTO();
        dto.setFechaDiagnostico(LocalDate.now().toString());
        model.addAttribute("diagnosticoDTO", dto); // Solo una vez es suficiente

        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
            model.addAttribute("usuario", usuario);
            model.addAttribute("logueado", true);
        } else {
            model.addAttribute("logueado", false);
        }

        // Cargar lista global de veterinarios
        List<Veterinario> veterinarios = veterinarioRepositorio.findAll();
        model.addAttribute("veterinarios", veterinarios);

        model.addAttribute("mascotas", List.of());
    }

    @GetMapping("/dueno/index")
    public String duenoIndex(Model model, Authentication authentication, HttpSession session) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<MascotaDTO> misMascotas = mascotaService.getAllMascotas()
                .stream()
                .filter(m -> m.getUsuarioId().equals(usuario.getIdUsuario()))
                .toList();
        model.addAttribute("misMascotas", misMascotas);
        model.addAttribute("mascotas", mascotaService.getAllMascotas()); // Para el modal lateral
        model.addAttribute("usuario", usuario);
        model.addAttribute("logueado", true);

        session.setAttribute("usuarioSesion", usuario);

        return "duenoMascota/index";
    }

    @GetMapping("/dueno/mascotas")
    public String listarMascotas(Model model, HttpSession session) {
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

        // 1. Activas
        List<DietaVistaDTO> dietas = dietaService.obtenerDietasPorMascotaId(idMascota);
        model.addAttribute("dietas", dietas);

        // 2. Historial (NUEVO)
        List<DietaVistaDTO> historial = dietaService.obtenerHistorialDietasPorMascotaId(idMascota);
        model.addAttribute("historial", historial);

        return "duenoMascota/dietasPorMascota";
    }

    @GetMapping("/dueno/mascotas/actividad")
    public String listarMascotasAc(Model model, HttpSession session) {
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

    // --- AQUÍ ESTÁ EL CAMBIO IMPORTANTE ---
    @GetMapping("/duenoMascota/actividades")
    public String consultarActividades(@RequestParam("idMascota") Long idMascota, Model model) {

        // 1. Obtener Actividades ACTIVAS (En curso)
        List<ActividadVistaDTO> actividades = actividadFisicaService.obtenerActividadesPorMascotaId(idMascota);
        model.addAttribute("actividades", actividades);

        // 2. Obtener Actividades TERMINADAS (Historial)
        // Asegúrate de que este método exista en tu Interfaz y ServiceImpl como lo creamos antes
        List<ActividadVistaDTO> historial = actividadFisicaService.obtenerHistorialActividadesPorMascotaId(idMascota);
        model.addAttribute("historial", historial);

        return "duenoMascota/actividadesPorMascota";
    }
    // --------------------------------------

    @GetMapping("/dueno/mascotas/{idMascota}/ficha/pdf")
    public ResponseEntity<byte[]> exportarFichaPdf(@PathVariable Long idMascota) {
        Map<String, Object> datos = citaService.obtenerDatosHistorialClinico(idMascota);
        MascotaDTO mascotaDTO = (MascotaDTO) datos.get("mascota");
        String nombreArchivo = "ficha_" + mascotaDTO.getNombre().toLowerCase().replace(" ", "_") + ".pdf";
        byte[] pdfBytes = pdfService.generarPdfDesdeHtml("reports/ficha-mascota-template", datos);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", nombreArchivo);
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/dueno/mascota/historial/{idMascota}")
    public String verHistorialMascota(@PathVariable Long idMascota, Model model) {
        MascotaDTO mascota = mascotaService.obtenerMascotaPorId(idMascota);
        List<CitaDTO> citas = citaService.listarCitasPorMascota(idMascota);
        List<DiagnosticoDuenoDTO> diagnosticos = diagnosticoDuenoService.listarDiagnosticosPorMascota(idMascota);

        citas.sort(Comparator.comparing(CitaDTO::getFechaCita).reversed());
        diagnosticos.sort(Comparator.comparing(DiagnosticoDuenoDTO::getFechaDiagnostico).reversed());

        model.addAttribute("mascota", mascota);
        model.addAttribute("citas", citas);
        model.addAttribute("diagnosticos", diagnosticos);

        return "duenoMascota/historialMascota";
    }
}