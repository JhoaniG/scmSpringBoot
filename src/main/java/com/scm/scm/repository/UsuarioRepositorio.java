package com.scm.scm.repository;

import com.scm.scm.model.Rol;
import com.scm.scm.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailAndContrasena(String email, String contrasena);
    List<Usuario> findByRolIdRol(Long idRol);
    List<Usuario> findByRol(Rol rol);
    @Query("SELECT u FROM Usuario u WHERE u.rol.idRol = 3 AND u.idUsuario NOT IN (SELECT v.usuario.idUsuario FROM Veterinario v)")
    List<Usuario> findUsuariosVeterinariosDisponibles();



    List<Usuario> findByRol_IdRol(Integer idRol);
    // --- NUEVO MÉTODO PARA BÚSQUEDA CON FILTROS ---
    @Query("SELECT u FROM Usuario u WHERE " +
            "(:nombre IS NULL OR u.nombre LIKE %:nombre%) AND " +
            "(:rolId IS NULL OR u.rol.idRol = :rolId)")
    Page<Usuario> findByNombreAndRol(@Param("nombre") String nombre,
                                     @Param("rolId") Long rolId,
                                     Pageable pageable);
}
