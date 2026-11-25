package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Buzo
import com.example.myapplication.data.model.EstadoEquipo
import com.example.myapplication.data.model.EstadoSalud
import com.example.myapplication.data.model.EtapaComprobacion
import com.example.myapplication.data.model.MomentoSalud
import com.example.myapplication.data.model.Prevencionista
import com.example.myapplication.data.model.RegistroFaena
import com.example.myapplication.data.model.RevisionEquipo
import com.example.myapplication.data.model.TipoFotoEquipos
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AquaRepositoryBusinessTest {

    private lateinit var repository: AquaRepository

    @Before
    fun setUp() {
        repository = AquaRepository.getInstance()
    }

    @Test
    fun login_credencialesValidas_retornaExito() {
        // Usuario por defecto: "12.345.678-5" / "admin123"
        val resultado = repository.login("12.345.678-5", "admin123")
        assertTrue(resultado.isSuccess)
        assertEquals("Cristóbal Loncón C.", resultado.getOrNull()?.nombre)
    }

    @Test
    fun login_rutSinFormato_retornaExito() {
        // Normalización automática de RUT sin puntos ni guiones
        val resultado = repository.login("123456785", "admin123")
        assertTrue(resultado.isSuccess)
    }

    @Test
    fun login_contrasenaInvalida_retornaFallo() {
        val resultado = repository.login("12.345.678-5", "wrong_password")
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("inválidas") == true)
    }

    @Test
    fun registrarPrevencionista_rutDuplicado_retornaFallo() {
        val duplicado = Prevencionista(
            nombre = "Otro Usuario",
            rut = "12.345.678-5", // Ya existe
            correo = "otro@aquachile.com",
            registroSeremi = "PR-9999",
            centro = "Centro Melinka 1",
            contrasena = "123456"
        )
        val resultado = repository.registrarPrevencionista(duplicado)
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("Ya existe") == true)
    }

    @Test
    fun agregarBuzo_rutDuplicado_retornaFallo() {
        val buzoDuplicado = Buzo(
            id = "test-dup",
            rut = "15.489.231-1", // Ya existe en catálogo inicial (Carlos Soto)
            nombre = "Buzo Clon",
            empresa = "AquaSub",
            matricula = "N° 9999",
            telefono = "+56 9 1111 2222",
            estadoSalud = EstadoSalud.PENDIENTE
        )
        val resultado = repository.agregarBuzo(buzoDuplicado)
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("Ya existe un buzo") == true)
    }

    @Test
    fun eliminarBuzo_conFaenaEnCurso_retornaFallo() {
        // En catálogo inicial, Rodrigo Mansilla (ID 3) tiene faena F-102 en curso
        val resultado = repository.eliminarBuzo("3")
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("faena en curso") == true)
    }

    @Test
    fun eliminarBuzo_siendoSocorristaEnFaenaEnCurso_retornaFallo() {
        // Pedro Almonacid (ID 2) es el socorrista de F-102
        val resultado = repository.eliminarBuzo("2")
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("faena en curso") == true)
    }

    @Test
    fun agregarEmpresa_nombreValido_agregaExitosamente() {
        val nueva = "Austral Diving Safety Ltda"
        val resultado = repository.agregarEmpresa(nueva)
        assertTrue(resultado.isSuccess)
        assertTrue(repository.empresas.contains(nueva))
    }

    @Test
    fun agregarEmpresa_enBlanco_retornaFallo() {
        val resultado = repository.agregarEmpresa("   ")
        assertTrue(resultado.isFailure)
    }

    @Test
    fun estadoDelEquipoSeComparteEntreEtapas() {
        val faena = faenaDePruebaEquipos()
        repository.guardarRevision(
            faena.id,
            faena.buzoId,
            RevisionEquipo(
                equipoId = "e1",
                categoria = "General",
                nombre = "Traje de buceo",
                descripcionUso = "Estanco",
                estado = EstadoEquipo.ACEPTABLE
            )
        )

        val pre = repository.revisionesCombinadas(faena, faena.buzoId, EtapaComprobacion.PRE_EQUIPACION)
        val equipado = repository.revisionesCombinadas(faena, faena.buzoId, EtapaComprobacion.EQUIPADO)

        assertEquals(EstadoEquipo.ACEPTABLE, pre.first { it.equipoId == "e1" }.estado)
        assertEquals(EstadoEquipo.ACEPTABLE, equipado.first { it.equipoId == "e1" }.estado)
    }

    @Test
    fun estadoDelEquipoEsIndependientePorBuzo() {
        val faena = faenaDePruebaEquipos()
        repository.guardarRevision(
            faena.id,
            faena.buzoId,
            RevisionEquipo("e1", "General", "Traje de buceo", "Estanco", EstadoEquipo.ACEPTABLE)
        )
        repository.guardarRevision(
            faena.id,
            faena.socorristaId,
            RevisionEquipo("e1", "General", "Traje de buceo", "Estanco", EstadoEquipo.INUTILIZABLE)
        )

        val ejecutor = repository.revisionesCombinadas(faena, faena.buzoId, EtapaComprobacion.PRE_EQUIPACION)
        val socorrista = repository.revisionesCombinadas(faena, faena.socorristaId, EtapaComprobacion.PRE_EQUIPACION)

        assertEquals(EstadoEquipo.ACEPTABLE, ejecutor.first { it.equipoId == "e1" }.estado)
        assertEquals(EstadoEquipo.INUTILIZABLE, socorrista.first { it.equipoId == "e1" }.estado)
    }

    @Test
    fun evidenciaFotograficaEsIndependientePorEtapa() {
        val faena = faenaDePruebaEquipos()
        val pre = EtapaComprobacion.PRE_EQUIPACION
        val equipado = EtapaComprobacion.EQUIPADO

        repository.guardarFotoEvidencia(faena.id, faena.buzoId, pre, TipoFotoEquipos.GENERAL, "content://media/general.jpg")
        repository.guardarFotoEvidencia(faena.id, faena.buzoId, equipado, TipoFotoEquipos.FRENTE, "content://media/frente.jpg")

        val evidenciaPre = repository.obtenerEvidencia(faena.id, faena.buzoId, pre)
        val evidenciaEquipado = repository.obtenerEvidencia(faena.id, faena.buzoId, equipado)

        assertTrue(evidenciaPre.estaCompleta(pre))
        assertEquals(listOf(TipoFotoEquipos.ATRAS), evidenciaEquipado.faltan(equipado))
        assertNull(evidenciaPre.fotos[TipoFotoEquipos.FRENTE])
    }

    @Test
    fun evidenciaFotograficaEsIndependientePorBuzo() {
        val faena = faenaDePruebaEquipos()
        val etapa = EtapaComprobacion.PRE_EQUIPACION

        repository.guardarFotoEvidencia(faena.id, faena.buzoId, etapa, TipoFotoEquipos.GENERAL, "content://media/ejecutor.jpg")

        assertTrue(repository.obtenerEvidencia(faena.id, faena.buzoId, etapa).estaCompleta(etapa))
        assertFalse(repository.obtenerEvidencia(faena.id, faena.socorristaId, etapa).estaCompleta(etapa))
    }

    @Test
    fun avanceDeIncluyeAmbosBuzosYUnaSolaEncuestaPorMomento() {
        val faena = faenaDePruebaEquipos()
        repository.autorizarFaena(faena)
        assertTrue(repository.registrarEncuestaSalud(faena.id, MomentoSalud.ANTES, true, "120/80", 72).isSuccess)

        val avance = repository.avanceDe(faena)

        assertEquals(setOf(faena.buzoId, faena.socorristaId), avance.buzos.keys)
        assertTrue(avance.buzos.values.all { it.buzo != null })
        // Aunque haya dos buzos, la encuesta se registra una sola vez por faena y momento.
        assertEquals(1, avance.encuestas.size)
        assertTrue(MomentoSalud.ANTES in avance.encuestas)
    }

    @Test
    fun confirmarEtapaEsIndependientePorBuzo() {
        val faena = faenaDePruebaEquipos()
        val etapa = EtapaComprobacion.PRE_EQUIPACION

        repository.confirmarEtapa(faena.id, faena.buzoId, etapa)

        assertTrue(repository.etapaConfirmada(faena.id, faena.buzoId, etapa))
        assertFalse(repository.etapaConfirmada(faena.id, faena.socorristaId, etapa))
    }

    @Test
    fun modoOffline_marcaRegistrosComoPendientesDeSincronizar() {
        val EstabaConectado = repository.isNetworkConnected.value
        if (EstabaConectado) {
            repository.alternarModoOfflineDemo()
        }
        assertFalse(repository.isNetworkConnected.value)

        val faenaOffline = faenaDePruebaEquipos().copy(id = "F-OFFLINE-TEST")
        repository.autorizarFaena(faenaOffline)

        assertTrue("F-OFFLINE-TEST" in repository.pendientesSincronizarIds.value)
        assertTrue(repository.syncStatus.value.contains("Pendiente de sincronizar"))

        // Restaurar estado online
        repository.alternarModoOfflineDemo()
        assertTrue(repository.isNetworkConnected.value)
    }

    @Test
    fun catalogoEquipos_incluyeCuatroEquiposCriticosDeLineaBaseExigidos() {
        val catalogo = repository.obtenerEquiposDefault()
        assertEquals(9, catalogo.size)

        val nombres = catalogo.map { it.nombre.lowercase() }
        assertTrue(nombres.any { it.contains("maleta de suministro de aire") })
        assertTrue(nombres.any { it.contains("filtros de compresor") })
        assertTrue(nombres.any { it.contains("aletas") })
        assertTrue(nombres.any { it.contains("census") })

        val faena = faenaDePruebaEquipos()
        val revisiones = repository.revisionesCombinadas(faena, faena.buzoId, EtapaComprobacion.PRE_EQUIPACION)
        val criticos = revisiones.filter { it.esEquipoCriticoLineaBase }
        assertEquals(4, criticos.size)
        assertEquals(
            setOf("MALETA_AIRE", "FILTROS_COMPRESOR", "ALETAS", "CENSUS"),
            criticos.map { it.codigoTipoEquipoBackend }.toSet()
        )
        assertTrue(criticos.all { it.fotoLineaBaseRef.startsWith("LB-") })
    }

    @Test
    fun rolOperador_noPuedeAprobarNiCerrarFaena_soloSupervisor() {
        val faena = faenaDePruebaEquipos().copy(id = "F-ROL-TEST")
        repository.autorizarFaena(faena)

        // Iniciar sesión con el segundo perfil de prueba (Rol Operador)
        val loginOperador = repository.login("18.765.432-K", "operador123")
        assertTrue(loginOperador.isSuccess)
        assertFalse(loginOperador.getOrNull()?.esSupervisor ?: true)

        val intentoAprobar = repository.aprobarDatosFaena("F-ROL-TEST")
        assertTrue(intentoAprobar.isFailure)
        assertTrue(intentoAprobar.exceptionOrNull()?.message?.contains("Restricción de Rol") == true)

        val intentoCerrar = repository.completarFaena("F-ROL-TEST")
        assertTrue(intentoCerrar.isFailure)
        assertTrue(intentoCerrar.exceptionOrNull()?.message?.contains("Restricción de Rol") == true)

        // Volver a iniciar sesión como Supervisor
        val loginSupervisor = repository.login("12.345.678-5", "admin123")
        assertTrue(loginSupervisor.isSuccess)
        assertTrue(loginSupervisor.getOrNull()?.esSupervisor == true)
        assertTrue(repository.aprobarDatosFaena("F-ROL-TEST").isSuccess)
    }

    private fun faenaDePruebaEquipos() = RegistroFaena(
        id = "F-TEST-EQUIPOS",
        buzoId = "1",
        buzoNombre = "Carlos Soto Paredes",
        socorristaId = "2",
        socorristaNombre = "Pedro Almonacid Vera",
        empresa = "AquaTest",
        centroCultivo = "Centro de prueba",
        tipoFaena = "Inspección",
        profundidadMetros = 10,
        tiempoMinutos = 20,
        fechaHora = "22/11/2026 10:00"
    )
}
