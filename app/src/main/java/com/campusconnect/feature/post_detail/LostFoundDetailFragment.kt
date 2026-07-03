package com.campusconnect.feature.post_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.campusconnect.databinding.FragmentLostFoundDetailBinding
import com.campusconnect.domain.model.PostType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LostFoundDetailFragment : Fragment() {

    private var _binding: FragmentLostFoundDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLostFoundDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val postId = arguments?.getString("postId")
        if (postId == null) {
            showErrorSnackbar("Invalid Post ID")
            findNavController().navigateUp()
            return
        }

        viewModel.loadDetails(postId, PostType.LOST_FOUND)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lostFoundDetails.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                            binding.contentCard.hide()
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            binding.contentCard.show()
                            
                            val details = state.data
                            binding.tvItemName.text = details.itemName.takeIf { it.isNotEmpty() } ?: "Unknown Item"
                            binding.tvCategory.text = details.category.takeIf { it.isNotEmpty() } ?: "Misc"
                            binding.tvLocation.text = details.location.takeIf { it.isNotEmpty() } ?: "Unknown Location"

                            binding.btnContactOwner.setOnClickListener {
                                showSnackbar("Navigating to chat/contact...")
                            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
