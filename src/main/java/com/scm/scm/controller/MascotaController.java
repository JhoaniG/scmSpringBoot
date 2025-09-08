package com.scm.scm.controller;

import com.scm.scm.dto.DiagnosticoDuenoDTO;
import com.scm.scm.dto.MascotaDTO;
import com.scm.scm.dto.RolDTO;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.service.MascotaService;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class MascotaController {
    private final MascotaService mascotaService;
    private final UsuarioRepositorio usuarioRepositorio;

    public MascotaController(MascotaService mascotaService, UsuarioRepositorio usuarioRepositorio) {
        this.mascotaService = mascotaService;
        this.usuarioRepositorio = usuarioRepositorio;
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

    //Listar masotas
    @GetMapping ("/api/mascotas/listar")
    public ResponseEntity<List<MascotaDTO>>getall(){
        List<MascotaDTO> mascotaDTOS=mascotaService.getAllMascotas();
        return  ResponseEntity.ok(mascotaDTOS);
    }
//Crear mascota
    @PostMapping ("/api/mascotas/crear")
    public ResponseEntity<MascotaDTO> crearMascota(@RequestBody MascotaDTO mascotaDTO) {
        MascotaDTO nuevaMascota = mascotaService.crearMascota(mascotaDTO);
        return ResponseEntity.ok(nuevaMascota);
    }

    @PutMapping("/api/mascotas/actualizar/{id}")
    public ResponseEntity<MascotaDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody MascotaDTO mascotaDTO) {

        MascotaDTO updatedTransaction = mascotaService.actualizarMascota(id, mascotaDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/api/mascotas/eliminar/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        mascotaService.eliminarMascota(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    @GetMapping("/mascotas/crear")
    public String mostrarFormulario(Model model, Authentication auth) {
        model.addAttribute("mascotaDTO", new MascotaDTO());

        // Pasar usuario al modelo
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepositorio.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));
            model.addAttribute("usuario", usuario);
            model.addAttribute("logueado", true);
        } else {
            model.addAttribute("logueado", false);
        }

        return "mascotas/crear";
    }

    // Guardar mascota
    @PostMapping("/mascotas/crear")
    public String crearMascota(@ModelAttribute MascotaDTO mascotaDTO, Authentication auth, Model model) {
        // Obtener usuario en sesión
        String emailUsuario = auth.getName(); // Spring Security guarda el username/email
        Usuario usuario = usuarioRepositorio.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));


        // Setear el id de usuario en el DTO
        mascotaDTO.setUsuarioId(usuario.getIdUsuario());

        // Guardar la mascota
        mascotaService.crearMascota(mascotaDTO);

        return "redirect:/mascotas/listar"; // redirige a la lista de mascotas
    }

    // Listar mascotas del usuario en sesión
    @GetMapping("/mascotas/listar")
    public String listarMascotas(Model model, Authentication auth) {
        String emailUsuario = auth.getName();
        Usuario usuario = usuarioRepositorio.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));
        // Pasar usuario al modelo
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();


            model.addAttribute("usuario", usuario);
            model.addAttribute("logueado", true);
        } else {
            model.addAttribute("logueado", false);
        }

        model.addAttribute("mascotas", mascotaService.getAllMascotas()
                .stream()
                .filter(m -> m.getUsuarioId().equals(usuario.getIdUsuario()))
                .toList());

        return "mascotas/listar"; // plantilla para listar
    }
}
