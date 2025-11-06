package com.scm.scm.service;

import com.scm.scm.dto.UsuarioDTO;
import com.scm.scm.model.PasswordResetToken;
import com.scm.scm.model.Usuario;
import com.scm.scm.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UsuarioService
{
    UsuarioDTO createUser(UsuarioDTO usuarioDTO);
    UsuarioDTO updateUser(Long id, UsuarioDTO usuarioDTO);
    UsuarioDTO getUserById(Long id);
    void deleteUser(Long id);
    UsuarioDTO authenticateUser(String email, String password);
    List<UsuarioDTO> findAllUsers();
    List<UsuarioDTO> obtenerDuenosDeMascota();

    Page<UsuarioDTO> findAllUsersPaginated(String nombre, Long rolId, Pageable pageable);

    void cargarUsuariosDesdeExcel(MultipartFile archivo) throws IOException;

    // Nueva firma de método
    void crearUsuarioIndividual(UsuarioDTO usuarioDTO);
    Optional<Usuario> finUserByEmail(String email);
    void CreatePasswordResetTokenForUser(Usuario usuario, String token);
    PasswordResetToken getPasswordResetToken(String token);
    void ChangeUserPassword(Usuario usuario, String newPassword);
    List<UsuarioDTO> obtenerDuenosConCitasTerminadas(Long veterinarioId);



}
