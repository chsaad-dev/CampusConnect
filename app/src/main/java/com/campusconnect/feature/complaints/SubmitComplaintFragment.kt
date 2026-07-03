package com.campusconnect.feature.complaints

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.core.common.showErrorSnackbar
import com.campusconnect.core.common.showSnackbar
import com.campusconnect.databinding.FragmentSubmitComplaintBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubmitComplaintFragment : Fragment() {

    private var _binding: FragmentSubmitComplaintBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ComplaintViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    // Image Picker Launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivPreview.setImageURI(uri)
            binding.cardImagePreview.show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubmitComplaintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupDropdowns()
        setupListeners()
        observeSubmitState()
    }

    private fun setupDropdowns() {
        val categories = arrayOf("Hostel", "Academics", "Infrastructure", "Others")
        binding.actvCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        )

        val priorities = arrayOf("Low", "Medium", "High")
        binding.actvPriority.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, priorities)
        )
    }

    private fun setupListeners() {
        binding.btnAttachImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnRemoveImage.setOnClickListener {
            selectedImageUri = null
            binding.cardImagePreview.hide()
        }

        binding.btnSubmit.setOnClickListener {
            val category = binding.actvCategory.text.toString().trim()
            val priority = binding.actvPriority.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (category.isEmpty() || priority.isEmpty() || location.isEmpty() || description.isEmpty()) {
                showErrorSnackbar("Please fill out all fields.")
                return@setOnClickListener
            }

            viewModel.checkForDuplicates(category, location, description)
        }
    }

    private fun executeComplaintSubmission() {
        val category = binding.actvCategory.text.toString().trim()
        val priority = binding.actvPriority.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        viewModel.submitComplaint(
            category = category,
            priority = priority,
            location = location,
            description = description,
            imageUri = selectedImageUri
        )
    }

    private fun observeSubmitState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. Observe duplicate check
                launch {
                    viewModel.duplicateCheckState.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.progressBar.show()
                                binding.btnSubmit.isEnabled = false
                            }
                            is Resource.Success -> {
                                binding.progressBar.hide()
                                binding.btnSubmit.isEnabled = true
                                val duplicates = state.data
                                if (duplicates.isEmpty()) {
                                    executeComplaintSubmission()
                                } else {
                                    com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                                        .setTitle("Similar Issue Reported")
                                        .setMessage("A similar issue in '${binding.etLocation.text}' was filed within the last 24 hours:\n\n\"${duplicates.first().description}\"\n\nDo you still want to file a new complaint?")
                                        .setNegativeButton("Cancel") { _, _ -> viewModel.resetDuplicateCheck() }
                                        .setPositiveButton("Submit Anyway") { _, _ ->
                                            executeComplaintSubmission()
                                            viewModel.resetDuplicateCheck()
                                        }
                                        .show()
                                }
                            }
                            is Resource.Error -> {
                                binding.progressBar.hide()
                                binding.btnSubmit.isEnabled = true
                                executeComplaintSubmission() // Proceed anyway if query fails
                                viewModel.resetDuplicateCheck()
                            }
                            null -> {}
                        }
                    }
                }

                // 2. Observe submit state
                launch {
                    viewModel.submitState.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                            binding.btnSubmit.isEnabled = false
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            binding.btnSubmit.isEnabled = true
                            showSnackbar("Complaint submitted successfully!")
                            val cat = binding.actvCategory.text.toString().trim()
                            com.campusconnect.core.common.AnalyticsHelper.logComplaintSubmitted(cat)
                            viewModel.resetSubmitState()
                            findNavController().navigateUp()
                        }
                        is Resource.Error -> {
                            binding.progressBar.hide()
                            binding.btnSubmit.isEnabled = true
                            showErrorSnackbar(state.message)
                            viewModel.resetSubmitState()
                        }
                        null -> {}
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
