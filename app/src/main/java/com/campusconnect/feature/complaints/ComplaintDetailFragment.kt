package com.campusconnect.feature.complaints

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.core.common.showErrorSnackbar
import com.campusconnect.databinding.FragmentComplaintDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ComplaintDetailFragment : Fragment() {

    private var _binding: FragmentComplaintDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ComplaintViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComplaintDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val complaintId = arguments?.getString("complaintId")
        if (complaintId == null) {
            showErrorSnackbar("Invalid Complaint ID")
            findNavController().navigateUp()
            return
        }

        setupObserver()
        viewModel.loadComplaintDetail(complaintId)
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.complaintDetail.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                            binding.contentCard.hide()
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            binding.contentCard.show()

                            val complaint = state.data
                            binding.tvCategory.text = complaint.category
                            binding.tvLocation.text = "Location: ${complaint.location}"
                            binding.tvDescription.text = complaint.description

                            if (complaint.mediaUrl.isNotEmpty()) {
                                binding.ivComplaint.show()
                                Glide.with(requireContext())
                                    .load(complaint.mediaUrl)
                                    .into(binding.ivComplaint)
                            } else {
                                binding.ivComplaint.hide()
                            }

                            // Setup timeline highlights
                            updateTimelineUI(complaint.status, complaint.duplicateOfId)
                        }
                        is Resource.Error -> {
                            binding.progressBar.hide()
                            showErrorSnackbar(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun updateTimelineUI(status: String, duplicateOfId: String) {
        val activeColor = ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
        val inactiveColor = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)

        val activeIcon = android.R.drawable.presence_online
        val inactiveIcon = android.R.drawable.presence_invisible

        // Reset step views
        binding.ivStepSubmitted.setImageResource(activeIcon)
        binding.tvStepSubmitted.setTextColor(activeColor)
        binding.tvStepSubmitted.setTypeface(null, Typeface.BOLD)

        binding.ivStepProgress.setImageResource(inactiveIcon)
        binding.tvStepProgress.setTextColor(inactiveColor)
        binding.tvStepProgress.setTypeface(null, Typeface.NORMAL)

        binding.ivStepResolved.setImageResource(inactiveIcon)
        binding.tvStepResolved.setTextColor(inactiveColor)
        binding.tvStepResolved.setTypeface(null, Typeface.NORMAL)

        binding.layoutDuplicate.hide()

        when (status) {
            "in_progress" -> {
                binding.ivStepProgress.setImageResource(activeIcon)
                binding.tvStepProgress.setTextColor(activeColor)
                binding.tvStepProgress.setTypeface(null, Typeface.BOLD)
            }
            "resolved" -> {
                binding.ivStepProgress.setImageResource(activeIcon)
                binding.tvStepProgress.setTextColor(activeColor)
                
                binding.ivStepResolved.setImageResource(activeIcon)
                binding.tvStepResolved.setTextColor(activeColor)
                binding.tvStepResolved.setTypeface(null, Typeface.BOLD)
            }
            "duplicate" -> {
                binding.layoutDuplicate.show()
                binding.tvDuplicateId.text = "Merged under ID: #$duplicateOfId"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
