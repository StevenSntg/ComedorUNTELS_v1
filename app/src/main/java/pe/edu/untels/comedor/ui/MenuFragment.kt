package pe.edu.untels.comedor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.edu.untels.comedor.R
import pe.edu.untels.comedor.data.CafeteriaRepository
import pe.edu.untels.comedor.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private val repository by lazy { CafeteriaRepository.getInstance(requireContext()) }
    private val adapter = WeeklyMenuAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        binding.btnReservarAhora.setOnClickListener {
            (activity as? MenuNavigation)?.openReservations()
        }
        loadMenu()
    }

    private fun setupRecyclerView() {
        binding.recyclerSemana.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSemana.adapter = adapter
    }

    private fun loadMenu() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.contentGroup.visibility = View.INVISIBLE
            binding.progressIndicator.visibility = View.VISIBLE

            val todayIndex = repository.getTodayMenuIndex()
            val todayMenu = withContext(Dispatchers.IO) {
                repository.getMenuForDay(todayIndex)
            }
            val weeklyMenu = withContext(Dispatchers.IO) {
                repository.getWeeklyMenu()
            }

            binding.progressIndicator.visibility = View.GONE
            binding.contentGroup.visibility = View.VISIBLE

            todayMenu?.let { menu ->
                binding.tvDiaActual.text = menu.dayName
                binding.tvDestacado.text = menu.highlight
                binding.tvPlatoFuerte.text = menu.mainCourse
                binding.tvEntrada.text = menu.soup
                binding.tvPostre.text = menu.dessert
                binding.tvBebida.text = menu.beverage
                binding.tvCalorias.text = getString(R.string.menu_calories, menu.calories)
            }
            adapter.submitList(weeklyMenu)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface MenuNavigation {
        fun openReservations()
    }
}
