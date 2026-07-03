package com.campusconnect.feature.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.campusconnect.R
import com.campusconnect.databinding.ItemChatBinding
import com.campusconnect.domain.model.Chat
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private val currentUid: String,
    private val onChatClick: (Chat, String, String) -> Unit
) : ListAdapter<Chat, ChatListAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(
        private val binding: ItemChatBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            // Find the other participant's UID
            val otherUid = chat.participants.firstOrNull { it != currentUid } ?: ""
            val otherName = chat.participantNames[otherUid] ?: "User"
            val otherUsername = chat.participantUsernames[otherUid] ?: "user"
            val otherPhotoUrl = chat.participantPhotoUrls[otherUid] ?: ""

            binding.tvName.text = otherName
            binding.tvLastMessage.text = chat.lastMessage.ifEmpty { "No messages yet" }
            
            if (chat.lastMessageAt > 0) {
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                binding.tvTime.text = sdf.format(Date(chat.lastMessageAt))
            } else {
                binding.tvTime.text = ""
            }

            Glide.with(binding.ivAvatar.context)
                .load(otherPhotoUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivAvatar)

            binding.root.setOnClickListener {
                onChatClick(chat, otherUid, otherName)
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat) = oldItem.chatId == newItem.chatId
        override fun areContentsTheSame(oldItem: Chat, newItem: Chat) = oldItem == newItem
    }
}
