# CURP Validator

Aplicación Spring Boot para **Plan por México S.A. de C.V.** que registra ciudadanos y valida documentos PDF de CURP mediante extracción de texto (PDFBox) y OCR (Tesseract).

## Inicio rápido

1. Configurar PostgreSQL y credenciales en `src/main/resources/application.properties`
2. Instalar Tesseract con idioma español (`tesseract-ocr-spa`)
3. Ejecutar:

```bash
./mvnw spring-boot:run
```

4. Abrir `http://localhost:8080`

## Documentación

- [`INSTRU.md`](INSTRU.md) — configuración detallada, PostgreSQL, ejecución
- [`estructura.txt`](estructura.txt) — árbol del proyecto y división del equipo

## API REST

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/ciudadanos` | Registrar ciudadano |
| `GET` | `/api/ciudadanos/{id}` | Consultar por ID |
| `GET` | `/api/ciudadanos/curp/{curp}` | Consultar por CURP |
| `POST` | `/api/documentos/validar` | Validar PDF (`ciudadanoId` + `archivo`) |
| `GET` | `/api/documentos/rechazados/{ciudadanoId}` | Listar rechazos |

## Interfaz web

- `/registro.html` — registro de ciudadanos
- `/carga-curp.html` — subida y validación de PDF

## Tests

```bash
./mvnw test
```

Los tests usan H2 en memoria (perfil `test`); no requieren PostgreSQL.
