package com.example.campusconnect.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.campusconnect.databinding.ActivityForgotPasswordBinding
import com.example.campusconnect.ui.viewmodel.AuthViewModel
import com.example.campusconnect.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnReset.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                binding.emailLayout.error = "Required"
                return@setOnClickListener
            }
            binding.emailLayout.error = null
            viewModel.sendPasswordResetEmail(email)
        }

        viewModel.resetState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnReset.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnReset.isEnabled = true
                    Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnReset.isEnabled = true
                    Snackbar.make(binding.root, resource.message ?: "Error", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
