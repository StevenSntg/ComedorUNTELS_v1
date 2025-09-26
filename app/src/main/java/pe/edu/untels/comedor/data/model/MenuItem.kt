package pe.edu.untels.comedor.data.model

data class MenuItem(
    val id: Long,
    val dayIndex: Int,
    val dayName: String,
    val soup: String,
    val mainCourse: String,
    val dessert: String,
    val beverage: String,
    val calories: Int,
    val highlight: String,
)
