package com.example.campusconnect.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.campusconnect.MainActivity
import com.example.campusconnect.databinding.ActivityLoginBinding
import com.example.campusconnect.ui.viewmodel.AuthViewModel
import com.example.campusconnect.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if already logged in
        if (viewModel.isUserLoggedIn()) {
            goToMain()
            return
        }

        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.emailLayout.error = "Email required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.passwordLayout.error = "Password required"
                return@setOnClickListener
            }
            binding.emailLayout.error = null
            binding.passwordLayout.error = null
            viewModel.login(email, password)
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeAuthState() {
        viewModel.authState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    goToMain()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Snackbar.make(
                        binding.root,
                        resource.message ?: "Login failed",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
