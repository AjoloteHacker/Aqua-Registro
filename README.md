stateDiagram-v2
    [*] --> InicioDeSesion : Iniciar app
    
    InicioDeSesion --> DecisionLogin : Ingresar credenciales
    DecisionLogin --> InicioDeSesion : [Credenciales Inválidas]
    DecisionLogin --> MenuPrincipal : [Acceso Aprobado]
    
    MenuPrincipal --> RegistroBuzo : Seleccionar módulo
    
    state RegistroBuzo {
        [*] --> IngresoDatos
        IngresoDatos --> ValidacionExamenes : Revisar Exámenes Ocupacionales
        ValidacionExamenes --> EscuelaBuceo : Participación AquaChile
    }
    
    RegistroBuzo --> DecisionExamenes
    DecisionExamenes --> [*] : [No Vigente / No Apto]
    DecisionExamenes --> RegistroEquipos : [Buzo Validado]
    
    RegistroEquipos --> EncuestaSalud : Checklist de implementos
    
    EncuestaSalud --> DecisionSalud : Medición parámetros
    DecisionSalud --> [*] : [No Apto]
    DecisionSalud --> RegistroTrabajo : [Estado Apto]
    
    RegistroTrabajo --> EncuestaPostTrabajo : Planificación AST y Faena
    EncuestaPostTrabajo --> Historial : Guardar registros
    
    Historial --> [*] : Fin de la tarea
