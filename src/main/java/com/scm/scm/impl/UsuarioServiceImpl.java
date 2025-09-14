package com.scm.scm.impl;

import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.exceptions.CustomExeception;
import com.scm.scm.model.Rol;
import com.scm.scm.model.Usuario;
import com.scm.scm.model.Veterinario;
import com.scm.scm.repository.RolRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import com.scm.scm.repository.VeterinarioRepositorio;
import com.scm.scm.service.EmailService;
import com.scm.scm.service.UsuarioService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;


@Service
public class UsuarioServiceImpl implements UsuarioService {

private  final UsuarioRepositorio usuarioRepositorio;
private  final RolRepositorio rolRepositorio;
private final PasswordEncoder passwordEncoder;
private final VeterinarioRepositorio veterinarioRepositorio;

    @Autowired
    private EmailService emailService;
    private  final ModelMapper modelMapper;
    public UsuarioServiceImpl(UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio, PasswordEncoder passwordEncoder, VeterinarioRepositorio veterinarioRepositorio, ModelMapper modelMapper) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.veterinarioRepositorio = veterinarioRepositorio;
        this.modelMapper = modelMapper;
    }

    public UsuarioDTO findByEmail(String email) {
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new CustomExeception("Usuario no encontrado"));
        return modelMapper.map(usuario, UsuarioDTO.class);
    }


    @Override
    @Transactional
    public UsuarioDTO createUser(UsuarioDTO usuarioDTO) {
        // Verificar si ya existe el email
        if (usuarioRepositorio.findByEmail(usuarioDTO.getEmail()).isPresent()) {
            throw new CustomExeception("Ya existe un usuario con ese email");
        }

        Usuario usuario = modelMapper.map(usuarioDTO, Usuario.class);

        // Encriptar contraseña
        if (usuarioDTO.getContrasena() != null && !usuarioDTO.getContrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(usuarioDTO.getContrasena()));
        } else {
            throw new CustomExeception("La contraseña no puede estar vacía");
        }

        // Procesar foto
        MultipartFile archivo = usuarioDTO.getArchivoFoto();
        if (archivo != null && !archivo.isEmpty()) {
            try {
                // Ruta absoluta dentro del proyecto
                Path rutaUploads = Paths.get(System.getProperty("user.dir"), "uploads");
                Files.createDirectories(rutaUploads); // crea carpeta si no existe

                // Nombre único para la foto
                String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();

                // Guardar archivo en disco
                Path rutaArchivo = rutaUploads.resolve(nombreArchivo);
                archivo.transferTo(rutaArchivo.toFile());

                // Guardar el nombre de la foto en la entidad
                usuario.setFoto(nombreArchivo);
            } catch (IOException e) {
                throw new CustomExeception("Error al guardar la foto: " + e.getMessage());
            }
        }

        // Asignar rol
        Rol rol = rolRepositorio.findById(usuarioDTO.getRolId())
                .orElseThrow(() -> new CustomExeception("Rol no encontrado con ID: " + usuarioDTO.getRolId()));
        usuario.setRol(rol);

        // Guardar usuario en DB
        usuario = usuarioRepositorio.save(usuario);

        // Si es veterinario → crear registro en veterinarios
        if ("Veterinario".equalsIgnoreCase(rol.getRol()) && usuarioDTO.getEspecialidad() != null && !usuarioDTO.getEspecialidad().isBlank()) {
            // La lógica de adentro no cambia
            if (usuarioDTO.getVeterinaria() == null || usuarioDTO.getVeterinaria().isBlank()) {
                throw new CustomExeception("Debe especificar la clínica para el veterinario");
            }
            Veterinario veterinario = new Veterinario();
            veterinario.setEspecialidad(usuarioDTO.getEspecialidad());
            veterinario.setVeterinaria(usuarioDTO.getVeterinaria());
            veterinario.setUsuario(usuario);
            veterinarioRepositorio.save(veterinario);
        }


        // Preparar respuesta DTO
        UsuarioDTO response = modelMapper.map(usuario, UsuarioDTO.class);
        response.setContrasena(null); // No enviar contraseña
        return response;
    }





    @Override
    public UsuarioDTO updateUser(Long id, UsuarioDTO usuarioDTO) {
        if (usuarioRepositorio.existsById(id)){
            Usuario usuario=usuarioRepositorio.findById(id)
                    .orElseThrow(() -> new CustomExeception("No existe un usuario con el ID: " + id));
            usuario.setNombre(usuarioDTO.getNombre());
            usuario.setEmail(usuarioDTO.getEmail());
            usuario.setContrasena(usuarioDTO.getContrasena());
            if (usuarioDTO.getRolId() != null){
                Rol rol = rolRepositorio.findById(usuarioDTO.getRolId())
                        .orElseThrow(() -> new CustomExeception("Rol no encontrado con ID: " + usuarioDTO.getRolId()));
                usuario.setRol(rol);
            }
            usuario=usuarioRepositorio.save(usuario);
            return modelMapper.map(usuario, UsuarioDTO.class);


        }else {
            throw new CustomExeception("No existe un usuario con el ID: " + id);
        }



    }

    @Override
    public UsuarioDTO getUserById(Long id) {

        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new CustomExeception("No existe un usuario con el ID: " + id));


        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    @Override
    public void deleteUser(Long id) {
        // 1. PRIMERA VALIDACIÓN: Verificar que el usuario realmente existe.
        //    Esta es tu lógica actual, ¡y está perfecta!
        if (!usuarioRepositorio.existsById(id)) {
            throw new CustomExeception("No existe un usuario con el ID: " + id);
        }

        // 2. SEGUNDA VALIDACIÓN: Intentar eliminar y manejar errores de dependencia.
        //    Si el código llega aquí, es porque el usuario SÍ existe.
        try {
            usuarioRepositorio.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // Si la eliminación falla, es por una clave foránea (mascotas, citas, etc.).
            // Lanzamos el error con el mensaje amigable.
            throw new RuntimeException("No se puede eliminar este usuario porque tiene registros asociados (mascotas, citas, etc.).");
        }
    }

    @Override
    public UsuarioDTO authenticateUser(String email, String password) {
        Usuario usuario = usuarioRepositorio.findByEmailAndContrasena(email, password)
                .orElseThrow(() -> new CustomExeception("Credenciales inválidas"));

        return modelMapper.map(usuario, UsuarioDTO.class);

    }


    @Override
    public List<UsuarioDTO> findAllUsers() {
        List<Usuario> usuarios=usuarioRepositorio.findAll();
        return usuarios.stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class))
                .toList();
    }

    @Override
    public List<UsuarioDTO> obtenerDuenosDeMascota() {
        // Buscar el rol por su nombre para hacerlo más flexible
        Optional<Rol> rolDuenoOpt = rolRepositorio.findByRol("duenoMascota");

        if (rolDuenoOpt.isEmpty()) {
            throw new CustomExeception("El rol 'duenoMascota' no fue encontrado en la base de datos.");
        }

        // Obtener el objeto Rol del Optional y pasarlo al repositorio
        Rol rolDueno = rolDuenoOpt.get();
        List<Usuario> duenos = usuarioRepositorio.findByRol(rolDueno);

        // Mapear la lista de entidades Usuario a una lista de DTOs
        return duenos.stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class))
                .collect(Collectors.toList());
    }
    @Override
    public Page<UsuarioDTO> findAllUsersPaginated(String nombre, Long rolId, Pageable pageable) {
        Page<Usuario> paginaUsuarios = usuarioRepositorio.findByNombreAndRol(nombre, rolId, pageable);
        return paginaUsuarios.map(usuario -> modelMapper.map(usuario, UsuarioDTO.class));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void crearUsuarioIndividual(UsuarioDTO usuarioDTO) {
        // Este método reintenta la lógica de createUser, pero ahora en su propia transacción.
        // Si falla, solo esta transacción se deshace, no la carga completa.
        createUser(usuarioDTO);
    }

    @Override
    public void cargarUsuariosDesdeExcel(MultipartFile archivo) throws IOException {
        List<String> exitosos = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        InputStream inputStream = archivo.getInputStream();
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next(); // Saltamos la cabecera

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            UsuarioDTO usuarioDTO = new UsuarioDTO();
            String nombreCompleto = "";
            try {
                // Leemos los datos de cada celda
                String nombre = row.getCell(0).getStringCellValue();
                String apellido = row.getCell(1).getStringCellValue();
                nombreCompleto = nombre + " " + apellido;
                String email = row.getCell(2).getStringCellValue();
                Long rolId = (long) row.getCell(3).getNumericCellValue();

                usuarioDTO.setNombre(nombre);
                usuarioDTO.setApellido(apellido);
                usuarioDTO.setEmail(email);
                usuarioDTO.setContrasena("default123");
                usuarioDTO.setRolId(rolId);

                // Llamamos al nuevo método que manejará su propia transacción
                crearUsuarioIndividual(usuarioDTO);
                exitosos.add("Usuario '" + nombreCompleto + "' creado.");

            } catch (Exception e) {
                int numeroFila = row.getRowNum() + 1;
                errores.add("Error en la fila " + numeroFila + " ("+nombreCompleto+"): " + e.getMessage());
            }
        }
        workbook.close();
// --- Preparamos y enviamos el correo de resumen ---
        StringBuilder resumen = new StringBuilder();
        resumen.append("Proceso de carga masiva de usuarios finalizado.\n\n");
        resumen.append("Total de usuarios procesados: ").append(exitosos.size() + errores.size()).append("\n");
        resumen.append("Usuarios creados exitosamente: ").append(exitosos.size()).append("\n");
        resumen.append("Errores encontrados: ").append(errores.size()).append("\n\n");

        if (!errores.isEmpty()) {
            resumen.append("--- DETALLE DE ERRORES ---\n");
            for (String error : errores) {
                resumen.append(error).append("\n");
            }
        }

        // Cambia "admin@tuapp.com" por el correo del administrador que debe recibir el reporte
        emailService.enviarMensajeSimple("thomaspp0105@gmail.com", "Reporte de Carga Masiva de Usuarios", resumen.toString());        // El resto del código para enviar el correo sigue igual...
        // ...
    }



}
