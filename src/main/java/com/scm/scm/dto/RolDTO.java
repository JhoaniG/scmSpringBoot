package com.scm.scm.dto;

import jakarta.persistence.Column;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RolDTO {

    private Long idRol;
    private String rol;
}
