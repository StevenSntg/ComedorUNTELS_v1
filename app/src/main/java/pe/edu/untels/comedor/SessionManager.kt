package pe.edu.untels.comedor

import android.content.Context
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val preferences =
        context.getSharedPreferences("comedor_session", Context.MODE_PRIVATE)

    private val demoUsers = listOf(
        UserCredential(
            username = "estudiante",
            password = "comedor2024",
            displayName = "Valeria Mendoza",
            program = "Ing. de Sistemas · C0021345",
            role = "Fanática del menú criollo"
        ),
        UserCredential(
            username = "nutri",
            password = "nutricion+",
            displayName = "Equipo de Nutrición",
            program = "Bienestar Estudiantil UNTELS",
            role = "Guía nutricional del comedor"
        ),
        UserCredential(
            username = "admin",
            password = "gestion123",
            displayName = "Gestión del Comedor",
            program = "Administración de Servicios",
            role = "Coordinación operativa"
        )
    )

    fun authenticate(username: String, password: String): UserCredential? {
        return demoUsers.firstOrNull { credential ->
            credential.username.equals(username, ignoreCase = true) &&
                credential.password == password
        }
    }

    fun persistSession(credential: UserCredential) {
        preferences.edit {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, credential.username)
            putString(KEY_DISPLAY_NAME, credential.displayName)
            putString(KEY_PROGRAM, credential.program)
            putString(KEY_ROLE, credential.role)
        }
    }

    fun isLoggedIn(): Boolean = preferences.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getActiveProfile(): UserProfile? {
        if (!preferences.contains(KEY_USERNAME)) return null
        val displayName = preferences.getString(KEY_DISPLAY_NAME, null) ?: return null
        val program = preferences.getString(KEY_PROGRAM, null) ?: return null
        val role = preferences.getString(KEY_ROLE, null) ?: return null

        return UserProfile(
            displayName = displayName,
            program = program,
            role = role
        )
    }

    fun logout() {
        preferences.edit {
            clear()
        }
    }

    data class UserCredential(
        val username: String,
        val password: String,
        val displayName: String,
        val program: String,
        val role: String
    )

    data class UserProfile(
        val displayName: String,
        val program: String,
        val role: String
    )

    private companion object {
        const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        const val KEY_USERNAME = "key_username"
        const val KEY_DISPLAY_NAME = "key_display_name"
        const val KEY_PROGRAM = "key_program"
        const val KEY_ROLE = "key_role"
    }
}
