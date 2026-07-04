package com.campusconnect.feature.settings

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
import com.campusconnect.core.theme.ThemeManager
import com.campusconnect.core.theme.ThemeMode
import com.campusconnect.databinding.FragmentSettingsBinding
import com.campusconnect.domain.model.UserRole
import com.campusconnect.domain.repository.UserRepository
import com.campusconnect.feature.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var userRepository: UserRepository

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
        setupMenuClickListeners()
        loadUserProfile()
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

    private fun setupMenuClickListeners() {
        binding.cardComplaints.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_complaintListFragment)
        }
        binding.cardEvents.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_eventListFragment)
        }
        binding.cardJobs.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_jobListFragment)
        }
        binding.cardAdmin.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_adminDashboardFragment)
        }
    }

    private fun loadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userRepository.getCurrentUserProfile().collectLatest { resource ->
                    if (resource is Resource.Success) {
                        val user = resource.data
                        binding.tvProfileName.text = user.name.takeIf { it.isNotEmpty() } ?: "User"
                        binding.tvProfileDept.text = user.department.takeIf { it.isNotEmpty() } ?: "General Department"
                        binding.tvProfileFriends.text = user.friendsCount.toString()
                        binding.tvProfileReputation.text = user.reputationPoints.toString()
                        binding.tvProfilePosts.text = "4"
                        
                        binding.viewProfileAvatar.loadAvatar(user.photoUrl, user.name)

                        if (user.role == UserRole.ADMIN) {
                            binding.cardAdmin.visibility = View.VISIBLE
                        } else {
                            binding.cardAdmin.visibility = View.GONE
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
