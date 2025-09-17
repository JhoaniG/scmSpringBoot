
package com.scm.scm.impl;

import com.lowagie.text.pdf.BaseFont;
import com.scm.scm.service.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfGenerationServiceImpl implements PdfGenerationService {

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public byte[] generarPdfDesdeHtml(String templateHtml, Map<String, Object> datos) {
        // 1. Thymeleaf procesa el HTML y lo llena con los datos
        Context context = new Context();
        context.setVariables(datos);
        String htmlProcesado = templateEngine.process(templateHtml, context);

        try {
            // 2. Flying Saucer convierte el HTML resultante en un PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlProcesado);
            renderer.layout();
            renderer.createPDF(outputStream, false);
            renderer.finishPDF();

            return outputStream.toByteArray();

        } catch (Exception e) {
            // Manejar la excepción apropiadamente (p.ej. loggear el error)
            e.printStackTrace();
            return null;
        }
    }
}