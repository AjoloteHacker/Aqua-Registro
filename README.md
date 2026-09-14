# 🌊 Aqua-Registro (MVP)

**Aqua-Registro** es una aplicación móvil diseñada para resolver la desorganización en el registro manual de buzos y sus trabajos Su propósito es digitalizar el pre-chequeo de seguridad, validando la identidad del buzo, la revisión de sus equipos y la encuesta de salud antes de cada inmersión, centralizando toda la información para el Prevencionista de Riesgos.


## 🎨 Identidad Visual

La interfaz está construida utilizando **Material Design 3** con la siguiente paleta de colores corporativa para transmitir confianza y alinearse con el entorno marino:

- **Principal:** Azul Claro (`\#0099FF`) - Usado en top bars y botones principales.

- **Secundario:** Verde Agua (`\#00CCCC`) - Usado en interfaces secundarias y botones de acción afirmativa\[cite: 2\].

- **Fondo:** Blanco (`\#FFFFFF`) - Para máxima legibilidad\[cite: 2\].

- **Texto:** Negro (`\#000000`) - Contraste principal\[cite: 2\].

- **Adicional:** Aqua (`\#00FFFF`) - Color de apoyo\[cite: 2\].

## 🗺️ Flujo de Usuario (UML)

A continuación, se presenta el Diagrama de Actividad UML con el recorrido principal del prevencionista dentro de la aplicación:

```mermaid
stateDiagram-v2  
    \[\*\] --\> InicioDeSesion : Iniciar app  
      
    InicioDeSesion --\> DecisionLogin : Ingresar credenciales  
    DecisionLogin --\> InicioDeSesion : \[Credenciales Inválidas\]  
    DecisionLogin --\> MenuPrincipal : \[Acceso Aprobado\]  
      
    MenuPrincipal --\> RegistroBuzo : Seleccionar módulo  
      
    state RegistroBuzo \{  
        \[\*\] --\> IngresoDatos  
        IngresoDatos --\> ValidacionExamenes : Revisar Exámenes Ocupacionales  
        ValidacionExamenes --\> EscuelaBuceo : Participación AquaChile  
    \}  
      
    RegistroBuzo --\> DecisionExamenes  
    DecisionExamenes --\> \[\*\] : \[No Vigente / No Apto\]  
    DecisionExamenes --\> RegistroEquipos : \[Buzo Validado\]  
      
    RegistroEquipos --\> EncuestaSalud : Checklist de implementos  
      
    EncuestaSalud --\> DecisionSalud : Medición parámetros  
    DecisionSalud --\> \[\*\] : \[No Apto\]  
    DecisionSalud --\> RegistroTrabajo : \[Estado Apto\]   
      
    RegistroTrabajo --\> EncuestaPostTrabajo : Planificación AST y Faena  
    EncuestaPostTrabajo --\> Historial : Guardar registros  
      
    Historial --\> \[\*\] : Fin de la tarea
```

