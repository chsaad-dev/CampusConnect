package com.campusconnect.feature.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.databinding.FragmentSearchBinding
import com.campusconnect.domain.repository.PostRepository
import com.campusconnect.feature.feed.PostAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var postRepository: PostRepository

    @Inject
    lateinit var preferenceManager: com.campusconnect.core.common.PreferenceManager

    private lateinit var adapter: PostAdapter
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchInput()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(
            preferenceManager = preferenceManager,
            onLikeClick = { post -> toggleLike(post) },
            onCommentClick = { post -> showComments(post.postId) },
            onShareClick = { post -> sharePost(post) },
            onCardClick = { post ->
                val bundle = Bundle().apply { putString("postId", post.postId) }
                when (post.type) {
                    com.campusconnect.domain.model.PostType.NOTE -> findNavController().navigate(R.id.noteDetailFragment, bundle)
                    com.campusconnect.domain.model.PostType.BLOOD -> findNavController().navigate(R.id.bloodRequestDetailFragment, bundle)
                    com.campusconnect.domain.model.PostType.LOST_FOUND -> findNavController().navigate(R.id.lostFoundDetailFragment, bundle)
                    com.campusconnect.domain.model.PostType.RIDE -> findNavController().navigate(R.id.rideDetailFragment, bundle)
                    com.campusconnect.domain.model.PostType.STATUS -> { /* No-op */ }
                }
            }
        )
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = adapter
    }

    private fun setupSearchInput() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                val queryText = s.toString().trim()
                if (queryText.isEmpty()) {
                    adapter.submitList(emptyList())
                    binding.emptyStateView.visibility = View.VISIBLE
                    binding.emptyStateView.setupEmptyState(
                        R.drawable.ic_search,
                        "Search CampusConnect",
                        "Type in keywords to search posts, notes, blood requests, or drivers."
                    )
                    return
                }
                searchJob = lifecycleScope.launch {
                    delay(300)
                    performSearch(queryText)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyStateView.visibility = View.GONE
        lifecycleScope.launch {
            postRepository.searchPosts(query).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        val results = resource.data
                        adapter.submitList(results)
                        if (results.isEmpty()) {
                            binding.emptyStateView.visibility = View.VISIBLE
                            binding.emptyStateView.setupEmptyState(
                                R.drawable.ic_search,
                                "No results found",
                                "We couldn't find any posts matching \"$query\". Check spelling or try other keywords."
                            )
                        } else {
                            binding.emptyStateView.visibility = View.GONE
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.emptyStateView.visibility = View.VISIBLE
                        binding.emptyStateView.setupEmptyState(
                            R.drawable.ic_search,
                            "Search Error",
                            resource.message ?: "Failed to perform search"
                        )
                    }
                }
            }
        }
    }

    private fun toggleLike(post: com.campusconnect.domain.model.Post) {
        lifecycleScope.launch {
            postRepository.toggleLike(post.postId).collect { resource ->
                if (resource is Resource.Success) {
                    val isLiked = resource.data
                    val updated = adapter.currentList.map { p ->
                        if (p.postId == post.postId) {
                            p.copy(
                                isLikedByCurrentUser = isLiked,
                                likeCount = if (isLiked) p.likeCount + 1 else p.likeCount - 1
                            )
                        } else p
                    }
                    adapter.submitList(updated)
                }
            }
        }
    }

    private fun showComments(postId: String) {
        // Navigate to comments bottom sheet or dialog if implemented, else detail
        val bundle = Bundle().apply { putString("postId", postId) }
        findNavController().navigate(R.id.noteDetailFragment, bundle)
    }

    private fun sharePost(post: com.campusconnect.domain.model.Post) {
        val intent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, "${post.authorName} posted: ${post.caption}")
        }
        startActivity(android.content.Intent.createChooser(intent, "Share post via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
