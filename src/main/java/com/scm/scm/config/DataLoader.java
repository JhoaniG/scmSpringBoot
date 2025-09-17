package com.scm.scm.config;

import com.scm.scm.model.Rol;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.RolRepositorio;
import com.scm.scm.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RolRepositorio rolRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // --- Creación de Roles ---
        if (rolRepositorio.count() == 0) {
            System.out.println("No existen roles, creando roles iniciales...");
            // Usando los nombres exactos de tu base de datos
            rolRepositorio.save(new Rol(1L, "Admin"));
            rolRepositorio.save(new Rol(2L, "duenoMascota"));
            rolRepositorio.save(new Rol(3L, "Veterinario"));
            System.out.println("Roles creados.");
        }

        // --- Creación del Usuario Administrador ---
        if (!usuarioRepositorio.findByEmail("jhoani@gmail.com").isPresent()) {
            System.out.println("Usuario administrador no encontrado, creando admin...");

            // Buscamos el rol "Admin"
            Rol adminRol = rolRepositorio.findByRol("Admin")
                    .orElseThrow(() -> new RuntimeException("Error: Rol 'Admin' no encontrado."));

            Usuario admin = new Usuario();
            admin.setNombre("jhoani");
            admin.setApellido("roscon");
            admin.setEmail("jhoani@gmail.com");
            admin.setContrasena(passwordEncoder.encode("roscon1234/"));
            admin.setTelefono("3001234567");
            admin.setDireccion("Dirección de Administrador");
            admin.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            admin.setRol(adminRol);

            usuarioRepositorio.save(admin);
            System.out.println("Usuario administrador creado exitosamente.");
        }
    }
}