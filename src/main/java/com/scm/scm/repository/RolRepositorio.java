package com.scm.scm.repository;

import com.scm.scm.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepositorio extends JpaRepository<Rol, Long> {

    Optional<Rol> findByRol(String rol);

}
