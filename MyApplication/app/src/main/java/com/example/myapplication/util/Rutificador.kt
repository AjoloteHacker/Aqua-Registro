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

    /**
     * Formatea el RUT automáticamente mientras el usuario escribe, manteniendo
     * los nuevos dígitos al final (después del guion si ya estaba formateado)
     * para evitar que el cursor de Compose quede antes del dígito verificador.
     */
    fun formatearEnVivo(rut: String): String {
        val raw = rut.filter { it.isDigit() || it.equals('k', ignoreCase = true) }.uppercase()
        // Solo se permite dígitos en el cuerpo y dígito o 'K' al final (máximo 9 caracteres: 8 de cuerpo + 1 DV)
        val limpio = buildString {
            for (ch in raw) {
                if (length >= 9) break
                if (ch.isDigit()) {
                    append(ch)
                } else if (ch == 'K' && isNotEmpty() && !contains('K')) {
                    append(ch)
                }
            }
        }
        // Si tiene una 'K' intermedia y luego se escribieron más dígitos, mover o ignorar la K intermedia
        val normalizado = if (limpio.indexOf('K') in 0 until limpio.lastIndex) {
            limpio.replace("K", "")
        } else {
            limpio
        }
        if (normalizado.length < 2) return normalizado
        return formatear(normalizado)
    }
}

/**
 * Países disponibles para formateo automático de números telefónicos (docs/nota_3).
 */
enum class PaisTelefono(
    val codigo: String,
    val etiqueta: String,
    val prefijo: String,
    val codigoPaisDigitos: String,
    val maxDigitosLocales: Int
) {
    CHILE("CL", "Chile (+56)", "+56", "56", 9),
    ARGENTINA("AR", "Argentina (+54)", "+54", "54", 10),
    PERU("PE", "Perú (+51)", "+51", "51", 9);

    /**
     * Extrae únicamente los dígitos locales del número, removiendo cualquier prefijo
     * internacional (`+56`, `+54`, `+51`) para que se pueda escribir y borrar sin bloqueos.
     */
    fun extraerDigitosLocales(entrada: String): String {
        val trimmed = entrada.trim()
        // Si ya viene con prefijo explícito "+XX " o "+XX", quitar ese prefijo primero
        val sinPrefijoExplicito = PaisTelefono.entries.firstOrNull {
            trimmed.startsWith("${it.prefijo} ") || trimmed.startsWith(it.prefijo)
        }?.let { paisDetectado ->
            trimmed.removePrefix(paisDetectado.prefijo).trimStart()
        } ?: trimmed

        val soloDigitos = sinPrefijoExplicito.filter { it.isDigit() }
        val sinCodigoPais = if (
            soloDigitos.startsWith(codigoPaisDigitos) &&
            soloDigitos.length > maxDigitosLocales
        ) {
            soloDigitos.drop(codigoPaisDigitos.length)
        } else {
            soloDigitos
        }
        return sinCodigoPais.take(maxDigitosLocales)
    }

    fun formatearNumero(entrada: String): String {
        val sinPrefijo = extraerDigitosLocales(entrada)
        if (sinPrefijo.isEmpty()) return ""

        return when (this) {
            CHILE -> {
                val p1 = sinPrefijo.take(1)
                val p2 = sinPrefijo.drop(1).take(4)
                val p3 = sinPrefijo.drop(5).take(4)
                buildString {
                    append("+56 ").append(p1)
                    if (p2.isNotEmpty()) append(" ").append(p2)
                    if (p3.isNotEmpty()) append(" ").append(p3)
                }
            }
            ARGENTINA -> {
                val p1 = sinPrefijo.take(2)
                val p2 = sinPrefijo.drop(2).take(4)
                val p3 = sinPrefijo.drop(6).take(4)
                buildString {
                    append("+54 ").append(p1)
                    if (p2.isNotEmpty()) append(" ").append(p2)
                    if (p3.isNotEmpty()) append(" ").append(p3)
                }
            }
            PERU -> {
                val p1 = sinPrefijo.take(3)
                val p2 = sinPrefijo.drop(3).take(3)
                val p3 = sinPrefijo.drop(6).take(3)
                buildString {
                    append("+51 ").append(p1)
                    if (p2.isNotEmpty()) append(" ").append(p2)
                    if (p3.isNotEmpty()) append(" ").append(p3)
                }
            }
        }
    }
}
