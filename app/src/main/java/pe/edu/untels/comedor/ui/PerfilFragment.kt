package pe.edu.untels.comedor.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.edu.untels.comedor.LoginActivity
import pe.edu.untels.comedor.R
import pe.edu.untels.comedor.SessionManager
import pe.edu.untels.comedor.data.CafeteriaRepository
import pe.edu.untels.comedor.databinding.FragmentPerfilBinding
import java.text.DateFormat
import java.util.Date

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val repository by lazy { CafeteriaRepository.getInstance(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        val profile = sessionManager.getActiveProfile()
        if (profile == null) {
            binding.layoutContent.visibility = View.GONE
            binding.stateEmpty.visibility = View.VISIBLE
            return
        }

        binding.layoutContent.visibility = View.VISIBLE
        binding.stateEmpty.visibility = View.GONE

        binding.tvNombreUsuario.text = profile.displayName
        binding.tvCodigo.text = getString(R.string.perfil_codigo, profile.code)
        binding.tvCarrera.text = profile.career

        binding.btnCerrarSesion.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
            requireActivity().finish()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val reservation = withContext(Dispatchers.IO) {
                repository.getLatestReservation(profile.id)
            }
            if (reservation != null) {
                val formattedDate = DateFormat.getDateTimeInstance().format(Date(reservation.createdAtMillis))
                binding.cardReserva.visibility = View.VISIBLE
                binding.tvUltimaReserva.text = getString(R.string.perfil_ultima_reserva, formattedDate)
            } else {
                binding.cardReserva.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
