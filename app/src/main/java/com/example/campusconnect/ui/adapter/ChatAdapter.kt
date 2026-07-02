package com.example.campusconnect.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.data.model.Chat
import com.example.campusconnect.databinding.ItemChatBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val onChatClick: (Chat) -> Unit
) : ListAdapter<Chat, ChatAdapter.ChatViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.apply {
                tvChatName.text = chat.type // Simplified, usually you'd get name from other participant
                tvLastMessage.text = chat.lastMessage
                
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                tvChatTime.text = sdf.format(Date(chat.lastMessageTimestamp))

                root.setOnClickListener { onChatClick(chat) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
}
