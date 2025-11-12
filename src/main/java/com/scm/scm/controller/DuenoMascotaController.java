package com.scm.scm.controller;

import com.scm.scm.dto.*;
import com.scm.scm.model.Usuario;
import com.scm.scm.model.Veterinario; // <-- 1. AÑADIR ESTE IMPORT
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio; // <-- 2. AÑADIR ESTE IMPORT
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

    // --- Tus inyecciones existentes (están bien) ---
    private final MascotaService mascotaService;
    private final DietaService dietaService;
    private final ActividadFisicaService actividadFisicaService;
    private final CitaService citaService;
    private final PdfGenerationService pdfService;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private ModelMapper modelMapper;

    // --- 3. AÑADIR ESTA INYECCIÓN ---
    @Autowired
    private VeterinarioRepositorio veterinarioRepositorio;

    // --- 4. AÑADIR EL REPOSITORIO AL CONSTRUCTOR ---
    public DuenoMascotaController(MascotaService mascotaService, DietaService dietaService,
                                  ActividadFisicaService actividadFisicaService, CitaService citaService,
                                  PdfGenerationService pdfService, VeterinarioRepositorio veterinarioRepositorio,
                                  DiagnosticoDuenoService diagnosticoDuenoService) { // <-- Añadir aquí
        this.mascotaService = mascotaService;
        this.dietaService = dietaService;
        this.actividadFisicaService = actividadFisicaService;
        this.citaService = citaService;
        this.pdfService = pdfService;
        this.veterinarioRepositorio = veterinarioRepositorio;
        this.diagnosticoDuenoService = diagnosticoDuenoService; // <-- Y aquí
    }
    private final DiagnosticoDuenoService diagnosticoDuenoService;

    @ModelAttribute
    public void agregarAtributosGlobales(Model model, Authentication auth) {

        DiagnosticoDuenoDTO dto = new DiagnosticoDuenoDTO();
        // 2. Ponle la fecha actual del servidor (convertida a String)
        dto.setFechaDiagnostico(LocalDate.now().toString());
        // 3. Pásalo al modelo
        model.addAttribute("diagnosticoDTO", dto);
        model.addAttribute("diagnosticoDTO", new DiagnosticoDuenoDTO());

        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
            model.addAttribute("usuario", usuario);
            model.addAttribute("logueado", true);
        } else {
            model.addAttribute("logueado", false);
        }

        // --- 5. CORRECCIÓN EN EL MÉTODO GLOBAL ---
        // Cargamos la lista correcta de Veterinarios aquí para que esté disponible en todas las vistas
        List<Veterinario> veterinarios = veterinarioRepositorio.findAll();
        model.addAttribute("veterinarios", veterinarios);

        // Dejamos que los controladores específicos sobreescriban "mascotas" si es necesario
        model.addAttribute("mascotas", List.of());
    }

    @GetMapping("/dueno/index")
    public String duenoIndex(Model model, Authentication authentication, HttpSession session) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // --- 6. CORRECCIÓN DE NOMBRES DE VARIABLE ---
        // La lista para las tarjetas de la página principal se llamará "misMascotas"
        List<MascotaDTO> misMascotas = mascotaService.getAllMascotas()
                .stream()
                .filter(m -> m.getUsuarioId().equals(usuario.getIdUsuario()))
                .toList();
        model.addAttribute("misMascotas", misMascotas);

        // La lista para la barra lateral se llamará "mascotas" (para el modal)
        // (La cargamos en @ModelAttribute, pero podemos ser explícitos si el modal la necesita completa)
        model.addAttribute("mascotas", mascotaService.getAllMascotas());

        // 'veterinarios' ya fue añadido correctamente por agregarAtributosGlobales
        // No necesitamos esta línea incorrecta:
        // List<Usuario> veterinarios = usuarioRepositorio.findByRolIdRol(3L);
        // model.addAttribute("veterinarios", veterinarios);

        model.addAttribute("usuario", usuario);
        model.addAttribute("logueado", true);
        model.addAttribute("diagnosticoDTO", new DiagnosticoDuenoDTO());

        session.setAttribute("usuarioSesion", usuario);

        return "duenoMascota/index";
    }

    // --- EL RESTO DE MÉTODOS NO CAMBIAN ---

    @GetMapping("/dueno/mascotas")
    public String listarMascotas(Model model, HttpSession session) {
        // ... (tu código original)
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
        // ... (tu código original)
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
        // ... (tu código original)
        List<ActividadVistaDTO> actividades = actividadFisicaService.obtenerActividadesPorMascotaId(idMascota);
        model.addAttribute("actividades", actividades);
        return "duenoMascota/actividadesPorMascota";
    }

    @GetMapping("/dueno/mascotas/{idMascota}/ficha/pdf")
    public ResponseEntity<byte[]> exportarFichaPdf(@PathVariable Long idMascota) {
        // ... (tu código original)
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

        // 1. Obtener la mascota para el título
        MascotaDTO mascota = mascotaService.obtenerMascotaPorId(idMascota);

        // 2. Obtener las listas (usando los métodos que creamos en el paso anterior)
        List<CitaDTO> citas = citaService.listarCitasPorMascota(idMascota);
        List<DiagnosticoDuenoDTO> diagnosticos = diagnosticoDuenoService.listarDiagnosticosPorMascota(idMascota);

        // 3. Ordenar las listas por fecha (más reciente primero)
        citas.sort(Comparator.comparing(CitaDTO::getFechaCita).reversed());
        diagnosticos.sort(Comparator.comparing(DiagnosticoDuenoDTO::getFechaDiagnostico).reversed());

        // 4. Añadir todo al modelo
        model.addAttribute("mascota", mascota);
        model.addAttribute("citas", citas);
        model.addAttribute("diagnosticos", diagnosticos);

        // 5. Devolver el nombre del nuevo archivo HTML
        return "duenoMascota/historialMascota";
    }
}