package com.planpormexico.curp_validator.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfService {

    private static final int OCR_DPI = 300;
    private static final int MIN_TEXTO_UTIL = 10;

    public String extraerTexto(byte[] pdfBytes) throws IOException {
        try (PDDocument documento = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(documento);
        }
    }

    public List<BufferedImage> renderizarPaginas(byte[] pdfBytes) throws IOException {
        try (PDDocument documento = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(documento);
            List<BufferedImage> paginas = new ArrayList<>();
            for (int i = 0; i < documento.getNumberOfPages(); i++) {
                paginas.add(renderer.renderImageWithDPI(i, OCR_DPI));
            }
            return paginas;
        }
    }

    public boolean tieneTextoUtil(String texto) {
        return texto != null && texto.replaceAll("\\s+", "").length() >= MIN_TEXTO_UTIL;
    }
}
