package pe.edu.untels.comedor.data

import android.content.Context
import pe.edu.untels.comedor.data.model.MenuItem
import pe.edu.untels.comedor.data.model.Reservation
import pe.edu.untels.comedor.data.model.Student
import java.util.Calendar
import java.util.Locale

class CafeteriaRepository private constructor(context: Context) {

    private val database = CafeteriaDatabase.getInstance(context)

    fun authenticateStudent(code: String, password: String): Student? =
        database.findStudent(code.trim(), password.trim())

    fun getStudentById(id: Long): Student? = database.getStudentById(id)

    fun getMenuForDay(dayIndex: Int): MenuItem? = database.getMenuForDay(dayIndex)

    fun getWeeklyMenu(): List<MenuItem> = database.getWeeklyMenu()

    fun reserveSlot(studentId: Long, dayIndex: Int): ReservationResult {
        val alreadyReserved = database.getReservation(studentId, dayIndex)
        if (alreadyReserved != null) {
            return ReservationResult.AlreadyReserved(alreadyReserved)
        }
        val reserved = database.reserveSlot(studentId, dayIndex)
        return if (reserved) {
            val reservation = database.getReservation(studentId, dayIndex)
            ReservationResult.Success(reservation!!)
        } else {
            ReservationResult.Error
        }
    }

    fun getReservationForDay(studentId: Long, dayIndex: Int): Reservation? =
        database.getReservation(studentId, dayIndex)

    fun getLatestReservation(studentId: Long): Reservation? =
        database.getLatestReservation(studentId)

    fun getTodayMenuIndex(): Int {
        val calendar = Calendar.getInstance(Locale.getDefault())
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Calendar: Sunday=1 ... Saturday=7; we only have Monday-Friday (1-5)
        return when (dayOfWeek) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 5
            else -> 1
        }
    }

    sealed interface ReservationResult {
        data class Success(val reservation: Reservation) : ReservationResult
        data class AlreadyReserved(val reservation: Reservation) : ReservationResult
        data object Error : ReservationResult
    }

    companion object {
        @Volatile
        private var instance: CafeteriaRepository? = null

        fun getInstance(context: Context): CafeteriaRepository =
            instance ?: synchronized(this) {
                instance ?: CafeteriaRepository(context.applicationContext).also { instance = it }
            }
    }
}
