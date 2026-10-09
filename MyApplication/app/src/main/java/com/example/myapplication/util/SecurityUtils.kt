package com.example.myapplication.util

/**
 * Validaciones de seguridad para el registro de usuarios y control de integridad de datos.
 */
object SecurityUtils {

    private val REGEX_NOMBRE = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s'-]{5,60}$")
    private val REGEX_EMAIL = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    private val REGEX_SEREMI = Regex("^PR-\\d{3,6}-[A-Za-z0-9]$", RegexOption.IGNORE_CASE)

    /**
     * Valida que el nombre contenga al menos dos palabras (nombre y apellido),
     * solo caracteres alfabéticos válidos y longitud adecuada.
     */
    fun validarNombre(nombre: String): Pair<Boolean, String?> {
        val trimmed = nombre.trim()
        if (trimmed.isBlank()) {
            return Pair(false, "El nombre completo es obligatorio.")
        }
        if (!REGEX_NOMBRE.matches(trimmed)) {
            return Pair(false, "El nombre solo debe contener letras y tener entre 5 y 60 caracteres.")
        }
        val palabras = trimmed.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (palabras.size < 2) {
            return Pair(false, "Debe ingresar tanto el nombre como el apellido.")
        }
        if (palabras.any { it.length < 2 }) {
            return Pair(false, "Cada parte del nombre debe contener al menos 2 caracteres.")
        }
        return Pair(true, null)
    }

    /**
     * Valida que el correo electrónico cumpla con una estructura válida de email.
     */
    fun validarEmail(email: String): Pair<Boolean, String?> {
        val trimmed = email.trim()
        if (trimmed.isBlank()) {
            return Pair(false, "El correo institucional es obligatorio.")
        }
        if (!REGEX_EMAIL.matches(trimmed)) {
            return Pair(false, "El formato del correo es inválido (ejemplo: usuario@aquachile.cl).")
        }
        return Pair(true, null)
    }

    /**
     * Valida el formato del registro SEREMI de Salud (ej: PR-12345-X).
     */
    fun validarRegistroSeremi(seremi: String): Pair<Boolean, String?> {
        val trimmed = seremi.trim()
        if (trimmed.isBlank()) {
            return Pair(false, "El registro SEREMI es obligatorio.")
        }
        if (!REGEX_SEREMI.matches(trimmed)) {
            return Pair(false, "El registro SEREMI debe cumplir con el formato oficial (ej: PR-12345-X).")
        }
        return Pair(true, null)
    }

    /**
     * Valida la robustez de la contraseña:
     * - Mínimo 8 caracteres.
     * - Al menos una letra mayúscula.
     * - Al menos una letra minúscula.
     * - Al menos un número.
     */
    fun validarPassword(password: String): Pair<Boolean, String?> {
        if (password.length < 8) {
            return Pair(false, "La contraseña debe tener al menos 8 caracteres.")
        }
        if (!password.any { it.isUpperCase() }) {
            return Pair(false, "La contraseña debe incluir al menos una letra mayúscula.")
        }
        if (!password.any { it.isLowerCase() }) {
            return Pair(false, "La contraseña debe incluir al menos una letra minúscula.")
        }
        if (!password.any { it.isDigit() }) {
            return Pair(false, "La contraseña debe incluir al menos un número.")
        }
        return Pair(true, null)
    }

    /**
     * Valida que la confirmación de contraseña coincida exactamente con la original.
     */
    fun validarPasswordConfirm(password: String, confirm: String): Pair<Boolean, String?> {
        if (password != confirm) {
            return Pair(false, "Las contraseñas no coinciden.")
        }
        return Pair(true, null)
    }

    /**
     * Valida un RUT chileno utilizando el Rutificador oficial (Módulo 11).
     */
    fun validarRut(rut: String): Pair<Boolean, String?> {
        val trimmed = rut.trim()
        if (trimmed.isBlank()) {
            return Pair(false, "El RUT es obligatorio.")
        }
        if (!Rutificador.validar(trimmed)) {
            return Pair(false, "El RUT ingresado no es válido según el algoritmo oficial (Módulo 11).")
        }
        return Pair(true, null)
    }
}
