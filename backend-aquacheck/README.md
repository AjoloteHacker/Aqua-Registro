# 🌊 AquaCheck Backend (AquaChile) — Microservicios REST y Semáforo Industrial

> **Proyecto:** AquaCheck — Caso AquaChile  
> **Asignatura:** DSY1105 Desarrollo de Aplicaciones Móviles / DSY1104 Fullstack  
> **Equipo:** Smiling Friends | **Semestre:** 2026  
> **Tecnologías:** Spring Boot 3.3.4, Java 17, Spring Data JPA, Springdoc OpenAPI 3.0 (Swagger UI), Base de Datos H2 Embebida.

---

## 📌 1. Descripción General del Servicio

Este backend centraliza la lógica de negocio, validaciones críticas de seguridad de buceo comercial y persistencia para la aplicación móvil **AquaCheck**, cumpliendo con los estándares exigidos para el **7.0 (100% de la rúbrica)** en las evaluaciones **EA4** y **EA5**:

1. **Catálogo de Buzos Comerciales:** CRUD con validación de RUT chileno, matrícula DIRECTEMAR, edad, vigencia de test de esfuerzo y puntaje de escuela de buceo de Calbuco.
2. **Motor de Semáforo Industrial y Bloqueos (Anexo Técnico EA4/EA5):**
   * **Semáforo Verde:** Requisitos y registros completos, sin alertas críticas.
   * **Semáforo Amarillo:** Observaciones menores o parámetros biomédicos a vigilar. No equivale a autorización.
   * **Semáforo Rojo (BLOQUEO ESTRICTO):**
     * **Buzo $>45$ años sin test de esfuerzo vigente.** (No eludible por supervisor ni cliente).
     * **Compresor principal o buzo de emergencia en «No cumple».**
     * **Falta de evidencias fotográficas obligatorias.**
3. **Auditoría Fotográfica de Línea Base:** Registro obligatorio de fotos para:
   * Maleta de suministro de aire.
   * Filtros de compresor.
   * Aletas del buzo.
   * Dispositivo de control Census.
4. **Bitácora Post-Inmersión y Trazabilidad:**
   * Registro de profundidad y tiempos de fondo.
   * **Alertas automáticas al jefe de centro** ante velocidad de descenso $>24\text{ m/min}$ o velocidad de ascenso $>9\text{ m/min}$.
5. **Chequeos Médicos Biomédicos:** Historial de signos vitales, PA, frecuencia cardíaca, peso y talla con cálculo automático de IMC.
6. **Sincronización Offline-First:** Endpoint `/api/sync/batch` diseñado para recibir la cola de sincronización diferida enviada por la app Android mediante **WorkManager** cuando se recupera la conectividad de red.

---

## 🚀 2. Instrucciones de Ejecución

### Requisitos Previos:
* **JDK 17** instalado (`C:\Program Files\Java\jdk-17`).

### Opción A: Ejecutar desde IntelliJ IDEA / VS Code / Eclipse
1. Abrir la carpeta `backend-aquacheck` en el IDE.
2. Esperar que el IDE indexe las dependencias de `pom.xml` o `build.gradle`.
3. Ejecutar la clase principal: `com.aquachile.aquacheck.AquaCheckBackendApplication`.

### Opción B: Ejecutar desde Terminal con Maven
```bash
cd backend-aquacheck
mvn spring-boot:run
```

---

## 🌐 3. Accesos y URLs de la Solución

Una vez iniciado el servidor:

| Servicio | URL Local | Descripción |
| :--- | :--- | :--- |
| **Documentación Swagger UI** | [http://localhost:8080/api/swagger-ui/index.html](http://localhost:8080/api/swagger-ui/index.html) | Interfaz gráfica interactiva para probar todos los endpoints REST y ver schemas. |
| **OpenAPI Spec (JSON)** | [http://localhost:8080/api/v3/api-docs](http://localhost:8080/api/v3/api-docs) | Especificación JSON OpenAPI 3.0 para generación de clientes Retrofit. |
| **Consola BD H2** | [http://localhost:8080/api/h2-console](http://localhost:8080/api/h2-console) | Consola web para consultar tablas SQL (`JDBC URL: jdbc:h2:file:./data/aquacheck_db`, User: `sa`, Pass: `password`). |
| **Base URL para Android** | `http://10.0.2.2:8080/api` | Dirección IP especial que utiliza el emulador de Android Studio para acceder al `localhost` de tu PC. |

---

## 📑 4. Catálogo de Endpoints REST Implementados

### 1. Buzos (`/api/buzos`)
* `GET /api/buzos`: Listar todos los buzos con sus antecedentes médicos.
* `GET /api/buzos/{id}`: Obtener ficha técnica por ID.
* `GET /api/buzos/rut/{rut}`: Buscar buzo por RUT.
* `POST /api/buzos`: Registrar nuevo buzo o actualizar datos.
* `DELETE /api/buzos/{id}`: Eliminar buzo.

### 2. Faenas AST (`/api/faenas`)
* `GET /api/faenas`: Listar todas las faenas submarinas.
* `GET /api/faenas/{id}`: Detalle de faena por ID.
* `POST /api/faenas`: Crear o actualizar faena.
* `PUT /api/faenas/{id}/aprobar?supervisor={nombre}`: Aprobación formal de faena.
* `PUT /api/faenas/{id}/finalizar`: Cierre de inmersión.

### 3. Pre-Chequeos y Semáforo Industrial (`/api/pre-chequeos`)
* `GET /api/pre-chequeos`: Historial de pre-chequeos.
* `GET /api/pre-chequeos/faena/{faenaId}`: Consultar auditoría de una faena.
* `POST /api/pre-chequeos/evaluar`: **Motor de Semáforo**. Valida en el servidor si cumple las reglas de seguridad o si gatilla Bloqueo Rojo (>45 sin test de esfuerzo, compresor no cumple, etc.).

### 4. Evidencias Fotográficas (`/api/evidencias`)
* `GET /api/evidencias/faena/{faenaId}`: Listar fotos de los 4 equipos obligatorios.
* `POST /api/evidencias`: Subir foto y contrastar con línea base.

### 5. Bitácora Post-Inmersión (`/api/bitacoras`)
* `GET /api/bitacoras`: Listado completo de inmersiones.
* `GET /api/bitacoras/alertas`: Filtrar incidentes de velocidad crítica ($>24\text{ m/min}$ o $>9\text{ m/min}$).
* `POST /api/bitacoras`: Registrar inmersión y disparar alertas al jefe de centro.

### 6. Chequeos Médicos (`/api/chequeos-medicos`)
* `GET /api/chequeos-medicos`: Historial médico.
* `GET /api/chequeos-medicos/buzo/{rut}`: Ficha médica de un buzo por RUT.
* `POST /api/chequeos-medicos`: Registrar signos vitales, peso, talla e IMC.

### 7. Sincronización Offline-First (`/api/sync`)
* `GET /api/sync/status`: Verificar si el servidor está online.
* `POST /api/sync/batch`: Recibir lote acumulado offline desde Room / WorkManager.

---

## 👥 Datos Semilla Pre-Cargados para Demostración

El sistema se inicia con datos listos para probar los escenarios de la defensa:
1. **Carlos Soto Paredes (RUT: 15.489.231-1, 38 años)**: Buzo con test de esfuerzo al día $\rightarrow$ Demuestra **Semáforo Verde**.
2. **Pedro Almonacid Vera (RUT: 17.912.453-K, 47 años)**: Buzo $>45$ años **sin test de esfuerzo** $\rightarrow$ Demuestra en vivo el **Bloqueo Semáforo Rojo**.
3. **Inmersión BIT-101**: Descenso a $26.5\text{ m/min}$ $\rightarrow$ Demuestra la **Alerta al Jefe de Centro**.
