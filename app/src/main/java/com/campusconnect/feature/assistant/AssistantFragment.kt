package com.campusconnect.feature.assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.core.network.GeminiClient
import com.campusconnect.databinding.FragmentAssistantBinding
import com.campusconnect.databinding.ItemMessageReceivedBinding
import com.campusconnect.databinding.ItemMessageSentBinding
import com.campusconnect.domain.repository.AssistantRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AssistantMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@AndroidEntryPoint
class AssistantFragment : Fragment() {

    private var _binding: FragmentAssistantBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var assistantRepository: AssistantRepository

    private val messages = mutableListOf<AssistantMessage>()
    private lateinit var adapter: AssistantMessageAdapter
    private var campusFactsText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        setupListeners()
        loadCampusFacts()

        // Insert initial assistant greeting
        if (messages.isEmpty()) {
            addMessage(AssistantMessage("Hello! I am your Campus Assistant. How can I help you today?", isUser = false))
        }
    }

    private fun setupRecyclerView() {
        adapter = AssistantMessageAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            val userText = binding.etMessage.text.toString().trim()
            if (userText.isEmpty()) return@setOnClickListener
            
            binding.etMessage.text?.clear()
            sendMessageToBot(userText)
        }
    }

    private fun loadCampusFacts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                assistantRepository.getCampusFacts().collectLatest { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            val facts = resource.data
                            campusFactsText = facts.joinToString("\n") { (topic, content) ->
                                "- Topic: $topic, Content: $content"
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.hide()
                            // Fallback to empty context
                            campusFactsText = ""
                        }
                    }
                }
            }
        }
    }

    private fun addMessage(msg: AssistantMessage) {
        messages.add(msg)
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvMessages.smoothScrollToPosition(messages.size - 1)
    }

    private fun sendMessageToBot(question: String) {
        addMessage(AssistantMessage(question, isUser = true))

        binding.tvTyping.show()
        binding.btnSend.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val prompt = """
                    You are CampusConnect's assistant for university students.
                    Known facts:
                    $campusFactsText
                    Answer briefly and only using the facts above. If unknown, say to check with administration.
                    Student question: $question
                """.trimIndent()

                val answer = GeminiClient.withBackoff {
                    GeminiClient.generateContent(prompt)
                }

                binding.tvTyping.hide()
                binding.btnSend.isEnabled = true
                addMessage(AssistantMessage(answer.trim(), isUser = false))
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvTyping.hide()
                binding.btnSend.isEnabled = true
                addMessage(AssistantMessage("I encountered an issue connecting. Please check with administration or try again later.", isUser = false))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner adapter class to bind standard sent/received bubbles
    class AssistantMessageAdapter(
        private val messageList: List<AssistantMessage>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val TYPE_SENT = 1
            private const val TYPE_RECEIVED = 2
        }

        override fun getItemViewType(position: Int): Int {
            return if (messageList[position].isUser) TYPE_SENT else TYPE_RECEIVED
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_SENT) {
                val binding = ItemMessageSentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SentViewHolder(binding)
            } else {
                val binding = ItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ReceivedViewHolder(binding)
            }
        }

        override fun getItemCount(): Int = messageList.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val msg = messageList[position]
            if (holder is SentViewHolder) {
                holder.bind(msg)
            } else if (holder is ReceivedViewHolder) {
                holder.bind(msg)
            }
        }

        class SentViewHolder(private val binding: ItemMessageSentBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(msg: AssistantMessage) {
                binding.tvMessage.text = msg.text
                binding.tvMessage.show()
                binding.ivImage.hide()
                
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                binding.tvTime.text = sdf.format(Date(msg.timestamp))
                binding.tvSeenStatus.text = "Sent"
            }
        }

        class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(msg: AssistantMessage) {
                binding.tvMessage.text = msg.text
                binding.tvMessage.show()
                binding.ivImage.hide()
                
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                binding.tvTime.text = sdf.format(Date(msg.timestamp))
            }
        }
    }
}
