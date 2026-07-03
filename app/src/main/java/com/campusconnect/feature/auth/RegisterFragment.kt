package com.campusconnect.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.core.common.showErrorSnackbar
import com.campusconnect.core.common.showSnackbar
import com.campusconnect.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeRegisterState()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            var isValid = true

            if (email.isEmpty()) {
                binding.emailLayout.error = "Email is required"
                isValid = false
            } else {
                binding.emailLayout.error = null
            }

            if (password.isEmpty()) {
                binding.passwordLayout.error = "Password is required"
                isValid = false
            } else if (password.length < 6) {
                binding.passwordLayout.error = "Minimum 6 characters"
                isValid = false
            } else {
                binding.passwordLayout.error = null
            }

            if (confirmPassword != password) {
                binding.confirmPasswordLayout.error = "Passwords don't match"
                isValid = false
            } else {
                binding.confirmPasswordLayout.error = null
            }

            if (isValid) {
                viewModel.register(email, password)
            }
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeRegisterState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                            binding.btnRegister.isEnabled = false
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            binding.btnRegister.isEnabled = true
                            viewModel.resetRegisterState()

                            val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            com.campusconnect.core.common.AnalyticsHelper.logRegistration(currentUid, "student")

                            // Send verification email after registration
                            viewModel.sendVerificationEmail()
                            // Navigate to email verification screen
                            findNavController().navigate(R.id.action_registerFragment_to_emailVerificationFragment)
                        }
                        is Resource.Error -> {
                            binding.progressBar.hide()
                            binding.btnRegister.isEnabled = true
                            showErrorSnackbar(state.message)
                            viewModel.resetRegisterState()
                        }
                        null -> {
                            binding.progressBar.hide()
                            binding.btnRegister.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
