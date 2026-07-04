package com.campusconnect.feature.post

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.databinding.DialogCreatePostBottomSheetBinding
import com.campusconnect.domain.model.MediaType
import com.campusconnect.domain.model.PostType
import com.campusconnect.feature.feed.HomeFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePostBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: DialogCreatePostBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePostViewModel by viewModels()

    private var selectedFileUri: Uri? = null
    private var selectedMediaType: MediaType = MediaType.NONE
    private var currentPostType = PostType.NOTE

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
        _binding = DialogCreatePostBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTypeSelectors()
        setupDropdowns()
        setupMediaPickers()
        setupTextWatchers()
        setupSubmitButton()
        observeUploadState()

        // Default selection
        selectPostType(PostType.NOTE)
    }

    private fun setupDropdowns() {
        val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        binding.actvBloodGroup.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, bloodGroups)
        )

        val urgencies = arrayOf("Low", "Medium", "High", "Critical")
        binding.actvBloodUrgency.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, urgencies)
        )
    }

    private fun setupTypeSelectors() {
        binding.cardTypeNote.setOnClickListener { selectPostType(PostType.NOTE) }
        binding.cardTypeBlood.setOnClickListener { selectPostType(PostType.BLOOD) }
        binding.cardTypeLostfound.setOnClickListener { selectPostType(PostType.LOST_FOUND) }
        binding.cardTypeRide.setOnClickListener { selectPostType(PostType.RIDE) }
    }

    private fun selectPostType(type: PostType) {
        currentPostType = type

        val outlineColor = requireContext().getColor(R.color.outline)
        val primaryColor = requireContext().getColor(R.color.primary)
        val defaultStroke = dpToPx(1)
        val activeStroke = dpToPx(2)

        // Reset borders
        binding.cardTypeNote.strokeColor = outlineColor
        binding.cardTypeNote.strokeWidth = defaultStroke
        binding.cardTypeBlood.strokeColor = outlineColor
        binding.cardTypeBlood.strokeWidth = defaultStroke
        binding.cardTypeLostfound.strokeColor = outlineColor
        binding.cardTypeLostfound.strokeWidth = defaultStroke
        binding.cardTypeRide.strokeColor = outlineColor
        binding.cardTypeRide.strokeWidth = defaultStroke

        // Reset backgrounds to transparent/default surface
        binding.cardTypeNote.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.surface))
        binding.cardTypeBlood.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.surface))
        binding.cardTypeLostfound.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.surface))
        binding.cardTypeRide.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.surface))

        // Hide dynamic inputs
        binding.layoutFieldsNote.visibility = View.GONE
        binding.layoutFieldsBlood.visibility = View.GONE
        binding.layoutFieldsLostfound.visibility = View.GONE
        binding.layoutFieldsRide.visibility = View.GONE

        // Apply active state
        when (type) {
            PostType.NOTE -> {
                binding.cardTypeNote.strokeColor = primaryColor
                binding.cardTypeNote.strokeWidth = activeStroke
                binding.cardTypeNote.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.avatar_palette_6))
                binding.layoutFieldsNote.visibility = View.VISIBLE
            }
            PostType.BLOOD -> {
                binding.cardTypeBlood.strokeColor = requireContext().getColor(R.color.blood_red)
                binding.cardTypeBlood.strokeWidth = activeStroke
                binding.cardTypeBlood.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.avatar_palette_1))
                binding.layoutFieldsBlood.visibility = View.VISIBLE
            }
            PostType.LOST_FOUND -> {
                binding.cardTypeLostfound.strokeColor = requireContext().getColor(R.color.lost_found_orange)
                binding.cardTypeLostfound.strokeWidth = activeStroke
                binding.cardTypeLostfound.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.avatar_palette_3))
                binding.layoutFieldsLostfound.visibility = View.VISIBLE
            }
            PostType.RIDE -> {
                binding.cardTypeRide.strokeColor = requireContext().getColor(R.color.ride_blue)
                binding.cardTypeRide.strokeWidth = activeStroke
                binding.cardTypeRide.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.avatar_palette_4))
                binding.layoutFieldsRide.visibility = View.VISIBLE
            }
        }
        validateFields()
    }

    private fun setupMediaPickers() {
        binding.chipUploadFile.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }
        binding.chipUploadPhoto.setOnClickListener {
            filePickerLauncher.launch("image/*")
        }
        binding.btnRemoveAttachment.setOnClickListener {
            selectedFileUri = null
            selectedMediaType = MediaType.NONE
            binding.cardAttachmentPreview.visibility = View.GONE
            validateFields()
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
        binding.cardAttachmentPreview.visibility = View.VISIBLE
        validateFields()
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateFields()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etCaption.addTextChangedListener(watcher)
        binding.etNoteSubject.addTextChangedListener(watcher)
        binding.etNoteTeacher.addTextChangedListener(watcher)
        binding.etBloodHospital.addTextChangedListener(watcher)
        binding.actvBloodGroup.addTextChangedListener(watcher)
        binding.actvBloodUrgency.addTextChangedListener(watcher)
        binding.etLfItemName.addTextChangedListener(watcher)
        binding.etLfCategory.addTextChangedListener(watcher)
        binding.etLfLocation.addTextChangedListener(watcher)
        binding.etRideFrom.addTextChangedListener(watcher)
        binding.etRideTo.addTextChangedListener(watcher)
        binding.etRideSeats.addTextChangedListener(watcher)
        binding.etRideCost.addTextChangedListener(watcher)
    }

    private fun validateFields() {
        val caption = binding.etCaption.text.toString().trim()
        if (caption.isEmpty()) {
            binding.btnSubmitPost.isEnabled = false
            return
        }

        val isValid = when (currentPostType) {
            PostType.NOTE -> {
                val subject = binding.etNoteSubject.text.toString().trim()
                val teacher = binding.etNoteTeacher.text.toString().trim()
                subject.isNotEmpty() && teacher.isNotEmpty() && selectedFileUri != null
            }
            PostType.BLOOD -> {
                val hospital = binding.etBloodHospital.text.toString().trim()
                val group = binding.actvBloodGroup.text.toString().trim()
                val urgency = binding.actvBloodUrgency.text.toString().trim()
                hospital.isNotEmpty() && group.isNotEmpty() && urgency.isNotEmpty()
            }
            PostType.LOST_FOUND -> {
                val name = binding.etLfItemName.text.toString().trim()
                val category = binding.etLfCategory.text.toString().trim()
                val location = binding.etLfLocation.text.toString().trim()
                name.isNotEmpty() && category.isNotEmpty() && location.isNotEmpty()
            }
            PostType.RIDE -> {
                val from = binding.etRideFrom.text.toString().trim()
                val to = binding.etRideTo.text.toString().trim()
                val seats = binding.etRideSeats.text.toString().trim()
                val cost = binding.etRideCost.text.toString().trim()
                from.isNotEmpty() && to.isNotEmpty() && seats.isNotEmpty() && seats.toIntOrNull() != null && cost.isNotEmpty()
            }
        }
        binding.btnSubmitPost.isEnabled = isValid
    }

    private fun setupSubmitButton() {
        binding.btnSubmitPost.setOnClickListener {
            val caption = binding.etCaption.text.toString().trim()
            val extraData = mutableMapOf<String, Any>()

            when (currentPostType) {
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
                    val seatsVal = binding.etRideSeats.text.toString().toIntOrNull() ?: 0
                    extraData["seatsTotal"] = seatsVal
                    extraData["seatsLeft"] = seatsVal
                    extraData["cost"] = binding.etRideCost.text.toString().trim()
                }
            }

            // In our system, default department to "General" unless loaded from profile
            val department = "General"

            viewModel.createPost(
                caption = caption,
                type = currentPostType,
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
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnSubmitPost.isEnabled = false
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Post published successfully!", Toast.LENGTH_SHORT).show()
                            viewModel.resetUploadState()

                            // Navigate & Scroll to top
                            val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                            val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
                            if (currentFragment is HomeFragment) {
                                currentFragment.reloadAndScrollToTop()
                            }

                            dismiss()
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnSubmitPost.isEnabled = true
                            Toast.makeText(requireContext(), state.message ?: "Failed to post", Toast.LENGTH_LONG).show()
                            viewModel.resetUploadState()
                        }
                        null -> {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
