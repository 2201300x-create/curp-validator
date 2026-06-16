package com.planpormexico.curp_validator.service;

import com.planpormexico.curp_validator.exception.InvalidDocumentException;
import com.planpormexico.curp_validator.util.CurpValidator;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@Service
public class ExtraccionTextoService {

    private final PdfService pdfService;
    private final OcrService ocrService;

    public ExtraccionTextoService(PdfService pdfService, OcrService ocrService) {
        this.pdfService = pdfService;
        this.ocrService = ocrService;
    }

    public String extraerTextoCompleto(byte[] contenido) throws IOException {
        String textoEmbebido = pdfService.extraerTexto(contenido);

        if (!requiereOcr(textoEmbebido)) {
            return textoEmbebido;
        }

        String textoOcr = aplicarOcr(contenido);
        if (pdfService.tieneTextoUtil(textoEmbebido)) {
            return textoEmbebido + "\n" + textoOcr;
        }
        return textoOcr;
    }

    private boolean requiereOcr(String textoEmbebido) {
        if (!pdfService.tieneTextoUtil(textoEmbebido)) {
            return true;
        }
        return CurpValidator.extraerCurp(textoEmbebido).isEmpty();
    }

    private String aplicarOcr(byte[] contenido) throws IOException {
        StringBuilder textoOcr = new StringBuilder();
        List<BufferedImage> paginas = pdfService.renderizarPaginas(contenido);

        for (BufferedImage pagina : paginas) {
            try {
                textoOcr.append(ocrService.extraerTexto(pagina)).append('\n');
            } catch (TesseractException e) {
                throw new InvalidDocumentException(
                        "Error al procesar el documento con OCR. Verifique que Tesseract esté instalado.", e);
            }
        }
        return textoOcr.toString();
    }
}
