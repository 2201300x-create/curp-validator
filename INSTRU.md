# CURP Validator — Instrucciones

Proyecto desarrollado para **Plan por México S.A. de C.V.**, cuyo objetivo es centralizar el registro de ciudadanos y validar que el documento PDF de CURP que suben corresponda realmente a la persona registrada, usando OCR.

> Para el árbol de carpetas, estado de cada módulo y división del equipo, ver [`estructura.txt`](estructura.txt).

---

## Estado actual del proyecto

| Módulo | Estado |
|---|---|
| Entidades JPA (`Ciudadano`, `DocumentoRechazado`) | Implementado |
| Repositorios Spring Data | Implementado |
| DTOs y validación regex de CURP | Implementado |
| `application.properties` | Implementado (requiere credenciales locales) |
| Controllers REST | Implementado |
| Services (OCR, PDF, validación) | Implementado |
| Utilidad `CurpValidator` | Implementado |
| Frontend (`registro.html`, `carga-curp.html`) | Implementado |
| Tests unitarios / integración | Implementado (perfil `test` con H2) |

Los tests se ejecutan con `./mvnw test` sin necesidad de PostgreSQL local.

---

## Tecnologías utilizadas

- **Java 21**
- **Spring Boot 3.5.x** (generado desde [start.spring.io](https://start.spring.io/))
- **PostgreSQL** como base de datos
- **Spring Data JPA / Hibernate**
- **Apache PDFBox 3.0.1** (detección y extracción de texto de PDFs)
- **Tesseract OCR / Tess4j 5.10.0** (reconocimiento óptico de caracteres)
- **Maven** (el proyecto incluye su propio wrapper `mvnw`, no es necesario instalar Maven)

---

## Requisitos previos

Antes de ejecutar el proyecto, cada integrante necesita tener instalado en su máquina:

1. **JDK 21** (no funciona con otras versiones; da error de compilación)
2. **PostgreSQL** corriendo localmente
3. **Tesseract OCR** (necesario cuando se implemente `OcrService`)

### Verificar Java 21

```bash
java -version
```

Debe mostrar `21.x.x`. Si tienen varias versiones de Java instaladas y no aparece la 21, hay que configurar `JAVA_HOME` para que apunte al JDK 21.

### Instalar Tesseract (Linux)

```bash
sudo apt update
sudo apt install tesseract-ocr tesseract-ocr-spa
```

En Windows, descargar el instalador desde [GitHub — tesseract-ocr/tesseract](https://github.com/tesseract-ocr/tesseract) e incluir el paquete de idioma español (`spa`).

---

## Configuración de PostgreSQL

Cada integrante debe crear su **propia base de datos local** llamada `curp_validator`. El nombre de la base de datos debe ser exactamente este, ya que así está configurado en `application.properties`.

### En Ubuntu / Linux

1. Instalar PostgreSQL (si no lo tienen):

```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl enable --now postgresql
```

2. Entrar a la consola de PostgreSQL como superusuario:

```bash
sudo -u postgres psql
```

3. Crear su usuario y la base de datos (cambien `mi_usuario` y `mi_password` por lo que ustedes quieran):

```sql
CREATE USER mi_usuario WITH PASSWORD 'mi_password';
CREATE DATABASE curp_validator OWNER mi_usuario;
GRANT ALL PRIVILEGES ON DATABASE curp_validator TO mi_usuario;
```

4. Salir con `\q`.

### En Windows

1. Descargar e instalar PostgreSQL desde [postgresql.org/download/windows](https://www.postgresql.org/download/windows/). El instalador incluye **pgAdmin**.

2. Durante la instalación les va a pedir una contraseña para el usuario `postgres` (el superusuario) — anótenla.

3. Abrir **pgAdmin** (o "SQL Shell (psql)" desde el menú de inicio) y conectarse con el usuario `postgres`.

4. Crear la base de datos y, si quieren, un usuario propio:

```sql
CREATE USER mi_usuario WITH PASSWORD 'mi_password';
CREATE DATABASE curp_validator OWNER mi_usuario;
GRANT ALL PRIVILEGES ON DATABASE curp_validator TO mi_usuario;
```

(También pueden usar directamente el usuario `postgres` y su contraseña, sin crear uno nuevo — funciona igual.)

---

## Configurar el proyecto

Abrir el archivo `src/main/resources/application.properties` y poner **su propio usuario y contraseña** de PostgreSQL:

```properties
spring.application.name=curp-validator

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/curp_validator
spring.datasource.username=mi_usuario
spring.datasource.password=mi_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Carpeta de evidencias
app.evidencias.path=src/main/resources/evidencias/

# Tamaño máximo de archivo para subir PDFs
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

> **Importante:** cada persona usa sus propias credenciales locales. No es necesario que coincidan entre integrantes, solo que el `spring.datasource.url` apunte a una base de datos llamada `curp_validator`.

---

## Cómo ejecutar el proyecto

Desde la raíz del proyecto (donde está `pom.xml`):

**Linux / macOS:**

```bash
./mvnw spring-boot:run
```

**Windows (CMD o PowerShell):**

```bash
mvnw.cmd spring-boot:run
```

La primera vez va a descargar dependencias; puede tardar un poco. Si todo está bien configurado, al final de la consola debe aparecer algo como:

```
Started CurpValidatorApplication in X seconds
```

Y antes de eso, Hibernate va a crear automáticamente las tablas `ciudadanos` y `documentos_rechazados` en la base de datos (no hace falta correr ningún script SQL manual).

La aplicación queda corriendo en `http://localhost:8080`. Para detenerla: `Ctrl + C`.

### Compilar sin correr tests

```bash
./mvnw compile -DskipTests
```

---

## Verificar que las tablas se crearon correctamente

Con la app corriendo (o después de haberla corrido al menos una vez), conéctense a su base de datos:

```bash
psql -U mi_usuario -d curp_validator -h localhost
```

Y dentro de `psql`:

```sql
\dt
```

Deberían ver las tablas `ciudadanos` y `documentos_rechazados`.

---

## Endpoints REST

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/ciudadanos` | Registrar un ciudadano |
| `GET` | `/api/ciudadanos/{id}` | Consultar ciudadano por ID |
| `GET` | `/api/ciudadanos/curp/{curp}` | Consultar ciudadano por CURP |
| `POST` | `/api/documentos/validar` | Subir PDF y validar (`ciudadanoId` + `archivo`) |
| `GET` | `/api/documentos/rechazados/{ciudadanoId}` | Listar documentos rechazados de un ciudadano |

---

## Flujo de validación de documento

1. El ciudadano ya debe existir en la base de datos con su CURP.
2. Se recibe un archivo PDF (`multipart/form-data`).
3. `PdfService` intenta extraer texto embebido con PDFBox.
4. Si no hay CURP en el texto embebido, se rasteriza el PDF y `OcrService` usa Tesseract.
5. Se verifica que el texto corresponda a una **constancia oficial de CURP** (indicadores RENAPO, CONSTANCIA, etc.).
6. Se extrae la CURP y se compara con la registrada.
7. Se verifica que **nombre y apellidos** aparezcan en el documento.
8. Resultado:
   - **Válido** → `documento_validado = true`.
   - **Inválido** → PDF guardado en `evidencias/`, registro en `documentos_rechazados`.

## Límite de registros

La fase de prueba permite un máximo de **10 ciudadanos** (`app.registro.limite=10` en `application.properties`).

---

## Estructura del proyecto

El proyecto sigue la arquitectura estándar de Spring Boot por capas:

```
src/main/java/com/planpormexico/curp_validator/
├── controller/   → Endpoints REST
├── service/      → Lógica de negocio (OCR, PDF, validación)
├── repository/   → Acceso a base de datos (Spring Data JPA)
├── model/        → Entidades (Ciudadano, DocumentoRechazado)
├── dto/          → Objetos de transferencia (CiudadanoDTO, ValidacionResultDTO)
└── util/         → Utilidades (validación de CURP)
```

---

## División del trabajo en equipo

| Integrante | Área | Responsabilidades |
|---|---|---|
| Dev 1 | Backend - Datos | Modelos, repositorios, conexión a BD, registro de ciudadanos |
| Dev 2 | Backend - Validación | OCR (Tesseract), lectura de PDF (PDFBox), comparación de CURP, repositorio de evidencias |
| Dev 3 | Frontend + Controller | Vistas, endpoints REST, integración general |

---

## Notas para desarrollo

- El paquete base es `com.planpormexico.curp_validator` (con guion bajo).
- Lombok está en el `pom.xml` pero las entidades actuales usan getters/setters manuales; pueden unificar criterio en el equipo.
- Los PDFs rechazados se almacenan en `src/main/resources/evidencias/` (ruta configurable con `app.evidencias.path`).
- Para tests sin PostgreSQL local, considerar un perfil `test` con H2 en memoria o Testcontainers.
