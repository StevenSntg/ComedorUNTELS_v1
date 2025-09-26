package pe.edu.untels.comedor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import pe.edu.untels.comedor.databinding.ActivityMainBinding
import pe.edu.untels.comedor.ui.MenuFragment
import pe.edu.untels.comedor.ui.PerfilFragment
import pe.edu.untels.comedor.ui.ReservasFragment

class MainActivity : AppCompatActivity(), MenuFragment.MenuNavigation {

    private lateinit var binding: ActivityMainBinding
    private val fragments = mutableMapOf<Int, Fragment>()
    private var currentItemId = R.id.nav_menu
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restoreFragments()
        currentItemId = savedInstanceState?.getInt(KEY_SELECTED_ITEM) ?: R.id.nav_menu

        showFragment(currentItemId)
        binding.bottomNav.selectedItemId = currentItemId

        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId != currentItemId) {
                showFragment(item.itemId)
            }
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_ITEM, currentItemId)
    }

    private fun showFragment(itemId: Int) {
        val fragment = fragments.getOrPut(itemId) {
            when (itemId) {
                R.id.nav_reservas -> ReservasFragment()
                R.id.nav_perfil -> PerfilFragment()
                else -> MenuFragment()
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
        listOf(R.id.nav_menu, R.id.nav_reservas, R.id.nav_perfil).forEach { itemId ->
            supportFragmentManager.findFragmentByTag(itemId.toString())?.let { fragment ->
                fragments[itemId] = fragment
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }

    override fun openReservations() {
        binding.bottomNav.selectedItemId = R.id.nav_reservas
        showFragment(R.id.nav_reservas)
    }

    companion object {
        private const val KEY_SELECTED_ITEM = "selected_bottom_nav_item"
    }
}
