package com.boxcontairner.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxcontairner.data.repository.AuthRepository
import com.boxcontairner.domain.model.Role
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Authenticated : AuthState()
    data object PendingApproval : AuthState()
    data class LockedOut(val remainingSeconds: Long) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentRole: StateFlow<Role> = authRepository.currentRole

    init {
        if (authRepository.isUserLoggedIn()) {
            _authState.value = AuthState.Authenticated
            viewModelScope.launch { authRepository.refreshCurrentRole() }
        }
    }

    fun login(nick: String, pin: String) {
        if (nick.isBlank() || pin.length != 6) {
            _authState.value = AuthState.Error("Nick y PIN de 6 dígitos son obligatorios")
            return
        }
        authRepository.checkLockout()?.let { ms ->
            _authState.value = AuthState.LockedOut(ms / 1000)
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            runCatching { authRepository.login(nick, pin) }
                .onSuccess { isActive ->
                    _authState.value = if (isActive) AuthState.Authenticated
                    else AuthState.Error("Cuenta inactiva. Espera aprobación del administrador.")
                }
                .onFailure { e ->
                    Timber.w(e, "Login falló nick=$nick")
                    _authState.value = AuthState.Error(friendlyAuthError(e))
                }
        }
    }

    fun register(nick: String, pin: String) {
        if (nick.isBlank() || pin.length != 6) {
            _authState.value = AuthState.Error("El PIN debe tener exactamente 6 dígitos")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            runCatching { authRepository.register(nick, pin) }
                .onSuccess { isFirstUser ->
                    _authState.value = if (isFirstUser) AuthState.Authenticated
                    else AuthState.PendingApproval
                }
                .onFailure { e ->
                    Timber.w(e, "Registro falló nick=$nick")
                    _authState.value = AuthState.Error(friendlyAuthError(e))
                }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = AuthState.Idle
    }

    private fun friendlyAuthError(e: Throwable): String {
        if (e is FirebaseAuthException) {
            return when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Ese usuario ya existe"
                "ERROR_WRONG_PASSWORD",
                "ERROR_INVALID_CREDENTIAL" -> "Usuario o PIN incorrectos"
                "ERROR_USER_NOT_FOUND" -> "Usuario no registrado"
                "ERROR_NETWORK_REQUEST_FAILED" -> "Sin conexión a internet"
                "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Esperá unos minutos."
                "ERROR_OPERATION_NOT_ALLOWED" ->
                    "Auth email/PIN no está habilitado en la consola Firebase"
                else -> "Error de autenticación [${e.errorCode}]"
            }
        }
        return e.message ?: "Error desconocido"
    }
}
