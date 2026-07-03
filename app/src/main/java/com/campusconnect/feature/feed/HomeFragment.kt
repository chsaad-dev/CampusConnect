package com.campusconnect.feature.feed

import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.core.common.showErrorSnackbar
import com.campusconnect.core.common.showToast
import com.campusconnect.databinding.FragmentHomeBinding
import com.campusconnect.domain.model.Post
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FeedViewModel by viewModels()
    private lateinit var adapter: PostAdapter
    private lateinit var recommendedAdapter: RecommendedNoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeFeedState()
        observeRecommendedState()

        viewModel.loadInitialFeed()
    }

    private fun setupRecyclerView() {
        // Horizontal recommended notes
        recommendedAdapter = RecommendedNoteAdapter { note ->
            val bundle = Bundle().apply { putString("postId", note.postId) }
            findNavController().navigate(R.id.action_homeFragment_to_noteDetailFragment, bundle)
        }
        binding.rvRecommendedNotes.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecommendedNotes.adapter = recommendedAdapter

        adapter = PostAdapter(
            onLikeClick = { post -> viewModel.toggleLike(post) },
            onCommentClick = { post -> showComments(post.postId) },
            onShareClick = { post -> sharePost(post) },
            onCardClick = { post -> 
                val bundle = Bundle().apply { putString("postId", post.postId) }
                when (post.type) {
                    com.campusconnect.domain.model.PostType.NOTE -> findNavController().navigate(R.id.action_homeFragment_to_noteDetailFragment, bundle)
                    com.campusconnect.domain.model.PostType.BLOOD -> findNavController().navigate(R.id.action_homeFragment_to_bloodRequestDetailFragment, bundle)
                    com.campusconnect.domain.model.PostType.LOST_FOUND -> findNavController().navigate(R.id.action_homeFragment_to_lostFoundDetailFragment, bundle)
                    com.campusconnect.domain.model.PostType.RIDE -> findNavController().navigate(R.id.action_homeFragment_to_rideDetailFragment, bundle)
                }
            }
        )

        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter

        // Pagination Scroll Listener
        binding.rvFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (totalItemCount <= lastVisibleItem + 3) {
                    viewModel.fetchNextFeedBatch()
                }
            }
        })
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadInitialFeed()
        }

        binding.fabCreatePost.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createPostFragment)
        }

        binding.tabFeedType.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                viewModel.toggleFeedSorting(tab?.position == 1)
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun observeRecommendedState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recommendedNotes.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.layoutRecommended.hide()
                        }
                        is Resource.Success -> {
                            val notes = state.data
                            if (notes.isNotEmpty()) {
                                binding.layoutRecommended.show()
                                recommendedAdapter.submitList(notes)
                            } else {
                                binding.layoutRecommended.hide()
                            }
                        }
                        is Resource.Error -> {
                            binding.layoutRecommended.hide()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    private fun observeFeedState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.feedState.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                            binding.viewEmptyState.hide()
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            binding.swipeRefresh.isRefreshing = false
                            val posts = state.data
                            adapter.submitList(posts)
                            
                            if (posts.isEmpty()) {
                                binding.viewEmptyState.show()
                                binding.rvFeed.hide()
                            } else {
                                binding.viewEmptyState.hide()
                                binding.rvFeed.show()
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.hide()
                            binding.swipeRefresh.isRefreshing = false
                            showErrorSnackbar(state.message)
                        }
                        null -> {
                            binding.progressBar.hide()
                        }
                    }
                }
            }
        }
    }

    private fun showComments(postId: String) {
        val bottomSheet = CommentsBottomSheetFragment.newInstance(postId)
        bottomSheet.show(childFragmentManager, "comments_sheet")
    }

    private fun sharePost(post: Post) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "CampusConnect Post")
            putExtra(Intent.EXTRA_TEXT, "${post.authorName} posted: \"${post.caption}\"\nShared via CampusConnect.")
        }
        startActivity(Intent.createChooser(shareIntent, "Share post via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
