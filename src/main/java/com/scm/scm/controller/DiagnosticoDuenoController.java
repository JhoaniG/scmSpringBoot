package com.scm.scm.controller;

import com.scm.scm.dto.DiagnosticoDuenoDTO;
import com.scm.scm.dto.DietaDTO;
import com.scm.scm.service.DiagnosticoDuenoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping ("/api/diagnosticos-duenos")
public class DiagnosticoDuenoController {
    private  final DiagnosticoDuenoService diagnosticoDuenoService;

    public DiagnosticoDuenoController(DiagnosticoDuenoService diagnosticoDuenoService) {
        this.diagnosticoDuenoService = diagnosticoDuenoService;
    }

    @GetMapping ("/listar")
    public ResponseEntity<List<DiagnosticoDuenoDTO>>listartodos(){
        List<DiagnosticoDuenoDTO> diagnosticosDuenos = diagnosticoDuenoService.ListartodosDiagnosticosDueno();
        return ResponseEntity.ok(diagnosticosDuenos);
    }

    // Crear un nuevo diagnóstico de dueño
    @PostMapping("/crear")
    public ResponseEntity<DiagnosticoDuenoDTO> crearDiagnosticoDueno(@RequestBody  DiagnosticoDuenoDTO diagnosticoDuenoDTO) {
        DiagnosticoDuenoDTO createdDiagnosticoDueno = diagnosticoDuenoService.crearDiagnosticoDueno(diagnosticoDuenoDTO);
        return ResponseEntity.ok(createdDiagnosticoDueno);
    }
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<DiagnosticoDuenoDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody DiagnosticoDuenoDTO diagnosticoDuenoDTO) {

        DiagnosticoDuenoDTO updatedTransaction = diagnosticoDuenoService.actualizarDiagnosticoDueno(id, diagnosticoDuenoDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        diagnosticoDuenoService.eliminarDiagnosticoDueno(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
