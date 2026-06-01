Markdown# Sistema de Control Médico Veterinario (SCM) 🐾

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MariaDB](https://img.shields.io/badge/MariaDB-10.x-003545?style=for-the-badge&logo=mariadb&logoColor=white)](https://mariadb.org/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Deployed on Railway](https://img.shields.io/badge/Deployed%20on-Railway-131415?style=for-the-badge&logo=railway&logoColor=white)](https://railway.app/)

SCM es una plataforma web empresarial diseñada para digitalizar y optimizar la gestión operativa y médica de clínicas veterinarias. Permite el control total de usuarios (administradores, veterinarios y dueños), el historial clínico de las mascotas, agendamiento de citas, reportes automatizados y herramientas avanzadas como la carga masiva de datos y notificaciones por correo electrónico.

---

## 🚀 Características Principales

* **Autenticación y Seguridad Robusta:** Filtros basados en roles de usuario con Spring Security.
* **Gestión Integral de Usuarios:** Módulos CRUD personalizados con control de perfiles y carga de avatares en disco.
* **Módulo de Diagnósticos Médicos:** Registro detallado de observaciones clínicas, enlazado directamente a veterinarios y mascotas con paginación inteligente.
* **Carga Masiva de Datos:** Procesamiento en lote a través de archivos Excel (`.xlsx`) utilizando **Apache POI**, procesado mediante transacciones aisladas a nivel de fila.
* **Sistema de Notificaciones Automatizado:** Integración con servidores SMTP (Brevo/Gmail) para el envío automático de reportes de auditoría post-carga.
* **Búsquedas con Filtros Dinámicos:** Consultas JPQL optimizadas para filtrar registros concurrentemente por texto y relaciones de base de datos.

---

## 🏗️ Arquitectura del Sistema

El proyecto sigue una arquitectura **Monolítica en Capas** guiada por las mejores prácticas del ecosistema Spring, asegurando el desacoplamiento de responsabilidades y la consistencia de los datos.

📂 com.scm.scm├── 📂 config       # Configuraciones de Seguridad, Beans y Codificadores├── 📂 controller   # Controladores Web (MVC - Thymeleaf Mapping)├── 📂 dto          # Objetos de Transferencia de Datos (Data Transfer Objects)├── 📂 exceptions   # Manejo Global de Excepciones Personalizadas├── 📂 impl         # Implementación de la Lógica de Negocio (Services)├── 📂 model        # Entidades de Persistencia (JPA / Hibernate)├── 📂 repository   # Repositorios de Acceso a Datos (Spring Data JPA)└── 📂 service      # Interfaces de Servicios de Negocio
* **Capa de Presentación:** Plantillas HTML5 dinámicas motorizadas por **Thymeleaf**, estilizadas con **Bootstrap 5** y estructuradas mediante fragmentos reutilizables (`menuA`, `barralateralA`).
* **Capa de Control (Web MVC):** Controladores Spring que interceptan las peticiones, gestionan el flujo de datos y manejan atributos de redirección (`RedirectAttributes`).
* **Capa de Servicio (Negocio):** Centraliza las reglas de negocio. Implementa **ModelMapper** para aislar las Entidades de la base de datos de los DTOs expuestos.
* **Capa de Datos:** Abstracción completa del motor SQL mediante interfaces que extienden de `JpaRepository`, utilizando consultas derivadas y anotaciones `@Query` con JPQL.

---

## 🛠️ Stack Tecnológico

* **Backend:** Java 17, Spring Boot 3.x (Spring Security, Spring Data JPA, Spring Web Mail).
* **Frontend:** HTML5, CSS3, Thymeleaf, Bootstrap 5.
* **Procesamiento de Archivos:** Apache POI (`poi-ooxml`).
* **Mapeo de Objetos:** ModelMapper, Lombok.
* **Base de Datos:** MariaDB / MySQL.
* **Gestor de Dependencias:** Maven.

---

## 📋 Metodología Ágil: SCRUM

El desarrollo del sistema SCM se rigió bajo el marco de trabajo **SCRUM**, garantizando entregas de valor incrementales y adaptabilidad continua.

* **Sprints:** Ciclos de desarrollo estructurados de 2 semanas.
* **Product Backlog:** Dividido de manera estricta por roles de usuario (Módulo Admin, Módulo Veterinario, Módulo Dueño).
* **Roles Clave:**
    * *Product Owner:* Encargado de priorizar los criterios de aceptación médicos y de auditoría clínica.
    * *Scrum Master / Dev Team:* Encargado del diseño arquitectónico de base de datos, lógica de negocio y control de transacciones de la plataforma.
* **Eventos Clave:** Ejecución rigurosa de *Daily Scrums* para mitigar bloqueos de código (como errores de renderizado en Thymeleaf o fallas de desajuste en tipos de datos de fecha) y *Sprint Reviews* para validar el correcto despliegue cloud.

---

## 💻 Configuración e Instalación Local

### Prerrequisitos
* Java Development Kit (JDK) 17 o superior.
* Apache Maven instalado.
* Motor de Base de Datos MariaDB o MySQL activo.

### 1. Clonar el repositorio
```bash
git clone [https://github.com/tu-usuario/scmspringbot.git](https://github.com/tu-usuario/scmspringbot.git)
cd scmspringbot
2. Configurar Variables de Entorno LocalesEdita el archivo src/main/resources/application.properties para adaptarlo a tu entorno local de desarrollo:Propertiesserver.port=8080
spring.application.name=scm

# Base de Datos Local
spring.datasource.url=jdbc:mysql://localhost:3306/scmDos?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=TU_CONTRASEÑA_LOCAL
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Propiedades JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# Proveedor de Email (Gmail / Brevo SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu_cuenta_autenticada@gmail.com
spring.mail.password=tu_token_de_aplicacion_16_digitos
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
3. Ejecutar la AplicaciónBashmvn spring-boot:run
La aplicación estará disponible en http://localhost:8080.☁️ Despliegue en RailwayLa arquitectura de SCM está preparada para entornos de Integración Continua y Despliegue Continuo (CI/CD). Sigue estos pasos para desplegar en Railway:1. Base de Datos en la NubeEn tu dashboard de Railway, haz clic en New Project y selecciona Provision MySQL o Provision MariaDB.Espera a que se cree el contenedor de la base de datos y copia las credenciales provistas por la plataforma.2. Despliegue del Servicio Spring BootHaz clic en New, selecciona GitHub Repo y vincula tu repositorio scmspringbot.Ve a la pestaña de Variables del servicio de Spring Boot y configura los mapeos dinámicos utilizando las variables nativas que inyecta Railway automáticamente:Fragmento de códigoPORT=${{PORT}}
MYSQLHOST=${{MYSQL_HOST}}
MYSQLPORT=${{MYSQL_PORT}}
MYSQLDATABASE=${{MYSQL_DATABASE}}
MYSQLUSER=${{MYSQL_USER}}
MYSQLPASSWORD=${{MYSQL_PASSWORD}}
MAIL_PASSWORD=tu_token_de_servidor_de_correos
3. Configuración Productiva (application.properties para Cloud)El archivo lee dinámicamente el entorno provisto por Railway sin exponer datos sensibles:Propertiesserver.port=${PORT}
spring.datasource.url=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}
spring.datasource.username=${MYSQLUSER}
spring.datasource.password=${MYSQLPASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
Railway compilará el proyecto usando el Buildpack de Java automáticamente y generará un dominio público HTTPS listo para producción.📊 Especificación de Carga Masiva (Formato de Archivo)Para utilizar el servicio de carga masiva de usuarios sin incurrir en excepciones de base de datos, el archivo Excel (.xlsx) provisto al sistema debe carecer de filas nulas e incluir obligatoriamente 7 columnas estructuradas bajo el siguiente orden estricto:Columna AColumna BColumna CColumna DColumna EColumna FColumna GNombreApellidoEmailTeléfonoDirecciónFecha NacimientoRol IDTextoTextoTexto (Único)NuméricoTextoTexto (YYYY-MM-DD)1, 2 o 3
