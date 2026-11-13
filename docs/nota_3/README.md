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

### Error de sistema / Chequeo de salud

Existe una propiedad de salud que no puede actualizarse, debido a que se elimino el acceso a sus pantallas, esto impide que los buzos nuevos puedan ser elegidos en las faenas, ya que por defecto aun los deja con `Chequeo pendiente`

## Cambios a realizar

### Eliminacion de botones laterales

Los accesos directos a Registro Faena, Encuesta de salud y Control de equipos pueden causar confucion y saturan el boton lateral

### Mejora / Rango de numeros en "ruedas"

Para facilitar el ingreso de datos como profundidad, tiempo, pulso, etc se cambiara sus "cajas de texto" a "ruedas"

#### Mejora / Apoyo visual

Todos los nombres de datos ahora tendran un icono de apoyo, para mejorar la visibilidad en condiciones de lluvia, niebla o similar

Todos los titulos, cajas de texto y demas ahora se mantendran en una sola linea

### Cambio / Supervisor implicito

En las faenas se puede cambiar al supervisor, esto es innecesario ya que solo hay un supervisor por faena, y por ende, siempre es el responsable a la hora de crearla en su sesion

### Cambio / Creacion de faena (CdF)

Las faenas tienen una serie de datos limitada a la hora de ser creadas, pero el usuario debe ser capaz de crear y terminar una faena desde que la empieza

#### CdF / 2 buzos

Para realizar una faena se nesecitan 2 buzos, el que la realiza y un socorrista, esto tambien implica que los procesos que realiza el buzo se hacen 2 veces mas para el socorrista

#### CdF / Excepciones de socorrista

Ya que el socorrista no necesariamente participa en la inmersion directamente, el usuario debe ser capaz de cerrar una faena completada sin la necesidad de cumplir la encuesta de salud post-inmersion

#### Cambio / Flujo de equipo

Previo a inmersion: Ahora basta con una foto general y el estado individual de los equipos

Equipado: Ahora se exige una foto de frente y atras, ademas, ahora se comparte el estado con previo a inmersion. La fase de encuesta de salud ahora puede realizarce sin terminar el chequeo de equipo

#### Cambio / Distincion de encuesta de salud y ficha de salud

La ficha de salud es parte del perfil del buzo y contiene los siguientes datos

- Estatura
- Peso
- Alergias
- Edad

Junto a estos datos almacenados explicitos se debe adjuntar el Certificado de evaluacion laboral de salud de la ACHS, ya que esta app es un prototipo, se aceptara cualquier archivo pdf por ahora

El chequeo de salud ya no solicitara estos datos
Ahora el chequeo de salud es independiente de el de equipo
Todos los datos de medicion tendran un rango de valores aceptable, riesgoso e incapacitante

#### Mejora / Formatos automaticos

Los siguientes datos tendran formato automatico en su ingreso

- RUT
- Número
    - Se agregaran paises para dar los formatos adecuados

#### Mejora / Fechas y alertas

La vigencia de las matriculas de buzo se almacenara como con la fecha de "inicio" en las fichas de cada buzo, dando una advertencia invasiva cuando queden 60 a 15 dias, y dando un pop up al iniciar sesion cuando menos de 15 dias

La advertencia invasiva se puede pasar a no invasiva con una opcion en la ficha del buzo o en Cuadrilla

Se lanzara una ultima advertencia cuando la ficha este vencida, no se impedira que el buzo participe en faenas

Si un buzo con la matricula vencida participa en una faena, se dara una advertencia bajo su nombre, tanto en la pantalla de creacion de faena como en el historial

