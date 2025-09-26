package pe.edu.untels.comedor.data.model

data class Reservation(
    val studentId: Long,
    val dayIndex: Int,
    val createdAtMillis: Long,
)
