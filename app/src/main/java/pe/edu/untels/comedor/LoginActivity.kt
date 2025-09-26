package pe.edu.untels.comedor

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.edu.untels.comedor.data.CafeteriaRepository
import pe.edu.untels.comedor.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private val repository: CafeteriaRepository by lazy { CafeteriaRepository.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else {
                false
            }
        }

        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }
    }

    private fun attemptLogin() {
        val code = binding.etCode.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()

        var hasError = false
        if (code.isEmpty()) {
            binding.tilCode.error = getString(R.string.login_error_required)
            hasError = true
        } else {
            binding.tilCode.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.login_error_required)
            hasError = true
        } else {
            binding.tilPassword.error = null
        }

        if (hasError) return

        showLoading(true)
        lifecycleScope.launch {
            val student = withContext(Dispatchers.IO) {
                repository.authenticateStudent(code, password)
            }
            showLoading(false)

            if (student != null) {
                sessionManager.persistSession(student)
                navigateToMain()
            } else {
                binding.tilPassword.error = getString(R.string.login_error_invalid)
                Snackbar.make(binding.root, R.string.login_error_invalid, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.btnLogin.isEnabled = !show
        binding.progressBar.isVisible = show
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }
}
