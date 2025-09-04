package com.scm.scm.service;

import com.scm.scm.dto.DiagnosticoDuenoDTO;

import java.util.List;

public interface DiagnosticoDuenoService {
    DiagnosticoDuenoDTO crearDiagnosticoDueno(DiagnosticoDuenoDTO diagnosticoDuenoDTO);
    DiagnosticoDuenoDTO obtenerDiagnosticoDuenoPorId(Long id);
    DiagnosticoDuenoDTO actualizarDiagnosticoDueno(Long id, DiagnosticoDuenoDTO diagnosticoDuenoDTO);
    void eliminarDiagnosticoDueno(Long id);
    List<DiagnosticoDuenoDTO>ListartodosDiagnosticosDueno();
}
