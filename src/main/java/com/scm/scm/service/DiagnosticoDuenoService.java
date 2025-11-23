package com.scm.scm.service;

import com.scm.scm.dto.DiagnosticoDuenoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiagnosticoDuenoService {
    DiagnosticoDuenoDTO crearDiagnosticoDueno(DiagnosticoDuenoDTO diagnosticoDuenoDTO);
    DiagnosticoDuenoDTO obtenerDiagnosticoDuenoPorId(Long id);
    DiagnosticoDuenoDTO actualizarDiagnosticoDueno(Long id, DiagnosticoDuenoDTO diagnosticoDuenoDTO);
    void eliminarDiagnosticoDueno(Long id);
    List<DiagnosticoDuenoDTO>ListartodosDiagnosticosDueno();
    Page<DiagnosticoDuenoDTO> listarDiagnosticosPorVeterinario(Long veterinarioId, String filtro, Pageable pageable);

    List<DiagnosticoDuenoDTO> listarDiagnosticosPorMascota(Long mascotaId);

    Page<DiagnosticoDuenoDTO> getAllDiagnosticosPaginados(Pageable pageable);


}
