package com.campusconnect.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.campusconnect.R
import com.campusconnect.core.common.showSnackbar
import com.campusconnect.databinding.FragmentEmailVerificationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmailVerificationFragment : Fragment() {

    private var _binding: FragmentEmailVerificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCheckVerification.setOnClickListener {
            checkVerification()
        }

        binding.btnResendEmail.setOnClickListener {
            viewModel.sendVerificationEmail()
            showSnackbar("Verification email resent!")
        }

        // Start auto-checking
        startAutoCheck()
    }

    private fun startAutoCheck() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Poll every 3 seconds for verification status
            repeat(60) { // Max ~3 minutes
                delay(3000)
                if (viewModel.isEmailVerified()) {
                    navigateToProfileCompletion()
                    return@launch
                }
            }
        }
    }

    private fun checkVerification() {
        if (viewModel.isEmailVerified()) {
            navigateToProfileCompletion()
        } else {
            showSnackbar("Email not yet verified. Please check your inbox.")
        }
    }

    private fun navigateToProfileCompletion() {
        findNavController().navigate(R.id.action_emailVerificationFragment_to_profileCompletionFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
