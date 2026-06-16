# CURP Validator

Sistema web desarrollado para **Plan por México S.A. de C.V.** que permite registrar ciudadanos y validar que el documento PDF de CURP cargado corresponda realmente a la persona registrada, mediante OCR (Reconocimiento Óptico de Caracteres).

---

## Descripción

El sistema resuelve la problemática de ciudadanos que cargaban documentos incorrectos, de otra persona, o que no correspondían a una constancia oficial de CURP. Para ello implementa un proceso de validación en 4 etapas:

1. **Registro** — El ciudadano ingresa nombre, apellidos y CURP (validada con expresión regular).
2. **Carga de PDF** — El ciudadano sube su documento oficial de CURP.
3. **Extracción** — PDFBox extrae el texto si el PDF es seleccionable; si es escaneado, Tesseract OCR lo procesa.
4. **Validación** — Se verifica que el documento sea una constancia oficial (RENAPO/CONSTANCIA), que la CURP coincida con la registrada y que los datos personales correspondan al ciudadano.

Los documentos rechazados se almacenan en un repositorio de evidencias para futuras aclaraciones.

---

## Tecnologías

- **Java 21**
- **Spring Boot 3.5.x** (generado con [start.spring.io](https://start.spring.io/))
- **PostgreSQL** — base de datos relacional
- **Spring Data JPA / Hibernate** — mapeo objeto-relacional
- **Apache PDFBox 3.0.1** — extracción de texto de PDFs
- **Tesseract OCR (Tess4j 5.10.0)** — OCR para PDFs escaneados
- **Maven** (incluye wrapper `mvnw`, no requiere instalación)

---

## Requisitos previos

- **JDK 21** instalado y configurado como `JAVA_HOME`
- **PostgreSQL** corriendo localmente
- **Tesseract OCR** instalado en el sistema

### Instalar Tesseract (Ubuntu/Linux)

```bash
sudo apt update
sudo apt install tesseract-ocr tesseract-ocr-spa
```

### Verificar la ruta de tessdata

```bash
find /usr/share/tesseract-ocr -name "*.traineddata"
```

Anota la carpeta que contiene los archivos `.traineddata` — la necesitarás en `application.properties`.

---

## Configuración

### 1. Crear la base de datos en PostgreSQL

```bash
sudo -u postgres psql
```

```sql
CREATE USER tu_usuario WITH PASSWORD 'tu_password';
CREATE DATABASE curp_validator OWNER tu_usuario;
GRANT ALL PRIVILEGES ON DATABASE curp_validator TO tu_usuario;
\q
```

### 2. Configurar `application.properties`

Edita el archivo `src/main/resources/application.properties` con tus credenciales:

```properties
spring.application.name=curp-validator

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/curp_validator
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Tesseract OCR
app.tesseract.datapath=/usr/share/tesseract-ocr/5/tessdata
app.tesseract.language=spa

# Carpeta de evidencias
app.evidencias.path=src/main/resources/evidencias/

# Tamaño máximo de archivo PDF
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

> Ajusta `app.tesseract.datapath` a la ruta que encontraste en el paso anterior.

---

## Ejecución

### 1. Iniciar el backend

Desde la raíz del proyecto (donde está `pom.xml`):

**Linux / macOS:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```bash
mvnw.cmd spring-boot:run
```

La primera vez descargará dependencias. Al finalizar verás:

```
Started CurpValidatorApplication in X seconds
```

Hibernate creará automáticamente las tablas `ciudadanos` y `documentos_rechazados` en PostgreSQL. La API queda disponible en `http://localhost:8080`.

### 2. Iniciar el frontend

En una terminal separada, desde la carpeta `frontend/`:

```bash
cd frontend
python3 -m http.server 3000
```

Luego abre en el navegador:

| URL | Descripción |
|---|---|
| `http://localhost:3000/registro.html` | Registro de ciudadanos |
| `http://localhost:3000/carga-curp.html` | Carga y validación del PDF |

> **Importante:** el backend debe estar corriendo en `http://localhost:8080` antes de usar el frontend.

Para detener cualquiera de los dos servidores: `Ctrl + C`.

---

## Estructura del proyecto

```
src/main/java/com/planpormexico/curp_validator/
├── controller/        # Endpoints REST
├── service/           # Lógica de negocio, OCR y validación
├── repository/        # Acceso a PostgreSQL (Spring Data JPA)
├── model/             # Entidades JPA
├── dto/               # Objetos de transferencia de datos
├── exception/         # Manejo global de excepciones
└── util/              # Validación CURP y análisis de documento
```

---

## Modelo de datos

**`ciudadanos`**
Almacena los datos del ciudadano registrado con validación de formato CURP mediante expresión regular.

**`documentos_rechazados`**
Registra cada documento inválido con: ruta del archivo guardado, motivo del rechazo, CURP detectada (si la hubo) y fecha de rechazo.

---

## Repositorio de evidencias

Los PDFs rechazados se guardan en `src/main/resources/evidencias/` con el siguiente formato de nombre:

```
CURP_YYYYMMDD_HHmmss_nombre_original.pdf
```

Ejemplo:
```
GACJ990101HMNRRL09_20260613_142305_curp.pdf
```

---

## Equipo de desarrollo

| Área | Responsabilidades |
|---|---|
| Backend · Datos | Modelos, repositorios, DTOs, conexión a PostgreSQL |
| Backend · Validación | OCR, PDFBox, comparación CURP, evidencias |
| Frontend · Controller | Vistas HTML, endpoints REST, integración |

---

*Universidad Michoacana de San Nicolás de Hidalgo · Facultad de Ingeniería Eléctrica · 2026*