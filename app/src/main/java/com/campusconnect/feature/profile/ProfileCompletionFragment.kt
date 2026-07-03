package com.campusconnect.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.isValidUsername
import com.campusconnect.core.common.show
import com.campusconnect.core.common.showErrorSnackbar
import com.campusconnect.databinding.FragmentProfileCompletionBinding
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileCompletionFragment : Fragment() {

    private var _binding: FragmentProfileCompletionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileCompletionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileCompletionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUsernameCheck()
        setupSkillChips()
        setupInterestChips()
        setupBloodGroupDropdown()
        setupDepartmentDropdown()
        setupSubmitButton()
        observeStates()
    }

    private fun setupUsernameCheck() {
        binding.etUsername.doAfterTextChanged { text ->
            val username = text.toString().trim()
            if (username.isValidUsername()) {
                viewModel.checkUsername(username)
            } else if (username.isNotEmpty()) {
                binding.usernameLayout.error = "Only lowercase letters, numbers, . and _"
                binding.tvUsernameStatus.hide()
            }
        }
    }

    private fun setupSkillChips() {
        val skills = listOf("Android", "iOS", "Web Dev", "ML/AI", "Data Science",
            "UI/UX", "Backend", "DevOps", "Cybersecurity", "Cloud", "Blockchain", "IoT")
        skills.forEach { skill ->
            val chip = Chip(requireContext()).apply {
                text = skill
                isCheckable = true
                isCheckedIconVisible = true
            }
            binding.chipGroupSkills.addView(chip)
        }
    }

    private fun setupInterestChips() {
        val interests = listOf("Coding", "Sports", "Music", "Art", "Gaming",
            "Photography", "Travel", "Reading", "Cooking", "Fitness", "Volunteering", "Debate")
        interests.forEach { interest ->
            val chip = Chip(requireContext()).apply {
                text = interest
                isCheckable = true
                isCheckedIconVisible = true
            }
            binding.chipGroupInterests.addView(chip)
        }
    }

    private fun setupBloodGroupDropdown() {
        val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            bloodGroups
        )
        binding.actvBloodGroup.setAdapter(adapter)
    }

    private fun setupDepartmentDropdown() {
        val departments = arrayOf(
            "Computer Science", "Electrical Engineering", "Mechanical Engineering",
            "Civil Engineering", "Electronics", "Chemical Engineering",
            "Information Technology", "Biotechnology", "Mathematics", "Physics", "Other"
        )
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            departments
        )
        binding.actvDepartment.setAdapter(adapter)
    }

    private fun setupSubmitButton() {
        binding.btnComplete.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val rollNumber = binding.etRollNumber.text.toString().trim()
            val department = binding.actvDepartment.text.toString().trim()
            val semesterText = binding.etSemester.text.toString().trim()
            val username = binding.etUsername.text.toString().trim().lowercase()
            val bloodGroup = binding.actvBloodGroup.text.toString().trim()

            var isValid = true

            if (name.isEmpty()) { binding.nameLayout.error = "Required"; isValid = false }
            else binding.nameLayout.error = null

            if (rollNumber.isEmpty()) { binding.rollLayout.error = "Required"; isValid = false }
            else binding.rollLayout.error = null

            if (department.isEmpty()) { binding.departmentLayout.error = "Required"; isValid = false }
            else binding.departmentLayout.error = null

            if (semesterText.isEmpty()) { binding.semesterLayout.error = "Required"; isValid = false }
            else binding.semesterLayout.error = null

            if (!username.isValidUsername()) { binding.usernameLayout.error = "Invalid username"; isValid = false }
            else binding.usernameLayout.error = null

            if (!isValid) return@setOnClickListener

            val semester = semesterText.toIntOrNull() ?: 1

            val selectedSkills = binding.chipGroupSkills.checkedChipIds.map { id ->
                binding.chipGroupSkills.findViewById<Chip>(id).text.toString()
            }

            val selectedInterests = binding.chipGroupInterests.checkedChipIds.map { id ->
                binding.chipGroupInterests.findViewById<Chip>(id).text.toString()
            }

            viewModel.completeProfile(
                name = name,
                rollNumber = rollNumber,
                department = department,
                semester = semester,
                uniqueUsername = username,
                bloodGroup = bloodGroup,
                skills = selectedSkills,
                interests = selectedInterests
            )
        }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.usernameState.collect { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.tvUsernameStatus.text = "Checking..."
                                binding.tvUsernameStatus.show()
                                binding.tvUsernameStatus.setTextColor(
                                    requireContext().getColor(R.color.text_secondary)
                                )
                            }
                            is Resource.Success -> {
                                binding.tvUsernameStatus.show()
                                if (state.data) {
                                    binding.tvUsernameStatus.text = "✓ Available"
                                    binding.tvUsernameStatus.setTextColor(
                                        requireContext().getColor(R.color.success)
                                    )
                                    binding.usernameLayout.error = null
                                } else {
                                    binding.tvUsernameStatus.text = "✗ Already taken"
                                    binding.tvUsernameStatus.setTextColor(
                                        requireContext().getColor(R.color.error)
                                    )
                                }
                            }
                            is Resource.Error -> {
                                binding.tvUsernameStatus.hide()
                            }
                            null -> {
                                binding.tvUsernameStatus.hide()
                            }
                        }
                    }
                }

                launch {
                    viewModel.completionState.collect { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.progressBar.show()
                                binding.btnComplete.isEnabled = false
                            }
                            is Resource.Success -> {
                                binding.progressBar.hide()
                                binding.btnComplete.isEnabled = true
                                viewModel.resetCompletionState()
                                // Navigate to main app
                                findNavController().navigate(R.id.action_profileCompletionFragment_to_mainNavGraph)
                            }
                            is Resource.Error -> {
                                binding.progressBar.hide()
                                binding.btnComplete.isEnabled = true
                                showErrorSnackbar(state.message)
                                viewModel.resetCompletionState()
                            }
                            null -> {
                                binding.progressBar.hide()
                                binding.btnComplete.isEnabled = true
                            }
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
