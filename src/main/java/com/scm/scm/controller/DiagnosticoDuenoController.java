package com.scm.scm.controller;

import com.scm.scm.dto.*;
import com.scm.scm.model.Usuario;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.DiagnosticoDuenoService;
import com.scm.scm.service.MascotaService;
import com.scm.scm.service.UsuarioService;
import com.scm.scm.service.VeterinarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class DiagnosticoDuenoController {
    private  final DiagnosticoDuenoService diagnosticoDuenoService;
    private final UsuarioRepositorio usuarioRepositorio;
   private  final UsuarioService usuarioService;
   private  final VeterinarioService veterinarioService;
    private final MascotaService mascotaService;
     private final VeterinarioRepositorio veterinarioRepositorio;

    public DiagnosticoDuenoController(DiagnosticoDuenoService diagnosticoDuenoService, UsuarioRepositorio usuarioRepositorio, UsuarioService usuarioService, VeterinarioService veterinarioService, MascotaService mascotaService, VeterinarioRepositorio veterinarioRepositorio) {
        this.diagnosticoDuenoService = diagnosticoDuenoService;
        this.usuarioRepositorio = usuarioRepositorio;
        this.usuarioService = usuarioService;
        this.veterinarioService = veterinarioService;

        this.mascotaService = mascotaService;

        this.veterinarioRepositorio = veterinarioRepositorio;
    }

    @GetMapping ("/api/diagnosticos-duenos/listar")
    public ResponseEntity<List<DiagnosticoDuenoDTO>>listartodos(){
        List<DiagnosticoDuenoDTO> diagnosticosDuenos = diagnosticoDuenoService.ListartodosDiagnosticosDueno();
        return ResponseEntity.ok(diagnosticosDuenos);
    }

    // Crear un nuevo diagnóstico de dueño
    @PostMapping("/api/diagnosticos-duenos/crear")
    public ResponseEntity<DiagnosticoDuenoDTO> crearDiagnosticoDueno(@RequestBody  DiagnosticoDuenoDTO diagnosticoDuenoDTO) {
        DiagnosticoDuenoDTO createdDiagnosticoDueno = diagnosticoDuenoService.crearDiagnosticoDueno(diagnosticoDuenoDTO);
        return ResponseEntity.ok(createdDiagnosticoDueno);
    }
    @PutMapping("/api/diagnosticos-duenos/actualizar/{id}")
    public ResponseEntity<DiagnosticoDuenoDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody DiagnosticoDuenoDTO diagnosticoDuenoDTO) {

        DiagnosticoDuenoDTO updatedTransaction = diagnosticoDuenoService.actualizarDiagnosticoDueno(id, diagnosticoDuenoDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/api/diagnosticos-duenos/eliminar/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        diagnosticoDuenoService.eliminarDiagnosticoDueno(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }



    @GetMapping("/diagnosticos/crear")
    public String mostrarFormularioDiagnostico(Model model, Authentication auth) {
        model.addAttribute("diagnosticoDTO", new DiagnosticoDuenoDTO());

        // Obtener usuario en sesión
        String email = auth.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Pasar mascotas del usuario al modelo
        model.addAttribute("mascotas", mascotaService.getAllMascotas()
                .stream()
                .filter(m -> m.getUsuarioId().equals(usuario.getIdUsuario()))
                .toList());

        // Pasar todos los veterinarios al modelo
        List<Veterinario> veterinarios = veterinarioRepositorio.findAll(); // Suponiendo que el ID del rol veterinario es 3
        model.addAttribute("veterinarios", veterinarios);

        model.addAttribute("usuario", usuario);
        model.addAttribute("logueado", true);

        return "diagnosticos/crear";
    }

    @PostMapping("/diagnosticos/crear")
    public String crearDiagnostico(@ModelAttribute DiagnosticoDuenoDTO diagnosticoDTO, RedirectAttributes redirectAttributes) {
        try {
            diagnosticoDuenoService.crearDiagnosticoDueno(diagnosticoDTO);
            // 3. Añade el mensaje de éxito aquí
            redirectAttributes.addFlashAttribute("mensajeExito", "Reporte de síntomas registrado exitosamente.");
        } catch (Exception e) {
            // 4. (Opcional pero recomendado) Maneja el error
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar el reporte: " + e.getMessage());
        }

        // La redirección sigue siendo la misma
        return "redirect:/dueno/index";
    }

    @GetMapping("/diagnosticos/listar")
    public String listarDiagnosticosVeterinario(Model model, Authentication auth) {
        Usuario usuario = usuarioRepositorio.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Veterinario vet = veterinarioRepositorio.findByUsuarioId(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("No se encontró un veterinario asociado al usuario"));

        List<DiagnosticoDuenoDTO> lista = diagnosticoDuenoService.listarDiagnosticosPorVeterinario(vet.getIdVeterinario());
        model.addAttribute("citaDTO", new CitaDTO());
        model.addAttribute("listaDiagnosticos", lista);
        model.addAttribute("usuario", usuario); // <---- esto faltaba

        return "diagnosticos/lista";
    }


    @GetMapping("/api/veterinarios-por-tipo-enfermedad")
    @ResponseBody
    public List<UsuarioDTO> getVeterinariosPorEspecialidad(@RequestParam String tipoEnfermedad) { // <-- CAMBIO AQUÍ

        String especialidadRequerida;

        // Mapea el tipo de enfermedad a una especialidad
        switch (tipoEnfermedad) {
            case "Problemas Digestivos / Nutricionales":
                especialidadRequerida = "Nutrición Veterinaria";
                break;
            case "Problemas Ortopédicos / Movilidad":
                especialidadRequerida = "Fisioterapia y Rehabilitación";
                break;
            case "Problemas de Comportamiento":
                especialidadRequerida = "Etología (Comportamiento Animal)";
                break;
            default:
                especialidadRequerida = "Medicina General";
        }

        List<Veterinario> especialistas = veterinarioRepositorio.findByEspecialidad(especialidadRequerida);

        // Siempre incluye a los médicos generales como opción de respaldo
        if (!especialidadRequerida.equals("Medicina General")) {
            especialistas.addAll(veterinarioRepositorio.findByEspecialidad("Medicina General"));
        }

        // Convierte la lista de Entidades a una lista de UsuarioDTOs
        return especialistas.stream()
                .map(v -> {
                    // Mapea a un UsuarioDTO simple para el JSON
                    UsuarioDTO dto = new UsuarioDTO();
                    dto.setIdUsuario(v.getIdVeterinario()); // Usamos el ID de Veterinario para el value
                    dto.setNombre(v.getUsuario().getNombre() + " " + v.getUsuario().getApellido());
                    dto.setEspecialidad(v.getEspecialidad());
                    return dto; // <-- Retorna UsuarioDTO
                })
                .distinct()
                .collect(Collectors.toList());
    }    }


