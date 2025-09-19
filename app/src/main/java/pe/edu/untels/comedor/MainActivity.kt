package pe.edu.untels.comedor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import pe.edu.untels.comedor.databinding.ActivityMainBinding
import pe.edu.untels.comedor.ui.HoyFragment
import pe.edu.untels.comedor.ui.PerfilFragment
import pe.edu.untels.comedor.ui.ReservasFragment
import pe.edu.untels.comedor.ui.SemanaFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fragment inicial
        loadFragment(HoyFragment())

        // Navegación inferior
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_hoy     -> loadFragment(HoyFragment())
                R.id.nav_semana  -> loadFragment(SemanaFragment())
                R.id.nav_reservas-> loadFragment(ReservasFragment())
                R.id.nav_perfil  -> loadFragment(PerfilFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
