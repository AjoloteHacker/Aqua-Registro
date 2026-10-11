package com.example.myapplication.util

/**
 * Utilidad reutilizable para la validación, cálculo y formateo de RUT (Rol Único Tributario) chileno
 * utilizando el algoritmo oficial de Módulo 11.
 */
object Rutificador {

    /**
     * Limpia el RUT eliminando puntos, guiones, espacios y convirtiendo la letra a mayúscula.
     * Ejemplo: " 12.345.678-k " -> "12345678K"
     */
    fun limpiar(rut: String): String {
        return rut.trim()
            .replace(".", "")
            .replace("-", "")
            .replace(" ", "")
            .uppercase()
    }

    /**
     * Calcula el dígito verificador para un cuerpo numérico dado según Módulo 11.
     */
    fun calcularDv(cuerpo: String): String {
        val limpio = cuerpo.filter { it.isDigit() }
        if (limpio.isEmpty()) return ""

        var suma = 0
        var multiplicador = 2

        for (i in limpio.indices.reversed()) {
            suma += (limpio[i] - '0') * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }

        val resto = 11 - (suma % 11)
        return when (resto) {
            11 -> "0"
            10 -> "K"
            else -> resto.toString()
        }
    }

    /**
     * Valida si una cadena corresponde a un RUT chileno matemáticamente válido.
     */
    fun validar(rut: String): Boolean {
        val limpio = limpiar(rut)
        // RUT chileno estándar tiene entre 7 y 9 caracteres (6 a 8 dígitos de cuerpo + 1 dígito verificador)
        if (limpio.length < 7 || limpio.length > 9) return false

        val cuerpo = limpio.dropLast(1)
        val dv = limpio.takeLast(1)

        // El cuerpo debe ser estrictamente numérico
        if (!cuerpo.all { it.isDigit() }) return false

        // Evitar números inválidos como puros ceros
        val cuerpoNumerico = cuerpo.toLongOrNull() ?: return false
        if (cuerpoNumerico < 100_000L) return false

        val dvEsperado = calcularDv(cuerpo)
        return dv.equals(dvEsperado, ignoreCase = true)
    }

    /**
     * Formatea un RUT en la representación canónica chilena "XX.XXX.XXX-Y".
     * Si no tiene la longitud mínima o no es numérico, retorna la cadena limpia o la original.
     */
    fun formatear(rut: String): String {
        val limpio = limpiar(rut)
        if (limpio.length < 2) return limpio

        val cuerpo = limpio.dropLast(1)
        val dv = limpio.takeLast(1)

        if (!cuerpo.all { it.isDigit() }) return rut

        val sb = StringBuilder()
        for (i in cuerpo.indices) {
            sb.append(cuerpo[i])
            val remaining = cuerpo.length - 1 - i
            if (remaining > 0 && remaining % 3 == 0) {
                sb.append('.')
            }
        }
        return "$sb-$dv"
    }
}
