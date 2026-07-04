package com.campusconnect.feature.chat

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
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.databinding.FragmentChatListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        viewModel.loadChats()
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter(
            currentUid = viewModel.currentUid,
            onChatClick = { chat, otherUid, otherName ->
                navigateToChat(otherUid, otherName)
            }
        )
        binding.rvChats.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChats.adapter = adapter

        binding.emptyState.setupEmptyState(
            iconRes = R.drawable.ic_chat,
            title = "No conversations yet",
            description = "Start chatting with a friend by searching for them.",
            actionText = "Find Friends",
            actionListener = {
                findNavController().navigate(R.id.friendsFragment)
            }
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chats.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                            binding.emptyState.hide()
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            val chatsList = state.data
                            adapter.submitList(chatsList)

                            if (chatsList.isEmpty()) {
                                binding.emptyState.show()
                                binding.rvChats.hide()
                            } else {
                                binding.emptyState.hide()
                                binding.rvChats.show()
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.hide()
                            binding.emptyState.show()
                        }
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
        findNavController().navigate(R.id.action_chatListFragment_to_chatFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
