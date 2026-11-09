# Aqua-Registro | Planteamiento teorico

- [Version Actual](#version-actual)
    - [Inicio de sesion / Registrar usuario](#inicio-de-sesion--registrar-usuario)
    - [Pantalla principal](#pantalla-principal)
    - [Registrar Faena](#registrar-faena)
    - [Agregar buzo](#agregar-buzo)
    - [Historial](#historial)
    - [Control de equipos](#control-de-equipos)
## Version Actual

### Inicio de sesion / Registrar usuario

Existe un usuario por defecto que ignora las restricciones de rut y contraseña

Para registrar a un usuario (prevencionista) se deben dar los siguientes datos

- Nombre completo
- RUT
- N° Registro del SEREMI ([Registro de expertos](https://seremienlinea.minsal.cl/asdigital/index.php?tramites#))
- Correo
- Centro de operaciones
- Contraseña
- Aceptar terminos de uso (No especificados)

Tras completar correctamente los datos solicitados se inicia la sesion automaticamente

### Pantalla principal

Desde esta pantalla se tiene acceso a todas las funciones principales de la aplicacion

- Registro de faena
- Agregar buzo
- Historial
- Cuadrilla

Ademas de algunos complementos como

- Nombre de usuario actual
- Centro donde se esta trabajando
- Estado off-line
- Accesos rapidos

El estado off-line se justifica ya que se considera que los trabajos se realizaran en zonas donde sea dificil acceder a internet

### Registrar Faena

Es el boton principal y permite crear una faena, que debe ser completada en el boton [Historial](#historial), y solicita los siguientes datos

- Buzo asignado (apto)
- Centro de operacion
- Tipo de faena
- Profundidad
- Tiempo de inmersion
- Supervisor

Al presionar existen datos por defecto

### Agregar buzo

Los buzos son registrados con los siguientes datos sin falta en una ventana emergente

- Nombre y Apellido
- RUT
- Tipo de buzo (inclusive supervisor)
- Telefono de contacto
- Empresa
- Vigencia de matricula

En la misma pantalla y en accesos rapidos existe un boton que permite registrar empresas, ya que no todos los buzos trabajan directamente con Aquachile

### Historial

En este boton se completan y verifican los datos solicitados para dar por terminada la faena los cuales son

- Datos de faena (creacion)
- Pre-equipacion y Equipado
- Encuesta de salud antes y despues de inmersion

Los datos pueden ser editados en caso de errores, y los demas se completan en pantallas dedicadas

### Control de equipos

En esta pantalla se debe realizar 3 tareas en 9 partes del equipo del buzo

- Evaluacion de estado
    - Aceptable, Riesgoso, Inutilizable
- Descripcion del equipo
    - Opcional en Aceptable, obligatorio en Riesgoso e Inutilizable
- Fotografia

Este proceso se realiza 2 veces por faena, una antes y despues de equiparse


