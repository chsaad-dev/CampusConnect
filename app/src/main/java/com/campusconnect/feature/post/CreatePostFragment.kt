package com.campusconnect.feature.post

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.campusconnect.databinding.FragmentCreatePostBinding
import com.campusconnect.domain.model.MediaType
import com.campusconnect.domain.model.PostType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePostViewModel by viewModels()
    private var selectedFileUri: Uri? = null
    private var selectedMediaType: MediaType = MediaType.NONE

    // File Picker Launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            handleSelectedFile(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupTypeSelectors()
        setupMediaPicker()
        setupSubmitButton()
        observeUploadState()
    }

    private fun setupDropdowns() {
        val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        binding.actvBloodGroup.setAdapter(
            android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, bloodGroups)
        )

        val urgencies = arrayOf("Low", "Medium", "High", "Critical")
        binding.actvBloodUrgency.setAdapter(
            android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, urgencies)
        )
    }

    private fun setupTypeSelectors() {
        binding.chipGroupType.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            
            // Hide all dynamic layouts
            binding.layoutFieldsNote.hide()
            binding.layoutFieldsBlood.hide()
            binding.layoutFieldsLostfound.hide()
            binding.layoutFieldsRide.hide()

            // Show selected dynamic layout
            when (checkedId) {
                R.id.chip_note -> binding.layoutFieldsNote.show()
                R.id.chip_blood -> binding.layoutFieldsBlood.show()
                R.id.chip_lost_found -> binding.layoutFieldsLostfound.show()
                R.id.chip_ride -> binding.layoutFieldsRide.show()
            }
        }
    }

    private fun setupMediaPicker() {
        binding.btnAddAttachment.setOnClickListener {
            // Pick any file
            filePickerLauncher.launch("*/*")
        }

        binding.btnRemoveAttachment.setOnClickListener {
            selectedFileUri = null
            selectedMediaType = MediaType.NONE
            binding.cardAttachmentPreview.hide()
        }
    }

    private fun handleSelectedFile(uri: Uri) {
        selectedFileUri = uri
        val mimeType = requireContext().contentResolver.getType(uri) ?: ""
        
        selectedMediaType = when {
            mimeType.startsWith("image/") -> MediaType.IMAGE
            mimeType.startsWith("video/") -> MediaType.VIDEO
            mimeType.equals("application/pdf", true) -> MediaType.PDF
            mimeType.contains("word", true) || mimeType.contains("officedocument.wordprocessing", true) -> MediaType.DOCX
            mimeType.contains("powerpoint", true) || mimeType.contains("officedocument.presentation", true) -> MediaType.PPT
            else -> MediaType.NONE
        }

        binding.tvAttachmentName.text = uri.toString().substringAfterLast("/")
        binding.tvAttachmentType.text = selectedMediaType.name
        binding.ivAttachmentIcon.setImageResource(
            when (selectedMediaType) {
                MediaType.IMAGE -> android.R.drawable.ic_menu_gallery
                MediaType.VIDEO -> android.R.drawable.ic_media_play
                MediaType.PDF -> android.R.drawable.ic_menu_save
                else -> android.R.drawable.ic_menu_agenda
            }
        )
        binding.cardAttachmentPreview.show()
    }

    private fun setupSubmitButton() {
        binding.btnSubmitPost.setOnClickListener {
            val caption = binding.etCaption.text.toString().trim()
            if (caption.isEmpty()) {
                binding.captionLayout.error = "Caption is required"
                return@setOnClickListener
            }
            binding.captionLayout.error = null

            val postType = when (binding.chipGroupType.checkedChipId) {
                R.id.chip_blood -> PostType.BLOOD
                R.id.chip_lost_found -> PostType.LOST_FOUND
                R.id.chip_ride -> PostType.RIDE
                else -> PostType.NOTE
            }

            val extraData = mutableMapOf<String, Any>()
            when (postType) {
                PostType.NOTE -> {
                    extraData["subject"] = binding.etNoteSubject.text.toString().trim()
                    extraData["teacher"] = binding.etNoteTeacher.text.toString().trim()
                }
                PostType.BLOOD -> {
                    extraData["hospital"] = binding.etBloodHospital.text.toString().trim()
                    extraData["bloodGroup"] = binding.actvBloodGroup.text.toString().trim()
                    extraData["urgency"] = binding.actvBloodUrgency.text.toString().trim()
                }
                PostType.LOST_FOUND -> {
                    extraData["itemName"] = binding.etLfItemName.text.toString().trim()
                    extraData["category"] = binding.etLfCategory.text.toString().trim()
                    extraData["location"] = binding.etLfLocation.text.toString().trim()
                }
                PostType.RIDE -> {
                    extraData["from"] = binding.etRideFrom.text.toString().trim()
                    extraData["to"] = binding.etRideTo.text.toString().trim()
                    extraData["seatsTotal"] = binding.etRideSeats.text.toString().toIntOrNull() ?: 0
                    extraData["seatsLeft"] = binding.etRideSeats.text.toString().toIntOrNull() ?: 0
                    extraData["cost"] = binding.etRideCost.text.toString().trim()
                }
            }

            val department = "General"

            viewModel.createPost(
                caption = caption,
                type = postType,
                mediaType = selectedMediaType,
                fileUri = selectedFileUri,
                department = department,
                extraData = extraData
            )
        }
    }

    private fun observeUploadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uploadState.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                            binding.btnSubmitPost.isEnabled = false
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            binding.btnSubmitPost.isEnabled = true
                            showSnackbar("Post published successfully!")
                            viewModel.resetUploadState()
                            findNavController().navigateUp()
                        }
                        is Resource.Error -> {
                            binding.progressBar.hide()
                            binding.btnSubmitPost.isEnabled = true
                            showErrorSnackbar(state.message)
                            viewModel.resetUploadState()
                        }
                        null -> {
                            binding.progressBar.hide()
                            binding.btnSubmitPost.isEnabled = true
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
