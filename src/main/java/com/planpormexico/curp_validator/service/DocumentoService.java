package com.planpormexico.curp_validator.service;

import com.planpormexico.curp_validator.dto.DocumentoRechazadoDTO;
import com.planpormexico.curp_validator.dto.ValidacionResultDTO;
import com.planpormexico.curp_validator.exception.InvalidDocumentException;
import com.planpormexico.curp_validator.model.Ciudadano;
import com.planpormexico.curp_validator.model.DocumentoRechazado;
import com.planpormexico.curp_validator.repository.DocumentoRechazadoRepository;
import com.planpormexico.curp_validator.util.CurpValidator;
import com.planpormexico.curp_validator.util.DocumentoCurpAnalyzer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentoService {

    private final CiudadanoService ciudadanoService;
    private final ExtraccionTextoService extraccionTextoService;
    private final DocumentoRechazadoRepository documentoRechazadoRepository;

    @Value("${app.evidencias.path}")
    private String evidenciasPath;

    private Path evidenciasDir;

    public DocumentoService(
            CiudadanoService ciudadanoService,
            ExtraccionTextoService extraccionTextoService,
            DocumentoRechazadoRepository documentoRechazadoRepository) {
        this.ciudadanoService = ciudadanoService;
        this.extraccionTextoService = extraccionTextoService;
        this.documentoRechazadoRepository = documentoRechazadoRepository;
    }

    @PostConstruct
    void inicializarDirectorioEvidencias() throws IOException {
        Path ruta = Paths.get(evidenciasPath);
        if (!ruta.isAbsolute()) {
            ruta = Paths.get(System.getProperty("user.dir")).resolve(ruta).normalize();
        }
        Files.createDirectories(ruta);
        this.evidenciasDir = ruta;
    }

    public ValidacionResultDTO validarDocumento(Long ciudadanoId, MultipartFile archivo) {
        validarArchivoPdf(archivo);

        Ciudadano ciudadano = ciudadanoService.obtenerEntidad(ciudadanoId);

        try {
            byte[] contenido = archivo.getBytes();
            String texto = extraccionTextoService.extraerTextoCompleto(contenido);

            if (!DocumentoCurpAnalyzer.esDocumentoCurp(texto)) {
                return registrarRechazo(
                        ciudadano,
                        contenido,
                        archivo.getOriginalFilename(),
                        CurpValidator.extraerCurp(texto).orElse(null),
                        "El documento no corresponde a una constancia oficial de CURP");
            }

            Optional<String> curpDetectada = CurpValidator.extraerCurp(texto);
            if (curpDetectada.isEmpty()) {
                return registrarRechazo(
                        ciudadano,
                        contenido,
                        archivo.getOriginalFilename(),
                        null,
                        "No se pudo detectar una CURP válida en el documento");
            }

            String curpEnDocumento = curpDetectada.get();
            if (!curpEnDocumento.equals(ciudadano.getCurp())) {
                return registrarRechazo(
                        ciudadano,
                        contenido,
                        archivo.getOriginalFilename(),
                        curpEnDocumento,
                        "La CURP del documento no coincide con la registrada");
            }

            if (!DocumentoCurpAnalyzer.coincidenDatosPersonales(ciudadano, texto)) {
                return registrarRechazo(
                        ciudadano,
                        contenido,
                        archivo.getOriginalFilename(),
                        curpEnDocumento,
                        "Los datos personales del documento no coinciden con el ciudadano registrado");
            }

            ciudadanoService.marcarDocumentoValidado(ciudadano);
            return new ValidacionResultDTO(
                    true,
                    "Documento validado correctamente. CURP y datos personales coinciden.",
                    curpEnDocumento);

        } catch (IOException e) {
            throw new InvalidDocumentException("No se pudo leer el archivo PDF", e);
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentoRechazadoDTO> listarRechazados(Long ciudadanoId) {
        ciudadanoService.obtenerEntidad(ciudadanoId);
        return documentoRechazadoRepository.findByCiudadanoId(ciudadanoId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ValidacionResultDTO registrarRechazo(
            Ciudadano ciudadano,
            byte[] contenido,
            String nombreOriginal,
            String curpDetectada,
            String motivo) throws IOException {

        String nombreArchivo = generarNombreEvidencia(ciudadano.getCurp(), nombreOriginal);
        Path destino = evidenciasDir.resolve(nombreArchivo);
        Files.write(destino, contenido);

        DocumentoRechazado rechazado = new DocumentoRechazado();
        rechazado.setCiudadano(ciudadano);
        rechazado.setRutaDocumento(destino.toString());
        rechazado.setMotivoRechazo(motivo);
        rechazado.setCurpDetectada(curpDetectada);
        documentoRechazadoRepository.save(rechazado);

        return new ValidacionResultDTO(false, motivo, curpDetectada);
    }

    private void validarArchivoPdf(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new InvalidDocumentException("Debe proporcionar un archivo PDF");
        }
        String nombre = archivo.getOriginalFilename();
        if (nombre == null || !nombre.toLowerCase().endsWith(".pdf")) {
            throw new InvalidDocumentException("Solo se permiten archivos PDF");
        }
        try {
            byte[] contenido = archivo.getBytes();
            if (!esPdfValido(contenido)) {
                throw new InvalidDocumentException("El archivo no es un PDF válido");
            }
        } catch (IOException e) {
            throw new InvalidDocumentException("No se pudo leer el archivo PDF", e);
        }
    }

    private boolean esPdfValido(byte[] contenido) {
        return contenido.length >= 4
                && contenido[0] == '%'
                && contenido[1] == 'P'
                && contenido[2] == 'D'
                && contenido[3] == 'F';
    }

    private String generarNombreEvidencia(String curp, String nombreOriginal) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String base = nombreOriginal != null ? nombreOriginal.replaceAll("[^a-zA-Z0-9._-]", "_") : "documento.pdf";
        return curp + "_" + timestamp + "_" + base;
    }

    private DocumentoRechazadoDTO toDto(DocumentoRechazado documento) {
        DocumentoRechazadoDTO dto = new DocumentoRechazadoDTO();
        dto.setId(documento.getId());
        dto.setRutaDocumento(documento.getRutaDocumento());
        dto.setMotivoRechazo(documento.getMotivoRechazo());
        dto.setCurpDetectada(documento.getCurpDetectada());
        dto.setFechaRechazo(documento.getFechaRechazo());
        return dto;
    }
}
