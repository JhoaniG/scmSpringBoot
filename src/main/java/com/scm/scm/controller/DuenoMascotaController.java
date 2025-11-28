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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.web.bind.annotation.ResponseBody;

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


    @Autowired
    private TemplateEngine templateEngine;

    // 3. AÑADE ESTE MÉTODO NUEVO
    @GetMapping("/dueno/mascotas/{idMascota}/ficha/preview")
    @ResponseBody // <-- Importante: Devuelve texto (HTML), no una vista
    public String previsualizarFicha(@PathVariable Long idMascota) {
        // Obtenemos los mismos datos que para el PDF
        Map<String, Object> datos = citaService.obtenerDatosHistorialClinico(idMascota);

        // Creamos el contexto de Thymeleaf
        Context context = new Context();
        context.setVariables(datos);

        // Procesamos la plantilla HTML y la devolvemos como String
        // Asegúrate de que la ruta "reports/historial-clinico-template" sea correcta
        return templateEngine.process("reports/historial-clinico-template", context);
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

    // 1. Vista para SELECCIONAR al veterinario (Paso previo)
    @GetMapping("/dueno/mascotas/{idMascota}/seleccionar-veterinario")
    public String seleccionarVeterinarioHistorial(@PathVariable("idMascota") Long mascotaId, Model model) {

        // Buscamos la mascota para mostrar su nombre en el título
        MascotaDTO mascota = mascotaService.obtenerMascotaPorId(mascotaId);

        // Obtenemos la lista de doctores con los que ha tenido citas
        List<VeterinarioDTO> listaVeterinarios = citaService.obtenerVeterinariosDeMascota(mascotaId);

        model.addAttribute("mascota", mascota);
        model.addAttribute("listaVeterinarios", listaVeterinarios);

        // IMPORTANTE: En esta vista NO usamos "veterinarioSeleccionado" todavía,
        // así que no hay riesgo de null pointer aquí si tu HTML es correcto.
        return "duenoMascota/seleccionar-veterinario";
    }

    // 2. Vista del HISTORIAL DETALLADO (Donde tenías el error)
    @GetMapping("/dueno/mascotas/{mascotaId}/historial/veterinario/{veterinarioId}")
    public String verHistorialFiltrado(@PathVariable Long mascotaId,
                                       @PathVariable Long veterinarioId,
                                       Model model) {

        // PASO 1: Garantizar que tenemos los datos de cabecera (Mascota y Veterinario)
        // Buscamos explícitamente al Veterinario por ID para asegurarnos de que no sea NULL en la vista
        Veterinario veterinario = veterinarioRepositorio.findById(veterinarioId)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado con ID: " + veterinarioId));

        MascotaDTO mascota = mascotaService.obtenerMascotaPorId(mascotaId);

        // PASO 2: Obtener los datos del historial (Citas, Dietas, Actividades)
        // Este mapa trae las listas, pero ya aseguramos el veterinario arriba
        Map<String, Object> datosHistorial = citaService.obtenerHistorialPorVeterinario(mascotaId, veterinarioId);

        // PASO 3: Cargar todo al modelo
        model.addAllAttributes(datosHistorial); // Carga las listas (citas, dietas, etc.)

        // Sobrescribimos/Aseguramos estos dos objetos para que el encabezado HTML nunca falle
        model.addAttribute("veterinarioSeleccionado", veterinario);
        model.addAttribute("mascota", mascota);

        return "duenoMascota/historialMascota";
    }
}
