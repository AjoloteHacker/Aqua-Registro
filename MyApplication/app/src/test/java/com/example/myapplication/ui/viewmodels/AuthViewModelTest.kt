package com.example.myapplication.ui.viewmodels

import com.example.myapplication.data.model.RolUsuario
import com.example.myapplication.data.repository.AquaRepository
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
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: AuthViewModel
    private lateinit var repository: AquaRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AquaRepository.getInstance()
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectTab_actualizaTab() {
        viewModel.selectTab(1)
        assertEquals(1, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun onRutLoginChange_formateaRut() {
        viewModel.onRutLoginChange("123456785")
        assertTrue(viewModel.uiState.value.rutLogin.contains("-"))

        // Correo sin formatear
        viewModel.onRutLoginChange("admin@aquachile.cl")
        assertEquals("admin@aquachile.cl", viewModel.uiState.value.rutLogin)
    }

    @Test
    fun onPasswordLoginChange_actualizaPassword() {
        viewModel.onPasswordLoginChange("nuevaPass1")
        assertEquals("nuevaPass1", viewModel.uiState.value.passwordLogin)
    }

    @Test
    fun togglePasswordLoginVisibility_invierteVisibilidad() {
        val inicial = viewModel.uiState.value.passwordLoginVisible
        viewModel.togglePasswordLoginVisibility()
        assertEquals(!inicial, viewModel.uiState.value.passwordLoginVisible)
    }

    @Test
    fun camposRegistro_actualizanEstado() {
        viewModel.onRegNombreChange("Ana Torres")
        assertEquals("Ana Torres", viewModel.uiState.value.regNombre)

        viewModel.onRegRutChange("16.320.108-9")
        assertEquals("16.320.108-9", viewModel.uiState.value.regRut)

        viewModel.onRegSeremiChange("PR-8888-K")
        assertEquals("PR-8888-K", viewModel.uiState.value.regSeremi)

        viewModel.onRegCorreoChange("ana@aquachile.cl")
        assertEquals("ana@aquachile.cl", viewModel.uiState.value.regCorreo)

        viewModel.onRegCentroChange("Centro Melinka 1")
        assertEquals("Centro Melinka 1", viewModel.uiState.value.regCentro)

        viewModel.onRegPasswordChange("Pass1234")
        assertEquals("Pass1234", viewModel.uiState.value.regPassword)

        viewModel.onRegPasswordConfirmChange("Pass1234")
        assertEquals("Pass1234", viewModel.uiState.value.regPasswordConfirm)

        viewModel.toggleRegPasswordVisibility()
        assertTrue(viewModel.uiState.value.regPasswordVisible)

        viewModel.onRegRolChange(RolUsuario.OPERADOR_BUCEO)
        assertEquals(RolUsuario.OPERADOR_BUCEO, viewModel.uiState.value.regRol)

        viewModel.onRegAceptaTerminosChange(false)
        assertFalse(viewModel.uiState.value.regAceptaTerminos)
    }

    @Test
    fun login_exitosoEInvalido() {
        // Fallido
        viewModel.onRutLoginChange("12.345.678-5")
        viewModel.onPasswordLoginChange("claveIncorrecta")
        viewModel.login {}
        assertFalse(viewModel.uiState.value.isLoginSuccessful)
        assertNotNull(viewModel.uiState.value.errorMessage)

        // Exitoso
        viewModel.onPasswordLoginChange("admin123")
        viewModel.login {}
        assertTrue(viewModel.uiState.value.isLoginSuccessful)
        assertNotNull(viewModel.uiState.value.usuarioAutenticado)
    }

    @Test
    fun registrar_validaciones() {
        // 1. Nombre inválido
        viewModel.onRegNombreChange("")
        viewModel.registrar {}
        assertNotNull(viewModel.uiState.value.errorMessage)

        // 2. RUT inválido
        viewModel.onRegNombreChange("Ana Torres")
        viewModel.onRegRutChange("123")
        viewModel.registrar {}
        assertNotNull(viewModel.uiState.value.errorMessage)

        // 3. RUT duplicado
        viewModel.onRegRutChange("12.345.678-5")
        viewModel.registrar {}
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("Ya existe"))

        // 4. SEREMI inválido
        viewModel.onRegRutChange("15.489.231-1")
        viewModel.onRegSeremiChange("INVALIDO")
        viewModel.registrar {}
        assertNotNull(viewModel.uiState.value.errorMessage)

        // 5. Correo inválido
        viewModel.onRegSeremiChange("PR-12345-X")
        viewModel.onRegCorreoChange("correo_invalido")
        viewModel.registrar {}
        assertNotNull(viewModel.uiState.value.errorMessage)

        // 6. Correo duplicado
        viewModel.onRegCorreoChange("c.loncon@aquachile.com")
        viewModel.registrar {}
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("Ya existe"))

        // 7. Password inválida
        viewModel.onRegCorreoChange("ana.torres@aquachile.com")
        viewModel.onRegPasswordChange("123")
        viewModel.registrar {}
        assertNotNull(viewModel.uiState.value.errorMessage)

        // 8. Confirmación contraseña no coincide
        viewModel.onRegPasswordChange("Password123")
        viewModel.onRegPasswordConfirmChange("OtraPass999")
        viewModel.registrar {}
        assertNotNull(viewModel.uiState.value.errorMessage)

        // 9. Términos no aceptados
        viewModel.onRegPasswordConfirmChange("Password123")
        viewModel.onRegAceptaTerminosChange(false)
        viewModel.registrar {}
        assertNotNull(viewModel.uiState.value.errorMessage)

        // 10. Registro Exitoso
        viewModel.onRegAceptaTerminosChange(true)
        var registradoExitosamente = false
        viewModel.registrar {
            registradoExitosamente = true
        }
        assertTrue(registradoExitosamente)
        assertTrue(viewModel.uiState.value.isLoginSuccessful)
        assertNotNull(viewModel.uiState.value.successDialogMessage)
        assertNull(viewModel.uiState.value.errorMessage)

        // Limpiar mensajes
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successDialogMessage)

        viewModel.cargarPerfilDemo("12.345.678-5", "admin123")
        assertEquals("12.345.678-5", viewModel.uiState.value.rutLogin)
    }
}
