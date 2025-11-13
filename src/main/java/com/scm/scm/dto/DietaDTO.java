package com.scm.scm.dto;

import com.scm.scm.model.Mascota;
import com.scm.scm.model.Veterinario;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DietaDTO {

    private Long idDieta;

    // --- AÑADIR VALIDACIÓN ---
    @NotBlank(message = "La descripción es obligatoria.")
    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres.")
    private String descripcion;

    @NotBlank(message = "El tipo de dieta es obligatorio.")
    private String tipoDieta;
    // --- FIN DE VALIDACIÓN ---

    private String foto;

    // --- AÑADIR VALIDACIÓN ---
    @NotNull(message = "Debe seleccionar una mascota.")
    private Long mascotaId;

    @NotNull(message = "El veterinario es obligatorio.")
    private Long veterinarioId;
    // --- FIN DE VALIDACIÓN ---

    private MultipartFile archivoFoto;
    private String nombreMascota;
    private String nombreVeterinario;

    // --- AÑADIR ESTOS DOS CAMPOS CON VALIDACIÓN ---
    @NotNull(message = "La fecha de inicio es obligatoria.")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria.")
    private LocalDate fechaFin;
    // --- FIN DE LA ADICIÓN ---
}
