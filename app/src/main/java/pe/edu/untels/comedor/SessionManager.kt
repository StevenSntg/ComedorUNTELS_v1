package pe.edu.untels.comedor

import android.content.Context
import androidx.core.content.edit
import pe.edu.untels.comedor.data.model.Student

class SessionManager(context: Context) {

    private val preferences =
        context.getSharedPreferences("comedor_session", Context.MODE_PRIVATE)

    fun persistSession(student: Student) {
        preferences.edit {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_STUDENT_ID, student.id)
            putString(KEY_STUDENT_CODE, student.code)
            putString(KEY_STUDENT_NAME, student.fullName)
            putString(KEY_STUDENT_CAREER, student.career)
        }
    }

    fun isLoggedIn(): Boolean =
        preferences.getBoolean(KEY_IS_LOGGED_IN, false) &&
            preferences.contains(KEY_STUDENT_ID)

    fun getActiveProfile(): UserProfile? {
        if (!isLoggedIn()) return null
        val id = preferences.getLong(KEY_STUDENT_ID, -1L)
        if (id == -1L) return null
        val code = preferences.getString(KEY_STUDENT_CODE, null) ?: return null
        val name = preferences.getString(KEY_STUDENT_NAME, null) ?: return null
        val career = preferences.getString(KEY_STUDENT_CAREER, null) ?: return null
        return UserProfile(id = id, code = code, displayName = name, career = career)
    }

    fun logout() {
        preferences.edit {
            clear()
        }
    }

    data class UserProfile(
        val id: Long,
        val code: String,
        val displayName: String,
        val career: String,
    )

    private companion object {
        const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        const val KEY_STUDENT_ID = "key_student_id"
        const val KEY_STUDENT_CODE = "key_student_code"
        const val KEY_STUDENT_NAME = "key_student_name"
        const val KEY_STUDENT_CAREER = "key_student_career"
    }
}
