package com.example.myapplication.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AquaRepositoryBusinessExtraTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: AquaRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AquaRepository.getInstance()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun servidorUrl_configuracion() {
        val urlOriginal = repository.serverUrl.value
        repository.cambiarUrlServidor("https://mi-servidor.com/api/")
        assertEquals("https://mi-servidor.com/api/", repository.serverUrl.value)

        repository.cambiarUrlServidor(urlOriginal)
        assertEquals(urlOriginal, repository.serverUrl.value)

        repository.probarConexionServidor()
        assertNotNull(repository.ultimoPingDetalle.value)
    }

    @Test
    fun modoOfflineDemo_alternar() {
        val onlineInicial = repository.isNetworkConnected.value
        repository.alternarModoOfflineDemo()
        assertEquals(!onlineInicial, repository.isNetworkConnected.value)
        repository.alternarModoOfflineDemo()
        assertEquals(onlineInicial, repository.isNetworkConnected.value)
    }

    @Test
    fun bitacoraPostInmersion_guardarYConsultar() {
        val faenas = repository.faenas.value
        if (faenas.isNotEmpty()) {
            val f = faenas.first()
            val res = repository.registrarBitacoraPostInmersion(
                faenaId = f.id,
                profundidadMetros = 22,
                tiempoFondoMinutos = 40,
                velocidadDescensoMpm = 26.0, // > 24 m/min alerta
                velocidadAscensoMpm = 10.0, // > 9 m/min alerta
                origenDato = "Census",
                observaciones = "Prueba velocidad"
            )
            assertTrue(res.isSuccess)
            val bitacora = res.getOrNull()
            assertNotNull(bitacora)
            assertTrue(bitacora!!.alertaVelocidadSuperada)
            assertEquals("ALERTA NOTIFICADA A JEFE DE CENTRO", bitacora.estadoRevision)
        }
    }

    @Test
    fun agregarEmpresa_validacion() {
        val resVacia = repository.agregarEmpresa("")
        assertTrue(resVacia.isFailure)

        val resOk = repository.agregarEmpresa("Empresa Test Extra " + System.currentTimeMillis())
        assertTrue(resOk.isSuccess)
    }

    @Test
    fun climaMaritimo_consulta() {
        repository.consultarClimaMaritimo("Centro Melinka 1")
        assertNotNull(repository.climaMaritimo.value)
    }

    @Test
    fun bitacoraPostInmersion_validacionesYVelocidadesNormales() {
        // Faena inexistente
        val resNoFaena = repository.registrarBitacoraPostInmersion("NO_EXISTE", 10, 10, 10.0, 5.0, "", "")
        assertTrue(resNoFaena.isFailure)

        val faena = repository.faenas.value.first()
        // Profundidad inválida
        val resProf = repository.registrarBitacoraPostInmersion(faena.id, 0, 10, 10.0, 5.0, "", "")
        assertTrue(resProf.isFailure)

        // Tiempo inválido
        val resTiempo = repository.registrarBitacoraPostInmersion(faena.id, 10, 0, 10.0, 5.0, "", "")
        assertTrue(resTiempo.isFailure)

        // Velocidad descenso inválida
        val resDesc = repository.registrarBitacoraPostInmersion(faena.id, 10, 10, 0.0, 5.0, "", "")
        assertTrue(resDesc.isFailure)

        // Velocidad ascenso inválida
        val resAsc = repository.registrarBitacoraPostInmersion(faena.id, 10, 10, 10.0, 0.0, "", "")
        assertTrue(resAsc.isFailure)

        // Velocidades normales (sin alerta de velocidad)
        val resNormal = repository.registrarBitacoraPostInmersion(
            faenaId = faena.id,
            profundidadMetros = 15,
            tiempoFondoMinutos = 30,
            velocidadDescensoMpm = 18.0, // <= 24 m/min OK
            velocidadAscensoMpm = 6.0,  // <= 9 m/min OK
            origenDato = "",
            observaciones = "Inmersión normal"
        )
        assertTrue(resNormal.isSuccess)
        assertFalse(resNormal.getOrNull()!!.alertaVelocidadSuperada)
    }

    @Test
    fun completarFaena_validacionesYRoles() {
        // 1. Rol no supervisor intenta cerrar faena
        repository.login("18.765.432-K", "operador123") // Matías Oyarzún (OPERADOR_BUCEO)
        val resRol = repository.completarFaena("F-101")
        assertTrue(resRol.isFailure)

        // 2. Supervisor intenta cerrar faena inexistente
        repository.login("12.345.678-5", "admin123") // Cristóbal Loncón (SUPERVISOR)
        val resNoExiste = repository.completarFaena("FAENA_INVENTADA")
        assertTrue(resNoExiste.isFailure)

        // 3. Faena incompleta
        val resIncompleta = repository.completarFaena("F-102")
        assertTrue(resIncompleta.isFailure)
    }

    @Test
    fun equiposPorBuzo_guardarYConsultar() {
        val buzoId = "1"
        val eqs = repository.obtenerEquiposDefault()
        repository.guardarEquiposParaBuzo(buzoId, eqs)
        val obtenidos = repository.obtenerEquiposParaBuzo(buzoId)
        assertEquals(eqs.size, obtenidos.size)

        assertFalse(repository.usuarios.value.isEmpty())
        assertFalse(repository.equiposPorBuzo.value.isEmpty())
    }

    @Test
    fun sincronizarAhora_flujos() {
        repository.sincronizarAhora()
        repository.alternarModoOfflineDemo()
        repository.sincronizarAhora()
        repository.alternarModoOfflineDemo()
    }

    @Test
    fun buzo_crudExtra() {
        val buzos = repository.buzos.value
        assertFalse(buzos.isEmpty())
        val primerBuzo = buzos.first()

        val buzoMod = primerBuzo.copy(telefono = "+56987654321")
        val res = repository.editarBuzo(buzoMod)
        assertTrue(res.isSuccess)
        assertEquals("+56987654321", repository.buzos.value.first { it.id == primerBuzo.id }.telefono)
    }
}
