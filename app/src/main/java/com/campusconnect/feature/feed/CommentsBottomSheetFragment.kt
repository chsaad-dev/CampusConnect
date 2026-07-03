package com.campusconnect.feature.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.core.common.showErrorSnackbar
import com.campusconnect.databinding.BottomsheetCommentsBinding
import com.campusconnect.domain.model.Comment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CommentsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomsheetCommentsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FeedViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: CommentAdapter
    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = arguments?.getString(ARG_POST_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupInput()
        observeComments()
        
        postId?.let { viewModel.fetchComments(it) }
    }

    private fun setupRecyclerView() {
        adapter = CommentAdapter()
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = adapter
    }

    private fun setupInput() {
        binding.btnSendComment.isEnabled = false
        binding.etComment.doAfterTextChanged { text ->
            binding.btnSendComment.isEnabled = !text.toString().trim().isNullOrEmpty()
        }

        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            val pid = postId
            if (text.isNotEmpty() && pid != null) {
                viewModel.addComment(pid, text)
                binding.etComment.text?.clear()
            }
        }
    }

    private fun observeComments() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.commentsState.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.progressBar.show()
                                binding.viewEmptyState.hide()
                            }
                            is Resource.Success -> {
                                binding.progressBar.hide()
                                val comments = state.data
                                adapter.submitList(comments)
                                if (comments.isEmpty()) {
                                    binding.viewEmptyState.show()
                                    binding.rvComments.hide()
                                } else {
                                    binding.viewEmptyState.hide()
                                    binding.rvComments.show()
                                }
                            }
                            is Resource.Error -> {
                                binding.progressBar.hide()
                                showErrorSnackbar(state.message)
                            }
                            null -> {
                                binding.progressBar.hide()
                            }
                        }
                    }
                }

                launch {
                    viewModel.commentActionState.collectLatest { state ->
                        if (state is Resource.Success) {
                            // Fetch user detail again or let the adapter automatically update
                            postId?.let { viewModel.fetchComments(it) }
                            viewModel.clearCommentActionState()
                        } else if (state is Resource.Error) {
                            showErrorSnackbar(state.message)
                            viewModel.clearCommentActionState()
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

    companion object {
        private const val ARG_POST_ID = "arg_post_id"

        fun newInstance(postId: String): CommentsBottomSheetFragment {
            return CommentsBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_ID, postId)
                }
            }
        }
    }
}
