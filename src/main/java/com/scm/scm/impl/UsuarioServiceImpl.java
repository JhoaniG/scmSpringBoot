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
import org.apache.poi.ss.usermodel.DataFormatter;
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
import java.time.LocalDate;
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


    // --- MÉTODO PARA EL FORMULARIO DE REGISTRO (RESTAURADO Y LIMPIO) ---
    @Override
    @Transactional
    public UsuarioDTO createUser(UsuarioDTO usuarioDTO) {
        if (usuarioRepositorio.findByEmail(usuarioDTO.getEmail()).isPresent()) {
            throw new CustomExeception("Ya existe un usuario con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setTelefono(usuarioDTO.getTelefono());
        usuario.setDireccion(usuarioDTO.getDireccion());
        usuario.setFechaNacimiento(usuarioDTO.getFechaNacimiento());
        usuario.setContrasena(passwordEncoder.encode(usuarioDTO.getContrasena()));

        Rol rol = rolRepositorio.findById(usuarioDTO.getRolId())
                .orElseThrow(() -> new CustomExeception("Rol no encontrado con ID: " + usuarioDTO.getRolId()));
        usuario.setRol(rol);

        // Procesar foto si existe
        MultipartFile archivo = usuarioDTO.getArchivoFoto();
        if (archivo != null && !archivo.isEmpty()) {
            try {
                Path rutaUploads = Paths.get(System.getProperty("user.dir"), "uploads");
                Files.createDirectories(rutaUploads);
                String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
                Path rutaArchivo = rutaUploads.resolve(nombreArchivo);
                archivo.transferTo(rutaArchivo.toFile());
                usuario.setFoto(nombreArchivo);
            } catch (IOException e) {
                throw new CustomExeception("Error al guardar la foto: " + e.getMessage());
            }
        }

        usuario = usuarioRepositorio.save(usuario);

        // Si es veterinario, crear el perfil de veterinario
        if ("Veterinario".equalsIgnoreCase(rol.getRol())) {
            if (usuarioDTO.getEspecialidad() == null || usuarioDTO.getEspecialidad().isBlank() || usuarioDTO.getVeterinaria() == null || usuarioDTO.getVeterinaria().isBlank()) {
                throw new CustomExeception("Debe especificar especialidad y clínica para el veterinario");
            }
            Veterinario veterinario = new Veterinario();
            veterinario.setEspecialidad(usuarioDTO.getEspecialidad());
            veterinario.setVeterinaria(usuarioDTO.getVeterinaria());
            veterinario.setUsuario(usuario);
            veterinarioRepositorio.save(veterinario);
        }

        return modelMapper.map(usuario, UsuarioDTO.class);
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
    // --- RESTO DE MÉTODOS (sin cambios) ---
    @Override
    public Page<UsuarioDTO> findAllUsersPaginated(String nombre, Long rolId, Pageable pageable) {
        Page<Usuario> paginaUsuarios = usuarioRepositorio.findByNombreAndRol(nombre, rolId, pageable);
        return paginaUsuarios.map(usuario -> modelMapper.map(usuario, UsuarioDTO.class));
    }

    // Este método sí debe ser público y estar en la interfaz
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void crearUsuarioIndividual(UsuarioDTO usuarioDTO) {
        crearUsuarioDesdeCargaMasiva(usuarioDTO);
    }
    @Transactional // <-- 2. Añade esta anotación
    private void crearUsuarioDesdeCargaMasiva(UsuarioDTO usuarioDTO) { // <-- 1. Cámbialo a private
        if (usuarioRepositorio.findByEmail(usuarioDTO.getEmail()).isPresent()) {
            throw new CustomExeception("El email '" + usuarioDTO.getEmail() + "' ya existe.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setContrasena(passwordEncoder.encode(usuarioDTO.getContrasena()));
        usuario.setTelefono(usuarioDTO.getTelefono());
        usuario.setDireccion(usuarioDTO.getDireccion());
        usuario.setFechaNacimiento(usuarioDTO.getFechaNacimiento());

        Rol rol = rolRepositorio.findById(usuarioDTO.getRolId())
                .orElseThrow(() -> new CustomExeception("Rol no encontrado con ID: " + usuarioDTO.getRolId()));
        usuario.setRol(rol);

        usuarioRepositorio.save(usuario);
    }
    @Override
    public void cargarUsuariosDesdeExcel(MultipartFile archivo) throws IOException {
        List<String> exitosos = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        // Esta herramienta nos ayudará a leer cualquier celda como texto
        DataFormatter dataFormatter = new DataFormatter();

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
                // Usamos dataFormatter.formatCellValue para leer todo como texto de forma segura
                String nombre = dataFormatter.formatCellValue(row.getCell(0));
                String apellido = dataFormatter.formatCellValue(row.getCell(1));
                nombreCompleto = nombre + " " + apellido;
                String email = dataFormatter.formatCellValue(row.getCell(2));
                String telefono = dataFormatter.formatCellValue(row.getCell(3));
                String direccion = dataFormatter.formatCellValue(row.getCell(4));
                // Leemos la fecha como texto y la convertimos
                LocalDate fechaNacimiento = LocalDate.parse(dataFormatter.formatCellValue(row.getCell(5)));
                // Leemos el Rol ID como texto y lo convertimos a número
                Long rolId = Long.parseLong(dataFormatter.formatCellValue(row.getCell(6)));

                // Verificamos que los campos obligatorios no estén vacíos después de leer
                if (nombre.isBlank() || apellido.isBlank() || email.isBlank()) {
                    throw new Exception("Nombre, Apellido y Email no pueden estar vacíos.");
                }

                usuarioDTO.setNombre(nombre);
                usuarioDTO.setApellido(apellido);
                usuarioDTO.setEmail(email);
                usuarioDTO.setTelefono(telefono);
                usuarioDTO.setDireccion(direccion);
                usuarioDTO.setFechaNacimiento(fechaNacimiento);
                usuarioDTO.setContrasena("default123");
                usuarioDTO.setRolId(rolId);

                crearUsuarioIndividual(usuarioDTO);
                exitosos.add("Usuario '" + nombreCompleto + "' creado.");

            } catch (Exception e) {
                int numeroFila = row.getRowNum() + 1;
                String causaError = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                errores.add("Error en la fila " + numeroFila + ": " + causaError);
            }
        }
        workbook.close();

        // El código para enviar el correo de resumen no cambia...
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
        emailService.enviarMensajeSimple("crackpito12@gmail.com", "Reporte de Carga Masiva de Usuarios", resumen.toString());
    }



}
