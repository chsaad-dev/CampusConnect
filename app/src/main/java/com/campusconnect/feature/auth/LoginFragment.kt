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
import com.campusconnect.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeLoginState()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

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
            } else {
                binding.passwordLayout.error = null
            }

            if (isValid) {
                viewModel.login(email, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                            binding.btnLogin.isEnabled = false
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            binding.btnLogin.isEnabled = true
                            viewModel.resetLoginState()

                            val user = state.data
                            com.campusconnect.core.common.AnalyticsHelper.logLogin(user.uid)

                            if (user.profileComplete) {
                                // Navigate to main app
                                findNavController().navigate(R.id.action_loginFragment_to_mainNavGraph)
                            } else {
                                // Navigate to profile completion
                                findNavController().navigate(R.id.action_loginFragment_to_profileCompletionFragment)
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.hide()
                            binding.btnLogin.isEnabled = true
                            showErrorSnackbar(state.message)
                            viewModel.resetLoginState()
                        }
                        null -> {
                            binding.progressBar.hide()
                            binding.btnLogin.isEnabled = true
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
