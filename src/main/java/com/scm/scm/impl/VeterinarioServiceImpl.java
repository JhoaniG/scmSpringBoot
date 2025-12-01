package com.scm.scm.impl;

import com.scm.scm.dto.SolicitudVeterinarioDTO;
import com.scm.scm.dto.VeterinarioDTO;
import com.scm.scm.model.Rol;
import com.scm.scm.model.SolicitudVeterinario;
import com.scm.scm.model.Usuario;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.RolRepositorio;
import com.scm.scm.repository.SolicitudVeterinarioRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.EmailService;
import com.scm.scm.service.VeterinarioService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
public class VeterinarioServiceImpl implements VeterinarioService {

    private  final VeterinarioRepositorio veterinarioRepositorio;
    private final ModelMapper modelMapper;
    private  final UsuarioRepositorio usuarioRepositorio;

    public VeterinarioServiceImpl(VeterinarioRepositorio veterinarioRepositorio, ModelMapper modelMapper, UsuarioRepositorio usuarioRepositorio) {
        this.veterinarioRepositorio = veterinarioRepositorio;
        this.modelMapper = modelMapper;
        this.usuarioRepositorio = usuarioRepositorio;
    }
    public Veterinario findByUsuarioId(Long idUsuario) {
        return veterinarioRepositorio.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
    }
    @Autowired
    private SolicitudVeterinarioRepositorio solicitudRepo;
    @Autowired private UsuarioRepositorio usuarioRepo;
    @Autowired private RolRepositorio rolRepo;
    @Override
    public VeterinarioDTO crearVeterinario(VeterinarioDTO veterinarioDTO) {
        Veterinario veterinario=modelMapper.map( veterinarioDTO, Veterinario.class);
        if(veterinarioDTO.getUsuarioId() != null){
            Usuario usuario= usuarioRepositorio.findById(veterinarioDTO.getUsuarioId()).orElseThrow(()-> new RuntimeException( "No existe un usuario con el ID: " + veterinarioDTO.getUsuarioId()));
            veterinario.setUsuario(usuario);
        }

        veterinario = veterinarioRepositorio.save(veterinario);
        return modelMapper.map(veterinario, VeterinarioDTO.class);
    }

    @Override
    public VeterinarioDTO obtenerVeterinarioPorEmail(String email) {
        return null;
    }

    @Override
    public VeterinarioDTO obtenerVeterinarioPorId(Long id) {
        Veterinario veterinario= veterinarioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe un veterinario con el ID: " + id));
        return modelMapper.map(veterinario, VeterinarioDTO.class);
    }

    @Override
    public VeterinarioDTO actualizarVeterinario(Long id, VeterinarioDTO veterinarioDTO) {
        if (veterinarioRepositorio.existsById(id)) {
            Veterinario veterinario = veterinarioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe un veterinario con el ID: " + id));
            veterinario.setEspecialidad(veterinarioDTO.getEspecialidad());
            veterinario.setVeterinaria(veterinarioDTO.getVeterinaria());
            if (veterinarioDTO.getUsuarioId() != null) {
                Usuario usuario = usuarioRepositorio.findById(veterinarioDTO.getUsuarioId())
                        .orElseThrow(() -> new RuntimeException("No existe un usuario con el ID: " + veterinarioDTO.getUsuarioId()));
                veterinario.setUsuario(usuario);
            }
            veterinario = veterinarioRepositorio.save(veterinario);
            return modelMapper.map(veterinario, VeterinarioDTO.class);
        }else {
            throw new RuntimeException("No existe un veterinario con el ID: " + id);
        }


    }

    @Override
    public void eliminarVeterinario(Long id) {
        // 1. Verificamos si existe
        if (!veterinarioRepositorio.existsById(id)) {
            throw new RuntimeException("No existe un veterinario con el ID: " + id);
        }

        // 2. Intentamos eliminar y capturamos el error de integridad
        try {
            veterinarioRepositorio.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // Este error ocurre si el veterinario tiene citas u otros registros asociados
            throw new RuntimeException("No se puede eliminar este veterinario porque tiene citas asociadas.");
        }
    }

    @Override
    public List<VeterinarioDTO> getAllVeterinarios() {
        List<VeterinarioDTO> veterinarioDTOS=veterinarioRepositorio .findAll()
                .stream()
                .map(veterinario -> modelMapper.map(veterinario, VeterinarioDTO.class))
                .toList();
        return veterinarioDTOS;
    }

    private VeterinarioDTO convertirADTO(Veterinario veterinario) {
        VeterinarioDTO dto = modelMapper.map(veterinario, VeterinarioDTO.class);
        if (veterinario.getUsuario() != null) {
            dto.setNombreUsuario(veterinario.getUsuario().getNombre() + " " + veterinario.getUsuario().getApellido());
            dto.setEmailUsuario(veterinario.getUsuario().getEmail());
            dto.setFotoUsuario(veterinario.getUsuario().getFoto());
        }
        return dto;
    }

    @Override
    public Page<VeterinarioDTO> getAllVeterinariosPaginados(Pageable pageable) {
        Page<Veterinario> paginaVeterinarios = veterinarioRepositorio.findAll(pageable);
        return paginaVeterinarios.map(this::convertirADTO);
    }

    @Override
    public Veterinario buscarPorUsuario(Usuario usuario) {
        return veterinarioRepositorio.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("No se encontró un perfil de veterinario para el usuario: " + usuario.getEmail()));
    }

    @Override
    public void crearSolicitud(SolicitudVeterinarioDTO dto, Long idUsuario) {
        // 1. Verificar si ya tiene una pendiente
        if (solicitudRepo.existsByUsuario_IdUsuarioAndEstado(idUsuario, "PENDIENTE")) {
            throw new RuntimeException("Ya tienes una solicitud en proceso.");
        }

        Usuario usuario = usuarioRepo.findById(idUsuario).orElseThrow();

        // --- 2. GUARDAR ARCHIVO FÍSICAMENTE (CORREGIDO) ---
        String nombreArchivo = null;
        MultipartFile archivo = dto.getArchivoHojaVida(); // Asegúrate de tener este getter en el DTO

        if (archivo != null && !archivo.isEmpty()) {
            try {
                // Definir la carpeta (puedes usar una constante o propiedad)

                String uploadDir = System.getProperty("user.dir") + "/uploads/hojas-vida/";


                // Crear el directorio si no existe
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generar nombre único
                nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();

                // Guardar el archivo
                Path filePath = uploadPath.resolve(nombreArchivo);
                Files.write(filePath, archivo.getBytes());

            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la hoja de vida: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("La hoja de vida es obligatoria.");
        }
        // --------------------------------------------------

        // 3. Crear Entidad
        SolicitudVeterinario solicitud = new SolicitudVeterinario();
        solicitud.setUsuario(usuario);
        solicitud.setEspecialidad(dto.getEspecialidad());
        solicitud.setVeterinaria(dto.getVeterinaria());
        solicitud.setPerfilProfesional(dto.getPerfilProfesional());
        solicitud.setHojaVidaPdf(nombreArchivo); // Guardamos el nombre del archivo que sí existe
        solicitud.setFechaSolicitud(LocalDate.now());
        solicitud.setEstado("PENDIENTE");

        solicitudRepo.save(solicitud);
    }
    @Override
    public List<SolicitudVeterinario> listarSolicitudesPendientes() {
        return solicitudRepo.findByEstado("PENDIENTE");
    }

    @Autowired
    private EmailService emailService; // <-- INYECTA EL SERVICIO DE EMAIL

    // ... (Constructor y otros métodos) ...

    @Override
    @Transactional
    public void aprobarSolicitud(Long idSolicitud) {
        SolicitudVeterinario solicitud = solicitudRepo.findById(idSolicitud).orElseThrow();

        // 1. Crear el Veterinario real
        Veterinario nuevoVet = new Veterinario();
        nuevoVet.setUsuario(solicitud.getUsuario());
        nuevoVet.setEspecialidad(solicitud.getEspecialidad());
        nuevoVet.setVeterinaria(solicitud.getVeterinaria());
        veterinarioRepositorio.save(nuevoVet);

        // 2. Cambiar el Rol del Usuario a VETERINARIO (ID 3)
        Usuario usuario = solicitud.getUsuario();
        Rol rolVet = rolRepo.findById(3L).orElseThrow();
        usuario.setRol(rolVet);
        usuarioRepo.save(usuario);

        // 3. Actualizar solicitud
        solicitud.setEstado("APROBADA");
        solicitudRepo.save(solicitud);

        // --- 4. ENVIAR CORREO DE NOTIFICACIÓN (NUEVO) ---
        // Lo hacemos al final para asegurar que todo lo anterior se guardó bien
        if (solicitud.getUsuario().getEmail() != null) {
            emailService.enviarCorreoAprobacion(
                    solicitud.getUsuario().getEmail(),
                    solicitud.getUsuario().getNombre()
            );
        // -----------------------------------------------
    }
    }

    @Override
    public void rechazarSolicitud(Long idSolicitud) {
        SolicitudVeterinario solicitud = solicitudRepo.findById(idSolicitud).orElseThrow();
        solicitud.setEstado("RECHAZADA");
        solicitudRepo.save(solicitud);
    }
}
