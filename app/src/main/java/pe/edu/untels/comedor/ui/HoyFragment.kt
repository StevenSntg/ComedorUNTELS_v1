package pe.edu.untels.comedor.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.snackbar.Snackbar
import pe.edu.untels.comedor.databinding.FragmentHoyBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.random.Random

class HoyFragment : Fragment() {

    private var _binding: FragmentHoyBinding? = null
    private val binding get() = _binding!!

    private var timer: CountDownTimer? = null
    private var currentMealType: MealType = MealType.ALMUERZO

    enum class MealType { DESAYUNO, ALMUERZO, CENA }

    data class Meal(
        val titulo: String,
        val detalle: String,
        val extra: String,
        val kcal: Int,
        val prot: Double,
        val lip: Double,
        val carb: Double,
        val color: String = "#6366F1", // Color principal del plato
        val tags: List<String> = emptyList(),
        val rating: Float = 4.5f,
        val popularityScore: Int = 0
    )

    // Datos mejorados con más información nutricional
    private val desayuno = Meal(
        titulo = "Desayuno Energético",
        detalle = "Siete semillas c/ avena + Pan integral c/ palta + Huevos revueltos",
        extra = "Jugo natural de naranja • Fruta de estación",
        kcal = 481, prot = 18.5, lip = 16.2, carb = 72.8,
        color = "#F59E0B",
        tags = listOf("🌅 Matutino", "🥑 Saludable", "💪 Proteínas"),
        rating = 4.3f,
        popularityScore = 85
    )

    private val almuerzo = Meal(
        titulo = "Almuerzo Completo",
        detalle = "Sopa de quinua + Lomo saltado con arroz + Ensalada fresca",
        extra = "Chicha morada • Fruta: Papaya",
        kcal = 1187, prot = 45.2, lip = 32.1, carb = 145.6,
        color = "#EF4444",
        tags = listOf("🔥 Energético", "🥩 Proteínas", "🍚 Peruano"),
        rating = 4.8f,
        popularityScore = 95
    )

    private val cena = Meal(
        titulo = "Cena Ligera",
        detalle = "Crema de verduras + Pescado a la plancha + Puré de camote",
        extra = "Infusión de manzanilla • Macedonia de frutas",
        kcal = 650, prot = 28.9, lip = 22.4, carb = 78.3,
        color = "#8B5CF6",
        tags = listOf("🌙 Nocturna", "🐟 Omega-3", "🥬 Light"),
        rating = 4.6f,
        popularityScore = 78
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHoyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupAnimations()
        setupCountdown()
        setupRandomCupos()
    }

    private fun setupUI() {
        // Configurar fecha con mejor formato
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "PE"))
        val today = sdf.format(Calendar.getInstance().time)
        binding.tvDia.text = today.replaceFirstChar { it.uppercase() }

        // Estado inicial: Almuerzo (selección inteligente por hora)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            currentHour < 10 -> {
                binding.cgComidas.check(binding.chipDesayuno.id)
                currentMealType = MealType.DESAYUNO
                renderMeal(desayuno)
            }
            currentHour < 16 -> {
                binding.cgComidas.check(binding.chipAlmuerzo.id)
                currentMealType = MealType.ALMUERZO
                renderMeal(almuerzo)
            }
            else -> {
                binding.cgComidas.check(binding.chipCena.id)
                currentMealType = MealType.CENA
                renderMeal(cena)
            }
        }

        binding.cgComidas.setOnCheckedStateChangeListener { _, ids ->
            when (ids.firstOrNull()) {
                binding.chipDesayuno.id -> {
                    currentMealType = MealType.DESAYUNO
                    renderMeal(desayuno)
                    animateCardTransition()
                }
                binding.chipAlmuerzo.id -> {
                    currentMealType = MealType.ALMUERZO
                    renderMeal(almuerzo)
                    animateCardTransition()
                }
                binding.chipCena.id -> {
                    currentMealType = MealType.CENA
                    renderMeal(cena)
                    animateCardTransition()
                }
            }
        }

        // Acciones mejoradas
        binding.btnReservar.setOnClickListener {
            animateButtonPress(it)
            showModernSnackbar("✅ ¡Reserva confirmada!", "#10B981")
            updateCuposAfterReservation()
        }

        binding.btnCompartir.setOnClickListener {
            animateButtonPress(it)
            showModernSnackbar("📤 Compartido en redes sociales", "#3B82F6")
        }

        binding.btnVerSemana.setOnClickListener {
            animateButtonPress(it)
            showModernSnackbar("📅 Navegando al menú semanal...", "#8B5CF6")
        }

        // Configurar rating clickeable
        binding.layoutRating.setOnClickListener {
            animateRating()
            showModernSnackbar("⭐ Gracias por tu valoración", "#F59E0B")
        }
    }

    private fun renderMeal(meal: Meal) {
        binding.tvTituloComida.text = meal.titulo
        binding.tvNombrePlatoHeader.text = "${meal.titulo} • ${meal.detalle}"
        binding.tvNombrePlato.text = meal.detalle
        binding.tvDescripcion.text = meal.extra

        // Valores nutricionales
        binding.tvKcal.text = meal.kcal.toString()
        binding.tvProt.text = "%.1f".format(meal.prot)
        binding.tvLip.text = "%.2f".format(meal.lip)
        binding.tvCarb.text = "%.1f".format(meal.carb)

        // Tags dinámicos
        updateTags(meal.tags)

        // Rating
        binding.tvRating.text = meal.rating.toString()
        updateRatingStars(meal.rating)

        // Barra de popularidad
        animatePopularityBar(meal.popularityScore)

        // Color accent del plato
        try {
            val color = Color.parseColor(meal.color)
            binding.viewColorAccent.setBackgroundColor(color)
        } catch (e: IllegalArgumentException) {
            // Color por defecto si hay error
            binding.viewColorAccent.setBackgroundColor(Color.parseColor("#6366F1"))
        }
    }

    private fun updateTags(tags: List<String>) {
        binding.tag1.text = tags.getOrElse(0) { "🍽️ Delicioso" }
        binding.tag2.text = tags.getOrElse(1) { "⚡ Nutritivo" }

        // Mostrar tercer tag si existe
        if (tags.size > 2) {
            binding.tag3.visibility = View.VISIBLE
            binding.tag3.text = tags[2]
        } else {
            binding.tag3.visibility = View.GONE
        }
    }

    private fun updateRatingStars(rating: Float) {
        val fullStars = rating.toInt()
        val hasHalfStar = (rating - fullStars) >= 0.5f

        // Aquí podrías actualizar las estrellas visuales si las tienes en el XML
        binding.tvPopularity.text = "Popular entre estudiantes (${fullStars}${if (hasHalfStar) ".5" else ""}/5)"
    }

    private fun animatePopularityBar(score: Int) {
        val animator = ValueAnimator.ofInt(0, score)
        animator.duration = 1500
        animator.interpolator = FastOutSlowInInterpolator()
        animator.addUpdateListener { valueAnimator ->
            val progress = valueAnimator.animatedValue as Int
            binding.progPopularidad.progress = progress
        }
        animator.start()
    }

    private fun setupAnimations() {
        // Animación de entrada para las cards
        binding.cardPrincipal.alpha = 0f
        binding.cardPrincipal.translationY = 50f

        binding.cardReserva.alpha = 0f
        binding.cardReserva.translationY = 100f

        binding.cardSostenibilidad.alpha = 0f
        binding.cardSostenibilidad.translationY = 150f

        // Animar entrada escalonada
        listOf(binding.cardPrincipal, binding.cardReserva, binding.cardSostenibilidad)
            .forEachIndexed { index, card ->
                card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay((index * 200).toLong())
                    .setInterpolator(FastOutSlowInInterpolator())
                    .start()
            }
    }

    private fun animateCardTransition() {
        val card = binding.cardPrincipal
        val scaleDown = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.95f)
        val scaleUp = ObjectAnimator.ofFloat(card, "scaleX", 0.95f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.95f)
        val scaleUpY = ObjectAnimator.ofFloat(card, "scaleY", 0.95f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.play(scaleDown).with(scaleDownY)
        animatorSet.play(scaleUp).with(scaleUpY).after(scaleDown)
        animatorSet.duration = 150
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    private fun animateButtonPress(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 200
        animatorSet.interpolator = BounceInterpolator()
        animatorSet.start()
    }

    private fun animateRating() {
        val pulse = ObjectAnimator.ofFloat(binding.layoutRating, "scaleX", 1f, 1.1f, 1f)
        val pulseY = ObjectAnimator.ofFloat(binding.layoutRating, "scaleY", 1f, 1.1f, 1f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(pulse, pulseY)
        animatorSet.duration = 300
        animatorSet.start()
    }

    private fun setupCountdown() {
        val targetHour = when (currentMealType) {
            MealType.DESAYUNO -> 7 to 30  // 7:30 AM
            MealType.ALMUERZO -> 12 to 0  // 12:00 PM
            MealType.CENA -> 18 to 30     // 6:30 PM
        }
        startCountdownTo(targetHour.first, targetHour.second)
    }

    private fun startCountdownTo(hour24: Int, minute: Int) {
        timer?.cancel()

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }

        val millis = max(0L, target.timeInMillis - now.timeInMillis)
        timer = object : CountDownTimer(millis, 1000) {
            override fun onTick(ms: Long) {
                val h = ms / (1000 * 60 * 60)
                val m = (ms / (1000 * 60)) % 60
                val s = (ms / 1000) % 60

                binding.tvCountdown.text = when {
                    h > 0 -> "⏰ Abre en: %02d:%02d:%02d".format(h, m, s)
                    m > 0 -> "⏰ Abre en: %02d:%02d".format(m, s)
                    else -> "⏰ ¡Solo %02d segundos!".format(s)
                }

                // Cambiar color cuando queden pocos minutos
                if (h == 0L && m < 5) {
                    binding.tvCountdown.setTextColor(Color.parseColor("#EF4444"))
                }
            }

            override fun onFinish() {
                binding.tvCountdown.text = "🎉 ¡Reservas abiertas!"
                binding.tvCountdown.setTextColor(Color.parseColor("#10B981"))
                binding.btnReservar.isEnabled = true
            }
        }.start()
    }

    private fun setupRandomCupos() {
        val maxCupos = 120
        val currentCupos = Random.nextInt(20, 100)

        binding.progCupos.max = maxCupos
        binding.tvCupos.text = "Cupos disponibles: $currentCupos de $maxCupos"

        // Animación de la barra de progreso
        val animator = ValueAnimator.ofInt(0, currentCupos)
        animator.duration = 1000
        animator.interpolator = FastOutSlowInInterpolator()
        animator.addUpdateListener { valueAnimator ->
            binding.progCupos.progress = valueAnimator.animatedValue as Int
        }
        animator.start()
    }

    private fun updateCuposAfterReservation() {
        val currentProgress = binding.progCupos.progress
        if (currentProgress > 0) {
            val newProgress = currentProgress - 1
            binding.progCupos.progress = newProgress
            binding.tvCupos.text = "Cupos disponibles: $newProgress de ${binding.progCupos.max}"
        }
    }

    private fun showModernSnackbar(message: String, colorHex: String) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        try {
            val color = Color.parseColor(colorHex)
            snackbar.setBackgroundTint(color)
        } catch (e: IllegalArgumentException) {
            // Color por defecto
        }
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}