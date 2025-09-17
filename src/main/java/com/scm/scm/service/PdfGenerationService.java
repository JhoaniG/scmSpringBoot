
package com.scm.scm.service;

import java.util.Map;
//Importaten crear el servicio apratodo el usuo de pdf
public interface PdfGenerationService {
    byte[] generarPdfDesdeHtml(String templateHtml, Map<String, Object> datos);
}