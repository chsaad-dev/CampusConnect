package com.campusconnect.feature.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.campusconnect.R
import com.campusconnect.core.theme.ThemeManager
import com.campusconnect.core.theme.ThemeMode
import com.campusconnect.databinding.FragmentSettingsBinding
import com.campusconnect.feature.auth.AuthViewModel
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupThemeToggle()
        setupLogout()
    }

    private fun setupThemeToggle() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                themeManager.themeMode.collect { mode ->
                    binding.switchTheme.isChecked = mode == ThemeMode.DARK
                    binding.tvThemeLabel.text = when (mode) {
                        ThemeMode.LIGHT -> "Light Mode"
                        ThemeMode.DARK -> "Dark Mode"
                    }
                }
            }
        }

        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                themeManager.setThemeMode(
                    if (isChecked) ThemeMode.DARK else ThemeMode.LIGHT
                )
            }
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_settingsFragment_to_authNavGraph)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
