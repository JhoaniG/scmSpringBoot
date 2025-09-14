package com.scm.scm.dto;

import com.scm.scm.model.Mascota;
import com.scm.scm.model.Veterinario;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ActividadFisicaDTO {

    private  Long idActividadFisica;

    private String descripcion;

    private  String tipoActividad;

    private String foto;

    private Long mascotaId;

    private Long veterinarioId;



    private MultipartFile archivoFoto;
    // --- Campos extras para la vista ---
    private String nombreMascota;
    private String nombreVeterinario;
}
