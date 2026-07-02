package com.example.campusconnect.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.campusconnect.data.model.User
import com.example.campusconnect.databinding.ActivityRegisterBinding
import com.example.campusconnect.ui.viewmodel.AuthViewModel
import com.example.campusconnect.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val rollNumber = binding.etRoll.text.toString().trim()
            val semester = binding.etSemester.text.toString().trim()
            val department = binding.etDepartment.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            var isValid = true

            if (name.isEmpty()) { binding.nameLayout.error = "Required"; isValid = false } else { binding.nameLayout.error = null }
            if (email.isEmpty()) { binding.emailLayout.error = "Required"; isValid = false } else { binding.emailLayout.error = null }
            if (rollNumber.isEmpty()) { binding.rollLayout.error = "Required"; isValid = false } else { binding.rollLayout.error = null }
            if (semester.isEmpty()) { binding.semesterLayout.error = "Required"; isValid = false } else { binding.semesterLayout.error = null }
            if (department.isEmpty()) { binding.deptLayout.error = "Required"; isValid = false } else { binding.deptLayout.error = null }
            if (password.length < 6) { binding.passwordLayout.error = "Min 6 chars"; isValid = false } else { binding.passwordLayout.error = null }

            if (!isValid) return@setOnClickListener

            val user = User(
                name = name,
                email = email,
                rollNumber = rollNumber,
                semester = semester,
                department = department,
                phoneNumber = phone
            )

            viewModel.register(user, password)
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun observeAuthState() {
        viewModel.authState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    finish() // go back to login
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Snackbar.make(
                        binding.root,
                        resource.message ?: "Registration failed",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
