package com.campusconnect.feature.chat

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.core.common.showErrorSnackbar
import com.campusconnect.databinding.FragmentChatBinding
import com.campusconnect.domain.model.Chat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: MessageAdapter

    private var targetUid: String? = null
    private var targetName: String? = null
    private var activeChatId: String? = null

    // Typing Debounce
    private val typingHandler = Handler(Looper.getMainLooper())
    private var isTyping = false
    private val typingRunnable = Runnable {
        activeChatId?.let {
            viewModel.updateTypingStatus(it, false)
            isTyping = false
        }
    }

    // Media Picker Launcher
    private val mediaPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && activeChatId != null) {
            viewModel.sendMessage(activeChatId!!, "", uri, "image")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        targetUid = arguments?.getString("targetUid")
        targetName = arguments?.getString("targetName")

        if (targetUid == null) {
            showErrorSnackbar("Invalid chat participant")
            findNavController().navigateUp()
            return
        }

        binding.toolbar.title = targetName ?: "Chat"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        setupMessageInput()
        observeViewModel()

        viewModel.getOrCreateChat(targetUid!!)
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(viewModel.currentUid)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true // Start showing messages from the bottom
        binding.rvMessages.layoutManager = layoutManager
        binding.rvMessages.adapter = adapter
    }

    private fun setupMessageInput() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty() && activeChatId != null) {
                viewModel.sendMessage(activeChatId!!, text, null, "none")
                binding.etMessage.text?.clear()
                // Stop typing immediately
                typingHandler.removeCallbacks(typingRunnable)
                typingRunnable.run()
            }
        }

        binding.btnAttach.setOnClickListener {
            mediaPickerLauncher.launch("image/*")
        }

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (activeChatId == null) return
                if (!isTyping) {
                    isTyping = true
                    viewModel.updateTypingStatus(activeChatId!!, true)
                }
                typingHandler.removeCallbacks(typingRunnable)
                typingHandler.postDelayed(typingRunnable, 1500)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. Observe Chat Object Retrieval/Creation
                launch {
                    viewModel.currentChat.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.progressBar.show()
                            }
                            is Resource.Success -> {
                                binding.progressBar.hide()
                                val chat = state.data
                                activeChatId = chat.chatId
                                
                                val otherUid = chat.participants.firstOrNull { it != viewModel.currentUid } ?: ""
                                val resolvedName = chat.participantNames[otherUid] ?: targetName ?: "Chat"
                                binding.toolbar.title = resolvedName
                                targetName = resolvedName

                                viewModel.loadMessages(chat.chatId)
                                viewModel.listenToTypingStatus(chat.chatId)
                                viewModel.observeOtherUserPresence(otherUid)
                            }
                            is Resource.Error -> {
                                binding.progressBar.hide()
                                showErrorSnackbar(state.message)
                            }
                            null -> {}
                        }
                    }
                }

                // 2. Observe Messages flow
                launch {
                    viewModel.messages.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                val messagesList = state.data
                                adapter.submitList(messagesList) {
                                    // Auto scroll to bottom
                                    if (messagesList.isNotEmpty()) {
                                        binding.rvMessages.scrollToPosition(messagesList.size - 1)
                                    }
                                }
                            }
                            is Resource.Error -> {
                                showErrorSnackbar(state.message)
                            }
                        }
                    }
                }

                // 3. Observe Typing Indicator
                launch {
                    viewModel.typingStatus.collectLatest { typingMap ->
                        val isOtherTyping = typingMap.entries.firstOrNull { it.key != viewModel.currentUid }?.value ?: false
                        if (isOtherTyping) {
                            binding.tvTyping.text = "$targetName is typing..."
                            binding.tvTyping.show()
                        } else {
                            binding.tvTyping.hide()
                        }
                    }
                }

                // 4. Observe Other User Presence (Online/Offline status subtitle)
                launch {
                    viewModel.otherUserPresence.collectLatest { user ->
                        if (user != null) {
                            binding.toolbar.subtitle = if (user.isOnline) "Online" else "Offline"
                        } else {
                            binding.toolbar.subtitle = null
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Reset typing status on exit
        activeChatId?.let {
            viewModel.updateTypingStatus(it, false)
        }
        typingHandler.removeCallbacks(typingRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetChatState()
        _binding = null
    }
}
