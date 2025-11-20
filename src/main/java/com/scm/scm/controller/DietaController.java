package com.scm.scm.controller;

import com.scm.scm.dto.*;
import com.scm.scm.model.Dieta;
import com.scm.scm.model.Mascota;
import com.scm.scm.model.Usuario;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.DietaRepositorio;
import com.scm.scm.repository.MascotaRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.DietaService;
import com.scm.scm.service.MascotaService;
import com.scm.scm.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity; // <-- Importa esto
import org.springframework.http.HttpStatus; // <-- Importa esto
import jakarta.validation.Valid; // <-- Importa esto
import org.springframework.validation.BindingResult; // <-- Importa esto

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dieta")
public class DietaController {

    private final DietaService dietaService;
    private final UsuarioService usuarioService;
    private final MascotaService mascotaService;
    private  final com.scm.scm.repository.UsuarioRepositorio usuarioRepositorio;
    private final VeterinarioRepositorio  veterinarioRepositorio;

    public DietaController(DietaService dietaService, UsuarioService usuarioService, MascotaService mascotaService, com.scm.scm.repository.UsuarioRepositorio usuarioRepositorio, VeterinarioRepositorio veterinarioRepositorio) {
        this.dietaService = dietaService;
        this.usuarioService = usuarioService;
        this.mascotaService = mascotaService;
        this.usuarioRepositorio = usuarioRepositorio;
        this.veterinarioRepositorio = veterinarioRepositorio;
    }
    @Autowired
    private DietaRepositorio dietaRepository;

    @Autowired
    private MascotaRepositorio mascotaRepository;

    @Autowired
    private UsuarioRepositorio usuarioRepository;
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
    @GetMapping("/seleccionar-dueno")
    public String seleccionarDueno(Model model, Authentication auth, HttpSession session) {

        // 1. Obtener el ID del Veterinario logueado
        String email = auth.getName();
        Usuario usuarioLogueado = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Veterinario veterinario = veterinarioRepositorio.findByUsuario(usuarioLogueado) // Asumo que tienes "findByUsuario"
                .orElseThrow(() -> new RuntimeException("Perfil de veterinario no encontrado"));

        Long veterinarioId = veterinario.getIdVeterinario();

        // 2. Obtener SÓLO los dueños que ya han terminado una cita con este veterinario
        List<UsuarioDTO> listaDuenos = usuarioService.obtenerDuenosConCitasTerminadas(veterinarioId);

        model.addAttribute("listaDuenos", listaDuenos);

        // 3. (Opcional) Mensaje si la lista está vacía
        if(listaDuenos.isEmpty()) {
            model.addAttribute("mensajeInfo", "Aún no tienes pacientes con citas terminadas. Marca una cita como 'Terminada' para poder asignarle una dieta.");
        }

        return "veterinarios/seleccionarDueno";
    }

    @GetMapping("/crear/seleccionar-mascota")
    public String seleccionarMascota(@RequestParam("duenoId") Long duenoId, Model model, HttpSession session) {
        Long idVeterinario = (Long) session.getAttribute("idVeterinario");
        List<MascotaDTO> listaMascotas = mascotaService.obtenerMascotasPorDuenoId(duenoId);
        model.addAttribute("listaMascotas", listaMascotas);
        model.addAttribute("idVeterinario", idVeterinario);

        // --- ¡NUEVO! Pasamos un DTO vacío para el th:object ---
        DietaDTO dietaDTO = new DietaDTO();
        dietaDTO.setVeterinarioId(idVeterinario); // Pre-llenamos el ID del vet
        model.addAttribute("dietaDTO", dietaDTO);

        return "veterinarios/crearDieta";
    }
    @PostMapping("/crear")
    public String crearDieta(
            @Valid @ModelAttribute("dietaDTO") DietaDTO dietaDTO, // 1. Añade @Valid y @ModelAttribute
            BindingResult result, // 2. Añade BindingResult para atrapar errores
            @RequestParam(value = "archivoFoto", required = false) MultipartFile archivoFoto,
            RedirectAttributes redirectAttributes,
            Model model, // 3. Añade Model
            HttpSession session) { // 4. Añade HttpSession

        // 5. LÓGICA DE VALIDACIÓN
        if (result.hasErrors()) {
            // Si hay errores, volvemos al formulario, NO redirigimos

            // Necesitamos recargar los datos para el select de mascotas
            Long duenoId = mascotaService.obtenerMascotaPorId(dietaDTO.getMascotaId()).getUsuarioId();
            List<MascotaDTO> listaMascotas = mascotaService.obtenerMascotasPorDuenoId(duenoId);
            model.addAttribute("listaMascotas", listaMascotas);
            model.addAttribute("idVeterinario", (Long) session.getAttribute("idVeterinario"));

            return "veterinarios/crearDieta"; // Devuelve la VISTA (así se mantienen los errores)
        }

        // Si la validación pasa, intentamos guardar
        try {
            dietaDTO.setArchivoFoto(archivoFoto);
            dietaService.crearDieta(dietaDTO);

            redirectAttributes.addFlashAttribute("mensajeExito", "¡Dieta creada! Puede registrar otra.");
            MascotaDTO mascota = mascotaService.obtenerMascotaPorId(dietaDTO.getMascotaId());
            Long duenoId = mascota.getUsuarioId();
            redirectAttributes.addAttribute("duenoId", duenoId);

            return "redirect:/dieta/crear/seleccionar-mascota";

        } catch (Exception e) {
            // El catch ahora solo atrapa errores inesperados (ej. base de datos)
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear la dieta: " + e.getMessage());
            return "redirect:/dieta/seleccionar-dueno";
        }
    }


    @PostMapping("/crear/foto")
    @ResponseBody
    public ResponseEntity<?> crearDietaConFoto(
            @RequestParam("mascotaId") Integer mascotaId,
            @RequestParam("tipoDieta") String tipoDieta,
            @RequestParam("fechaInicio") String fechaInicio,
            @RequestParam("fechaFin") String fechaFin,
            @RequestParam("descripcion") String descripcion,
            // 1. Marcamos la foto como NO requerida
            @RequestParam(value = "foto", required = false) MultipartFile foto
    ) {

        try {
            // ... (Tu lógica de Mascota y Veterinario está bien) ...
            Mascota mascota = mascotaRepository.findById(Long.valueOf(mascotaId))
                    .orElseThrow(() -> new RuntimeException("Mascota no existe"));
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String correo = auth.getName();
            Usuario usuarioVet = usuarioRepository.findByEmail(correo)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Veterinario veterinario = veterinarioRepositorio.findByUsuario(usuarioVet)
                    .orElseThrow(() -> new RuntimeException("Perfil veterinario no encontrado"));


            // 2. Iniciamos el nombre del archivo como null
            String nombreArchivo = null;

            // 3. Solo procesamos la foto SI existe y NO está vacía
            if (foto != null && !foto.isEmpty()) {
                // 3. Guardar foto
                nombreArchivo = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
                Path ruta = Paths.get("uploads/dietas/" + nombreArchivo);

                Files.createDirectories(ruta.getParent());
                Files.write(ruta, foto.getBytes());
            }

            // 4. Crear dieta
            Dieta dieta = new Dieta();
            dieta.setMascota(mascota);
            dieta.setVeterinario(veterinario);
            dieta.setTipoDieta(tipoDieta);
            dieta.setDescripcion(descripcion);
            dieta.setFechaInicio(LocalDate.parse(fechaInicio));
            dieta.setFechaFin(LocalDate.parse(fechaFin));

            // 4. Asignamos el nombre (o se queda null si no hubo foto)
            dieta.setFoto(nombreArchivo);

            dietaRepository.save(dieta);

            return ResponseEntity.ok(Map.of("mensaje", "Dieta registrada"));

        } catch (Exception e) {
            e.printStackTrace(); // Es bueno ver el error en la consola de Spring
            return ResponseEntity.badRequest().body(Map.of("error", "Datos inválidos o incompletos"));
        }
    }

    @PostMapping("/terminar/{id}")
    @ResponseBody // Importante para que el JavaScript reciba el JSON
    public ResponseEntity<?> terminarDieta(@PathVariable("id") Long id) {
        try {
            // LLAMAMOS AL SERVICIO (que ya tiene la lógica de cambiar la fecha a ayer)
            dietaService.terminarDieta(id);

            return ResponseEntity.ok(Map.of("mensaje", "Dieta marcada como terminada"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se pudo terminar la dieta: " + e.getMessage()));
        }
    }

}



