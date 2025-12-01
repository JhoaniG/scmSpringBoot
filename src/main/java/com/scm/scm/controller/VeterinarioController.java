package com.scm.scm.controller;

import com.scm.scm.dto.*;
import com.scm.scm.model.Diagnosticodueno;
import com.scm.scm.repository.*;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.Context;
import org.thymeleaf.TemplateEngine;
import java.util.Map;

import com.scm.scm.model.Mascota;
import com.scm.scm.model.Usuario;
import com.scm.scm.model.Veterinario;
import com.scm.scm.service.PdfGenerationService;
import com.scm.scm.service.VeterinarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class VeterinarioController {
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    private final VeterinarioService veterinarioService;
    private final VeterinarioRepositorio veterinarioRepositorio;
    private  final com.scm.scm.service.CitaService citaService;
    private  final PdfGenerationService pdfService;
    private final MascotaRepositorio mascotaRepositorio;
    private final com.scm.scm.service.MascotaService mascotaService;
    private final DiagnosticoDuenoRepositorio diagnosticoDuenoRepositorio;


    public VeterinarioController(VeterinarioRepositorio veterinarioRepositorio, VeterinarioService veterinarioService, VeterinarioRepositorio veterinarioRepositorio1, com.scm.scm.service.CitaService citaService, PdfGenerationService pdfService, MascotaRepositorio mascotaRepositorio, com.scm.scm.service.MascotaService mascotaService, DiagnosticoDuenoRepositorio diagnosticoDuenoRepositorio) {
        this.veterinarioService = veterinarioService;
        this.veterinarioRepositorio = veterinarioRepositorio1;
        this.citaService = citaService;
        this.pdfService = pdfService;
        this.mascotaRepositorio = mascotaRepositorio;
        this.mascotaService = mascotaService;
        this.diagnosticoDuenoRepositorio = diagnosticoDuenoRepositorio;
    }
    @Autowired
    private TemplateEngine templateEngine;
    @ModelAttribute
    public void agregarAtributosGlobales(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
            model.addAttribute("usuario", usuario);
            model.addAttribute("logueado", true);

            // Datos para la barra lateral
            model.addAttribute("diagnosticoDTO", new DiagnosticoDuenoDTO());
            model.addAttribute("mascotas", mascotaService.getAllMascotas());
            model.addAttribute("veterinarios", veterinarioRepositorio.findAll());
        } else {
            model.addAttribute("logueado", false);
        }
    }

    @GetMapping("/api/veterinarios/listar")
    public ResponseEntity<List<VeterinarioDTO>> listarVeterinairos() {
        List<VeterinarioDTO> veterinairos = veterinarioService.getAllVeterinarios();
        return ResponseEntity.ok(veterinairos);
    }

    @PostMapping("/api/veterinarios/crear")
    public ResponseEntity<VeterinarioDTO> crearVeterinario(@RequestBody VeterinarioDTO veterinarioDTO) {
        VeterinarioDTO createdVeterinario = veterinarioService.crearVeterinario(veterinarioDTO);
        return ResponseEntity.ok(createdVeterinario);
    }

    @GetMapping("/api/veterinarios/obtener/{id}")
    public ResponseEntity<VeterinarioDTO> obtenerVeterinarioPorId(Long id) {
        VeterinarioDTO veterinario = veterinarioService.obtenerVeterinarioPorId(id);
        return ResponseEntity.ok(veterinario);
    }

    @PutMapping("/api/veterinarios/actualizar/{id}")
    public ResponseEntity<VeterinarioDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody VeterinarioDTO veterinarioDTO) {
        VeterinarioDTO updatedTransaction = veterinarioService.actualizarVeterinario(id, veterinarioDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/api/veterinarios/eliminar/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        veterinarioService.eliminarVeterinario(id);
        return ResponseEntity.noContent().build();
    }

    // En tu VeterinarioController.java

    @GetMapping("veterinario/index")
    public String veterinarioIndex(Model model, Authentication authentication, jakarta.servlet.http.HttpSession session) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        session.setAttribute("usuario", usuario);
        model.addAttribute("usuario", usuario);

        Veterinario vet = veterinarioRepositorio.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        session.setAttribute("idVeterinario", vet.getIdVeterinario());
        session.setAttribute("veterinarioSesion", vet);

        // CORRECCIÓN: Pedimos una Página de 4 elementos, pero extraemos la Lista
        Pageable top4 = PageRequest.of(0, 4);
        Page<Mascota> paginaPacientes = mascotaRepositorio.findPacientesByVeterinarioId(vet.getIdVeterinario(), top4);

        // Pasamos la lista (.getContent()) para que el th:each del index funcione igual
        model.addAttribute("misPacientes", paginaPacientes.getContent());

        List<Diagnosticodueno> todosLosDiagnosticos = diagnosticoDuenoRepositorio.findByVeterinario_IdVeterinario(vet.getIdVeterinario());

        // Filtramos los que tengan la fecha de HOY
        String fechaHoyStr = LocalDate.now().toString(); // "2025-11-23"

        long nuevosReportes = todosLosDiagnosticos.stream()
                .filter(d -> d.getFechaDiagnostico().equals(fechaHoyStr))
                .count();

        if (nuevosReportes > 0) {
            model.addAttribute("notificacionReportes", "Tienes " + nuevosReportes + " nuevo(s) reporte(s) de síntomas recibidos hoy.");
        }



        return "veterinarios/index";
    }

    @GetMapping("/uploads/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> verFoto(@PathVariable String filename) throws MalformedURLException {
        Path path = Paths.get("uploads").resolve(filename).toAbsolutePath();
        Resource recurso = new UrlResource(path.toUri());
        if (!recurso.exists() || !recurso.isReadable()) {
            throw new RuntimeException("No se pudo leer el archivo: " + filename);
        }
        return ResponseEntity.ok().body(recurso);
    }




    @GetMapping("/citas")
    public String listarCitasVeterinario(HttpSession session, Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

        model.addAttribute("usuario", usuario);
        session.setAttribute("usuario", usuario);

        // Recuperar el veterinario desde sesión
        Long idVeterinario = (Long) session.getAttribute("idVeterinario");

        if (idVeterinario == null) {
            return "redirect:/login"; // Si no hay sesión, redirigir
        }

        // Llamar al servicio pasando el id del veterinario
        List<CitaDTO> citas = citaService.obtenerCitasPorVeterinario(idVeterinario);

        model.addAttribute("listaCitas", citas);
        return "veterinarios/citas"; // tu JSP
    }



    @GetMapping("/veterinario/mascotas/{idMascota}")
    public String verDetallesMascota(@PathVariable Long idMascota, Model model, Authentication authentication) {
        // 1. Obtenemos info del usuario logueado para el menú
        String email = authentication.getName();
        Usuario usuarioLogueado = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("usuario", usuarioLogueado);
        model.addAttribute("logueado", true); // <-- Añadido para consistencia

        // 2. Usamos el servicio que trae TODO el historial (el mismo del PDF)
        Map<String, Object> datosHistorial = citaService.obtenerDatosHistorialClinico(idMascota);

        // 3. Pasamos todos los datos a la vista usando el Map
        model.addAllAttributes(datosHistorial);
        // Esto añade automáticamente:
        // model.addAttribute("mascota", (MascotaDTO) datosHistorial.get("mascota"));
        // model.addAttribute("citas", (List<CitaDTO>) datosHistorial.get("citas"));
        // model.addAttribute("diagnosticos", (List<DiagnosticoDuenoDTO>) datosHistorial.get("diagnosticos"));
        // model.addAttribute("dietas", (List<DietaDTO>) datosHistorial.get("dietas"));
        // model.addAttribute("actividades", (List<ActividadFisicaDTO>) datosHistorial.get("actividades"));

        // 4. Devolvemos la vista
        return "veterinarios/detalles-mascota";
    }

    // MOSTRAR LA LISTA DE PACIENTES ---
    @GetMapping("/veterinario/mis-pacientes")
    public String listarMisPacientes(
            Model model,
            Authentication authentication,
            @RequestParam(value = "filtro", required = false) String filtro,
            @RequestParam(value = "page", defaultValue = "0") int page,
            // --- CAMBIO AQUÍ: Tamaño de página por defecto 6 ---
            @RequestParam(value = "size", defaultValue = "6") int size) {

        String email = authentication.getName();
        Usuario usuarioLogueado = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Veterinario veterinario = veterinarioRepositorio.findByUsuario(usuarioLogueado)
                .orElseThrow(() -> new RuntimeException("Perfil de veterinario no encontrado"));

        Pageable pageable = PageRequest.of(page, size);

        Page<Mascota> pacientesPage;

        if (filtro != null && !filtro.trim().isEmpty()) {
            pacientesPage = mascotaRepositorio.findPacientesByVeterinarioIdAndFiltro(veterinario.getIdVeterinario(), filtro, pageable);
        } else {
            pacientesPage = mascotaRepositorio.findPacientesByVeterinarioId(veterinario.getIdVeterinario(), pageable);
        }

        model.addAttribute("pacientesPage", pacientesPage);
        model.addAttribute("usuario", usuarioLogueado);
        model.addAttribute("filtro", filtro); // Devuelve el filtro a la vista

        return "veterinarios/mis-pacientes";
    }

    @GetMapping("/veterinario/mascotas/{idMascota}/historial/pdf")
    public ResponseEntity<byte[]> exportarHistorialPdf(@PathVariable Long idMascota) {
        // 1. Obtenemos todos los datos necesarios
        Map<String, Object> datos = citaService.obtenerDatosHistorialClinico(idMascota);

        // 2. Generamos el PDF usando nuestro servicio
        byte[] pdfBytes = pdfService.generarPdfDesdeHtml("reports/historial-clinico-template", datos);

        // 3. Preparamos la respuesta para que se descargue
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "historial_clinico.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("veterinario/calendario") // <-- La ruta es /veterinario + /calendario
    public String mostrarCalendario(Model model, Authentication auth) {

        // (Carga el usuario logueado para la barra lateral)
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
            model.addAttribute("usuario", usuario);
            model.addAttribute("logueado", true);
        }

        // Devuelve el archivo HTML: /templates/veterinario/calendario.html
        return "veterinarios/calendario";
    }
    @GetMapping("/api/historial/preview/{idMascota}")
    @ResponseBody // <-- ¡Importante! Devuelve un String (el HTML), no una vista.
    public String getHistorialPreview(@PathVariable Long idMascota) {

        // 1. Obtenemos los datos (exactamente igual que en el método del PDF)
        Map<String, Object> datos = citaService.obtenerDatosHistorialClinico(idMascota);

        // 2. Creamos el "Contexto" de Thymeleaf con esos datos
        Context context = new Context();
        context.setVariables(datos); // Pasa el Map completo (mascota, citas, dietas, etc.)

        // 3. Procesamos la plantilla HTML del PDF y la devolvemos como un String
        //    (Asegúrate que la ruta 'reports/historial-clinico-template' sea correcta)
        return templateEngine.process("reports/historial-clinico-template", context);
    }

    @Autowired private DietaRepositorio dietaRepositorio;
    @Autowired private ActividadFisicaRepositorio actividadFisicaRepositorio;

    @Autowired private CitaRepositorio citaRepositorio;

    // --- PERFIL VETERINARIO ---
    @GetMapping("/perfil")
    public String verPerfilProfesional(Model model, Authentication auth) {
        // Datos globales (Barra lateral, usuario)

        String email = auth.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElseThrow();
        Veterinario vet = veterinarioRepositorio.findByUsuario(usuario).orElseThrow();

        // --- ESTADÍSTICAS ---
        // 1. Pacientes (Usamos Pageable.unpaged() para contar todos si tu método devuelve Page, o .size() si devuelve List)
        // Asumiendo que findPacientesByVeterinarioId devuelve Page:
        long totalPacientes = mascotaRepositorio.findPacientesByVeterinarioId(vet.getIdVeterinario(), Pageable.unpaged()).getTotalElements();

        // 2. Citas Totales
        long totalCitas = citaRepositorio.countByVeterinario_IdVeterinario(vet.getIdVeterinario());

        // 3. Dietas Asignadas
        long totalDietas = dietaRepositorio.countByVeterinario_IdVeterinario(vet.getIdVeterinario());

        // 4. Actividades Asignadas
        long totalActividades = actividadFisicaRepositorio.countByVeterinario_IdVeterinario(vet.getIdVeterinario());

        // 5. Diagnósticos Atendidos
        long totalDiagnosticos = diagnosticoDuenoRepositorio.countByVeterinario_IdVeterinario(vet.getIdVeterinario());

        // Enviar al modelo
        model.addAttribute("totalPacientes", totalPacientes);
        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("totalDietas", totalDietas);
        model.addAttribute("totalActividades", totalActividades);
        model.addAttribute("totalDiagnosticos", totalDiagnosticos);
        model.addAttribute("veterinario", vet); // Datos específicos del vet (especialidad, etc.)

        return "veterinarios/perfil";
    }
}