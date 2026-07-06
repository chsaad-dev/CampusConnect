package com.campusconnect.feature.feed

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.databinding.DialogStoryViewBottomSheetBinding
import com.campusconnect.domain.model.MediaType
import com.campusconnect.domain.repository.PostRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StoryViewBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: DialogStoryViewBottomSheetBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var postRepository: PostRepository

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString(ARG_USER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogStoryViewBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        if (userId.isNullOrBlank()) {
            binding.tvNoStatus.show()
            return
        }

        loadUserStatus()
    }

    private fun loadUserStatus() {
        binding.progressBar.show()
        lifecycleScope.launch {
            postRepository.getStatusByUserId(userId!!).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.show()
                    }
                    is Resource.Success -> {
                        binding.progressBar.hide()
                        val statuses = resource.data
                        if (statuses.isNotEmpty()) {
                            // Display the latest status
                            val latestStatus = statuses.last()
                            
                            binding.tvAuthorName.text = latestStatus.authorName
                            binding.viewAvatar.loadAvatar(latestStatus.authorPhotoUrl, latestStatus.authorName)

                            val timeAgo = DateUtils.getRelativeTimeSpanString(
                                latestStatus.createdAt,
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS
                            )
                            binding.tvTime.text = timeAgo

                            if (latestStatus.caption.isNotEmpty()) {
                                binding.tvStatusText.text = latestStatus.caption
                                binding.tvStatusText.show()
                            } else {
                                binding.tvStatusText.hide()
                            }

                            // Render attachment
                            val firstMediaUrl = latestStatus.mediaUrls.firstOrNull()
                            if (latestStatus.mediaType != MediaType.NONE && firstMediaUrl != null) {
                                if (latestStatus.mediaType == MediaType.IMAGE) {
                                    binding.cardStatusImage.show()
                                    binding.cardStatusFile.hide()
                                    Glide.with(this@StoryViewBottomSheetFragment)
                                        .load(firstMediaUrl)
                                        .placeholder(R.color.surface_variant)
                                        .into(binding.ivStatusImage)
                                } else {
                                    binding.cardStatusFile.show()
                                    binding.cardStatusImage.hide()
                                    binding.tvFileTitle.text = firstMediaUrl.substringAfterLast("/").substringBefore("?")
                                    binding.tvFileType.text = latestStatus.mediaType.name
                                    
                                    binding.btnOpenFile.setOnClickListener {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(firstMediaUrl))
                                        startActivity(intent)
                                    }
                                }
                            } else {
                                binding.cardStatusImage.hide()
                                binding.cardStatusFile.hide()
                            }
                        } else {
                            binding.tvNoStatus.show()
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.hide()
                        binding.tvNoStatus.text = "Failed to load status"
                        binding.tvNoStatus.show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String): StoryViewBottomSheetFragment {
            val fragment = StoryViewBottomSheetFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }
}
