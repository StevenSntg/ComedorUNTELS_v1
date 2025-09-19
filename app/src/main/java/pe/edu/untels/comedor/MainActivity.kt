package pe.edu.untels.comedor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import pe.edu.untels.comedor.databinding.ActivityMainBinding
import pe.edu.untels.comedor.ui.HoyFragment
import pe.edu.untels.comedor.ui.PerfilFragment
import pe.edu.untels.comedor.ui.ReservasFragment
import pe.edu.untels.comedor.ui.SemanaFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fragments = mutableMapOf<Int, Fragment>()
    private var currentItemId = R.id.nav_hoy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restoreFragments()
        currentItemId = savedInstanceState?.getInt(KEY_SELECTED_ITEM) ?: R.id.nav_hoy

        // Fragment inicial
        showFragment(currentItemId)
        binding.bottomNav.selectedItemId = currentItemId

        // Navegación inferior
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId != currentItemId) {
                showFragment(item.itemId)
            }
            true
        }

        binding.bottomNav.setOnItemReselectedListener { item ->
            fragments[item.itemId]?.view?.findViewById<NestedScrollView?>(R.id.scrollView)?.smoothScrollTo(0, 0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_ITEM, currentItemId)
    }

    private fun showFragment(itemId: Int) {
        val fragment = fragments.getOrPut(itemId) {
            when (itemId) {
                R.id.nav_semana -> SemanaFragment()
                R.id.nav_reservas -> ReservasFragment()
                R.id.nav_perfil -> PerfilFragment()
                else -> HoyFragment()
            }
        }

        supportFragmentManager.commit {
            setReorderingAllowed(true)

            fragments.forEach { (id, storedFragment) ->
                if (storedFragment.isAdded && id != itemId) {
                    hide(storedFragment)
                }
            }

            if (fragment.isAdded) {
                show(fragment)
            } else {
                add(R.id.container, fragment, itemId.toString())
            }
        }

        currentItemId = itemId
    }

    private fun restoreFragments() {
        listOf(R.id.nav_hoy, R.id.nav_semana, R.id.nav_reservas, R.id.nav_perfil).forEach { itemId ->
            supportFragmentManager.findFragmentByTag(itemId.toString())?.let { fragment ->
                fragments[itemId] = fragment
            }
        }
    }

    companion object {
        private const val KEY_SELECTED_ITEM = "selected_bottom_nav_item"
    }
}
