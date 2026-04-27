package com.textify.app.ui.screens.auth

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.textify.app.data.local.entity.UsuarioEntity
import com.textify.app.data.remote.api.LoginRequest
import com.textify.app.data.remote.api.RegisterRequest
import com.textify.app.ai.models.AiApiClient
import com.textify.app.utils.Constants
import com.textify.app.ui.screens.profile.VoiceGender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val error: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val db = (application as com.textify.app.TextifyApp).database
    private val prefs = application.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = AiApiClient.textifyApi.login(LoginRequest(correo = email, contrasena = password))
                if (response.isSuccessful) {
                    val authResponse = response.body()!!
                    // En el login, intentamos recuperar el género si el servidor lo devolviera, 
                    // si no, mantenemos el actual o usamos uno por defecto.
                    saveSession(authResponse.token, authResponse.userId, authResponse.nombre, email, password, null)
                    _authState.value = AuthState.Success("Bienvenido")
                    onSuccess()
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _authState.value = AuthState.Error(errorMsg ?: "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error de conexión: ${e.localizedMessage}")
                Log.e("Auth", "Login failed", e)
            }
        }
    }

    fun register(name: String, email: String, password: String, gender: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = AiApiClient.textifyApi.register(RegisterRequest(nombre = name, correo = email, contrasena = password))
                if (response.isSuccessful) {
                    val authResponse = response.body()!!
                    saveSession(authResponse.token, authResponse.userId, authResponse.nombre, email, password, gender)
                    _authState.value = AuthState.Success("Cuenta creada")
                    onSuccess()
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _authState.value = AuthState.Error(errorMsg ?: "Error del servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Fallo de red: ${e.localizedMessage}")
                Log.e("Auth", "Registration failed", e)
            }
        }
    }

    private fun parseError(errorBody: String?): String? {
        return try {
            errorBody?.let { JSONObject(it).getString("error") }
        } catch (e: Exception) { null }
    }

    private suspend fun saveSession(token: String, userId: String, name: String, email: String, password: String, gender: String?) {
        val editor = prefs.edit().apply {
            putString(Constants.KEY_USER_TOKEN, token)
            putString(Constants.KEY_USER_ID, userId)
            putBoolean(Constants.KEY_IS_LOGGED_IN, true)
        }

        // Asignación de voz por defecto según el género
        if (gender != null) {
            val voiceGender = if (gender == VoiceGender.MALE.name) VoiceGender.MALE else VoiceGender.FEMALE
            val defaultVoiceId = if (voiceGender == VoiceGender.MALE) Constants.VOICE_ID_ADAM else Constants.VOICE_ID_SARAH
            
            editor.putString(Constants.KEY_VOICE_GENDER, voiceGender.name)
            editor.putString(Constants.KEY_SELECTED_VOICE_ID, defaultVoiceId)
        }
        
        editor.apply()

        db.usuarioDao().clearUsuarios()
        db.usuarioDao().insertUsuario(
            UsuarioEntity(id = userId, nombre = name, correo = email, contrasena = password)
        )
    }
}
