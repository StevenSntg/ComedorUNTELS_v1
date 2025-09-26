package pe.edu.untels.comedor.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import pe.edu.untels.comedor.data.model.MenuItem
import pe.edu.untels.comedor.data.model.Reservation
import pe.edu.untels.comedor.data.model.Student
import java.util.Locale

class CafeteriaDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE students (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL,
                career TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE menus (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                day_index INTEGER NOT NULL,
                day_name TEXT NOT NULL,
                soup TEXT NOT NULL,
                main_course TEXT NOT NULL,
                dessert TEXT NOT NULL,
                beverage TEXT NOT NULL,
                calories INTEGER NOT NULL,
                highlight TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE reservations (
                student_id INTEGER NOT NULL,
                day_index INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                PRIMARY KEY (student_id, day_index),
                FOREIGN KEY (student_id) REFERENCES students(id)
            )
            """.trimIndent()
        )

        insertSeedStudents(db)
        insertSeedMenus(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS reservations")
        db.execSQL("DROP TABLE IF EXISTS menus")
        db.execSQL("DROP TABLE IF EXISTS students")
        onCreate(db)
    }

    fun findStudent(code: String, password: String): Student? {
        val sql =
            "SELECT id, code, full_name, career FROM students WHERE LOWER(code) = ? AND password = ?"
        readableDatabase.rawQuery(sql, arrayOf(code.lowercase(Locale.ROOT), password)).use { cursor ->
            return if (cursor.moveToFirst()) {
                Student(
                    id = cursor.getLong(0),
                    code = cursor.getString(1),
                    fullName = cursor.getString(2),
                    career = cursor.getString(3),
                )
            } else {
                null
            }
        }
    }

    fun getStudentById(id: Long): Student? {
        val sql = "SELECT id, code, full_name, career FROM students WHERE id = ?"
        readableDatabase.rawQuery(sql, arrayOf(id.toString())).use { cursor ->
            return if (cursor.moveToFirst()) {
                Student(
                    id = cursor.getLong(0),
                    code = cursor.getString(1),
                    fullName = cursor.getString(2),
                    career = cursor.getString(3),
                )
            } else {
                null
            }
        }
    }

    fun getMenuForDay(dayIndex: Int): MenuItem? {
        val sql = "SELECT id, day_index, day_name, soup, main_course, dessert, beverage, calories, highlight FROM menus WHERE day_index = ?"
        readableDatabase.rawQuery(sql, arrayOf(dayIndex.toString())).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toMenuItem() else null
        }
    }

    fun getWeeklyMenu(): List<MenuItem> {
        val sql =
            "SELECT id, day_index, day_name, soup, main_course, dessert, beverage, calories, highlight FROM menus ORDER BY day_index"
        readableDatabase.rawQuery(sql, emptyArray()).use { cursor ->
            val items = mutableListOf<MenuItem>()
            while (cursor.moveToNext()) {
                items.add(cursor.toMenuItem())
            }
            return items
        }
    }

    fun reserveSlot(studentId: Long, dayIndex: Int): Boolean {
        val values = ContentValues().apply {
            put("student_id", studentId)
            put("day_index", dayIndex)
            put("created_at", System.currentTimeMillis())
        }
        val result = writableDatabase.insertWithOnConflict(
            "reservations",
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE,
        )
        return result != -1L
    }

    fun getReservation(studentId: Long, dayIndex: Int): Reservation? {
        val sql =
            "SELECT student_id, day_index, created_at FROM reservations WHERE student_id = ? AND day_index = ?"
        readableDatabase.rawQuery(sql, arrayOf(studentId.toString(), dayIndex.toString())).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toReservation() else null
        }
    }

    fun getLatestReservation(studentId: Long): Reservation? {
        val sql =
            "SELECT student_id, day_index, created_at FROM reservations WHERE student_id = ? ORDER BY created_at DESC LIMIT 1"
        readableDatabase.rawQuery(sql, arrayOf(studentId.toString())).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toReservation() else null
        }
    }

    private fun Cursor.toMenuItem(): MenuItem =
        MenuItem(
            id = getLong(0),
            dayIndex = getInt(1),
            dayName = getString(2),
            soup = getString(3),
            mainCourse = getString(4),
            dessert = getString(5),
            beverage = getString(6),
            calories = getInt(7),
            highlight = getString(8),
        )

    private fun Cursor.toReservation(): Reservation =
        Reservation(
            studentId = getLong(0),
            dayIndex = getInt(1),
            createdAtMillis = getLong(2),
        )

    private fun insertSeedStudents(db: SQLiteDatabase) {
        val students = listOf(
            Triple("20210001", "comedor2024", "Valeria Mendoza"),
            Triple("20200452", "energia2024", "Diego Salazar"),
            Triple("20190531", "nutriuntels", "María Flores"),
        )
        val careers = listOf(
            "Ingeniería de Sistemas",
            "Ingeniería Ambiental",
            "Ingeniería Electrónica",
        )
        students.forEachIndexed { index, (code, password, name) ->
            val values = ContentValues().apply {
                put("code", code)
                put("password", password)
                put("full_name", name)
                put("career", careers[index % careers.size])
            }
            db.insert("students", null, values)
        }
    }

    private fun insertSeedMenus(db: SQLiteDatabase) {
        val menuData = listOf(
            MenuSeed(1, "Lunes", "Crema de zapallo", "Seco de res con frijoles y arroz", "Ensalada de frutas", "Emoliente de quinua", 850, "Inicio con energía"),
            MenuSeed(2, "Martes", "Sopa criolla", "Ají de gallina con papa dorada", "Gelatina de fresa", "Refresco de maracuyá", 780, "Clásico favorito"),
            MenuSeed(3, "Miércoles", "Ensalada fresca", "Pollo al horno con puré de camote", "Yogurt natural con granola", "Agua de piña", 720, "Balance perfecto"),
            MenuSeed(4, "Jueves", "Sopa de quinua", "Pescado en salsa de limón con arroz integral", "Mazamorra morada", "Chicha morada", 695, "Omega para el cerebro"),
            MenuSeed(5, "Viernes", "Crema de espinaca", "Lomo saltado con tacu tacu", "Mousse de maracuyá", "Refresco de hierba luisa", 890, "Viernes criollo"),
        )
        menuData.forEach { item ->
            val values = ContentValues().apply {
                put("day_index", item.dayIndex)
                put("day_name", item.dayName)
                put("soup", item.soup)
                put("main_course", item.mainCourse)
                put("dessert", item.dessert)
                put("beverage", item.beverage)
                put("calories", item.calories)
                put("highlight", item.highlight)
            }
            db.insert("menus", null, values)
        }
    }

    private data class MenuSeed(
        val dayIndex: Int,
        val dayName: String,
        val soup: String,
        val mainCourse: String,
        val dessert: String,
        val beverage: String,
        val calories: Int,
        val highlight: String,
    )

    companion object {
        private const val DATABASE_NAME = "comedor_untels.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var instance: CafeteriaDatabase? = null

        fun getInstance(context: Context): CafeteriaDatabase =
            instance ?: synchronized(this) {
                instance ?: CafeteriaDatabase(context.applicationContext).also { instance = it }
            }
    }
}
