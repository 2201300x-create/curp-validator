package com.planpormexico.curp_validator.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class OcrService {

    private final ITesseract tesseract;

    public OcrService(@Value("${app.tesseract.datapath:}") String dataPath) {
        this.tesseract = new Tesseract();
        this.tesseract.setLanguage("spa");
        if (dataPath != null && !dataPath.isBlank()) {
            this.tesseract.setDatapath(dataPath);
        }
    }

    public String extraerTexto(BufferedImage imagen) throws TesseractException {
        return tesseract.doOCR(imagen);
    }
}
