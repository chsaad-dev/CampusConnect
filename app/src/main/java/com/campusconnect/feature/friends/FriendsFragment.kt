package com.campusconnect.feature.friends

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.databinding.FragmentFriendsBinding
import com.campusconnect.domain.model.User
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendsViewModel by viewModels()

    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var requestsAdapter: FriendRequestsAdapter
    private lateinit var searchAdapter: SearchUsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupTabLayout()
        setupSearchInput()
        observeViewModel()

        // Load initial tab (Friends)
        viewModel.loadFriends()
    }

    private fun setupRecyclerViews() {
        // Friends Adapter
        friendsAdapter = FriendsAdapter { friend ->
            navigateToChat(friend.uid, friend.name)
        }
        binding.rvFriends.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriends.adapter = friendsAdapter

        // Requests Adapter
        requestsAdapter = FriendRequestsAdapter(
            onAcceptClick = { request -> viewModel.acceptFriendRequest(request) },
            onRejectClick = { request -> viewModel.rejectFriendRequest(request) }
        )
        binding.rvRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRequests.adapter = requestsAdapter

        // Search Adapter
        searchAdapter = SearchUsersAdapter(
            onSendRequestClick = { user -> viewModel.sendFriendRequest(user) },
            onFriendClick = { user -> navigateToChat(user.uid, user.name) }
        )
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = searchAdapter
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // Friends
                        binding.containerFriends.show()
                        binding.containerRequests.hide()
                        binding.containerSearch.hide()
                        binding.layoutSearch.hide()
                        viewModel.loadFriends()
                    }
                    1 -> {
                        // Requests
                        binding.containerFriends.hide()
                        binding.containerRequests.show()
                        binding.containerSearch.hide()
                        binding.layoutSearch.hide()
                        viewModel.loadPendingRequests()
                    }
                    2 -> {
                        // Search
                        binding.containerFriends.hide()
                        binding.containerRequests.hide()
                        binding.containerSearch.show()
                        binding.layoutSearch.show()
                        val currentQuery = binding.etSearch.text.toString().trim()
                        if (currentQuery.isNotEmpty()) {
                            viewModel.searchUsers(currentQuery)
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearchInput() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                viewModel.searchUsers(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe friends list
                launch {
                    viewModel.friendsList.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> binding.progressBar.show()
                            is Resource.Success -> {
                                binding.progressBar.hide()
                                val list = state.data
                                friendsAdapter.submitList(list)
                                if (list.isEmpty()) {
                                    binding.emptyStateFriends.show()
                                } else {
                                    binding.emptyStateFriends.hide()
                                }
                            }
                            is Resource.Error -> binding.progressBar.hide()
                        }
                    }
                }

                // Observe pending requests
                launch {
                    viewModel.pendingRequests.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> binding.progressBar.show()
                            is Resource.Success -> {
                                binding.progressBar.hide()
                                val list = state.data
                                requestsAdapter.submitList(list)
                                if (list.isEmpty()) {
                                    binding.emptyStateRequests.show()
                                } else {
                                    binding.emptyStateRequests.hide()
                                }
                            }
                            is Resource.Error -> binding.progressBar.hide()
                        }
                    }
                }

                // Observe search results
                launch {
                    viewModel.searchResults.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> binding.progressBar.show()
                            is Resource.Success -> {
                                binding.progressBar.hide()
                                val list = state.data
                                searchAdapter.submitList(list)
                                if (list.isEmpty() && binding.etSearch.text.toString().trim().isNotEmpty()) {
                                    binding.emptyStateSearch.show()
                                } else {
                                    binding.emptyStateSearch.hide()
                                }
                            }
                            is Resource.Error -> binding.progressBar.hide()
                        }
                    }
                }

                // Observe user connection statuses
                launch {
                    viewModel.statuses.collectLatest { statusesMap ->
                        searchAdapter.setStatuses(statusesMap)
                    }
                }
            }
        }
    }

    private fun navigateToChat(targetUid: String, targetName: String) {
        val bundle = Bundle().apply {
            putString("targetUid", targetUid)
            putString("targetName", targetName)
        }
        findNavController().navigate(R.id.action_friendsFragment_to_chatFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
