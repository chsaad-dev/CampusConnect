package com.example.campusconnect.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.campusconnect.R
import com.example.campusconnect.databinding.FragmentChatBinding
import com.example.campusconnect.ui.adapter.ChatAdapter
import com.example.campusconnect.ui.viewmodel.ChatViewModel
import com.example.campusconnect.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        binding.fabNewChat.setOnClickListener {
            showNewChatDialog()
        }

        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            viewModel.fetchChats(currentUserId)
        }
    }

    private fun showNewChatDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_new_chat, null)
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<android.widget.Button>(R.id.btn_start_chat).setOnClickListener {
            val identifier = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_user_identifier).text.toString()
            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (identifier.isNotEmpty() && currentUserId != null) {
                viewModel.startNewChat(identifier, currentUserId)
                Toast.makeText(context, "Chat started with $identifier", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(
            onChatClick = { chat ->
                Toast.makeText(context, "Open chat: ${chat.id}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvChats.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.chats.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    adapter.submitList(resource.data)
                }
                is Resource.Error -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
