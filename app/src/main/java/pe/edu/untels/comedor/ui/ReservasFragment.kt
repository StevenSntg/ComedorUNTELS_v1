package pe.edu.untels.comedor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.edu.untels.comedor.R
import pe.edu.untels.comedor.SessionManager
import pe.edu.untels.comedor.data.CafeteriaRepository
import pe.edu.untels.comedor.databinding.FragmentReservasBinding

class ReservasFragment : Fragment() {

    private var _binding: FragmentReservasBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val repository by lazy { CafeteriaRepository.getInstance(requireContext()) }
    private var selectedDayIndex: Int = 1
    private var studentId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentReservasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        val profile = sessionManager.getActiveProfile()
        if (profile == null) {
            binding.root.visibility = View.GONE
            return
        }
        studentId = profile.id
        selectedDayIndex = repository.getTodayMenuIndex()

        setupChipGroup()
        binding.btnReservar.setOnClickListener { makeReservation() }
        binding.tvSaludo.text = getString(R.string.reservas_saludo, profile.displayName)

        viewLifecycleOwner.lifecycleScope.launch {
            updateMenuForSelectedDay()
            updateCurrentReservation()
        }
    }

    private fun setupChipGroup() {
        val dayMapping = mapOf(
            binding.chipLunes.id to 1,
            binding.chipMartes.id to 2,
            binding.chipMiercoles.id to 3,
            binding.chipJueves.id to 4,
            binding.chipViernes.id to 5,
        )

        dayMapping.forEach { (chipId, dayIndex) ->
            binding.chipGroup.findViewById<Chip>(chipId).tag = dayIndex
        }

        val initialChipId = dayMapping.entries.firstOrNull { it.value == selectedDayIndex }?.key
            ?: binding.chipLunes.id
        binding.chipGroup.check(initialChipId)

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chipId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(chipId)
            val dayIndex = chip.tag as? Int ?: return@setOnCheckedStateChangeListener
            if (selectedDayIndex != dayIndex) {
                selectedDayIndex = dayIndex
                viewLifecycleOwner.lifecycleScope.launch {
                    updateMenuForSelectedDay()
                    updateCurrentReservation()
                }
            }
        }
    }

    private suspend fun updateMenuForSelectedDay() {
        binding.groupContent.visibility = View.INVISIBLE
        binding.progressIndicator.visibility = View.VISIBLE

        val menu = withContext(Dispatchers.IO) {
            repository.getMenuForDay(selectedDayIndex)
        }

        binding.progressIndicator.visibility = View.GONE
        binding.groupContent.visibility = View.VISIBLE

        menu?.let {
            binding.tvDiaSeleccionado.text = it.dayName
            binding.tvMenuPrincipal.text = it.mainCourse
            binding.tvMenuEntrada.text = getString(R.string.reservas_menu_detail, getString(R.string.reservas_entrada), it.soup)
            binding.tvMenuPostre.text = getString(R.string.reservas_menu_detail, getString(R.string.reservas_postre), it.dessert)
            binding.tvMenuBebida.text = getString(R.string.reservas_menu_detail, getString(R.string.reservas_bebida), it.beverage)
        }
    }

    private suspend fun updateCurrentReservation() {
        val reservation = withContext(Dispatchers.IO) {
            repository.getReservationForDay(studentId, selectedDayIndex)
        }
        if (reservation != null) {
            binding.tvEstadoReserva.visibility = View.VISIBLE
            binding.tvEstadoReserva.text = getString(R.string.reservas_estado_reservado)
            binding.btnReservar.isEnabled = false
        } else {
            binding.tvEstadoReserva.visibility = View.GONE
            binding.btnReservar.isEnabled = true
        }
    }

    private fun makeReservation() {
        binding.btnReservar.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.reserveSlot(studentId, selectedDayIndex)
            }
            when (result) {
                is CafeteriaRepository.ReservationResult.Success -> {
                    binding.tvEstadoReserva.visibility = View.VISIBLE
                    binding.tvEstadoReserva.text = getString(R.string.reservas_estado_reservado)
                    Snackbar.make(binding.root, R.string.reservas_exito, Snackbar.LENGTH_SHORT).show()
                }
                is CafeteriaRepository.ReservationResult.AlreadyReserved -> {
                    binding.tvEstadoReserva.visibility = View.VISIBLE
                    binding.tvEstadoReserva.text = getString(R.string.reservas_estado_reservado)
                    Snackbar.make(binding.root, R.string.reservas_ya_registrada, Snackbar.LENGTH_SHORT).show()
                }
                CafeteriaRepository.ReservationResult.Error -> {
                    binding.btnReservar.isEnabled = true
                    Snackbar.make(binding.root, R.string.reservas_error, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
