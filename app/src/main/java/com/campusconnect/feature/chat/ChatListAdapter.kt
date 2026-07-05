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
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivAvatar)

            (binding.root.tag as? com.google.firebase.firestore.ListenerRegistration)?.remove()

            if (otherUid.isNotBlank()) {
                val userRef = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(otherUid)

                val registration = userRef.addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val isOnline = snapshot.getBoolean("isOnline") ?: false
                        binding.viewOnlineDot.visibility = if (isOnline) android.view.View.VISIBLE else android.view.View.GONE
                    } else {
                        binding.viewOnlineDot.visibility = android.view.View.GONE
                    }
                }
                binding.root.tag = registration
            } else {
                binding.viewOnlineDot.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener {
                onChatClick(chat, otherUid, otherName)
            }
        }
    }

    override fun onViewRecycled(holder: ChatViewHolder) {
        super.onViewRecycled(holder)
        (holder.itemView.tag as? com.google.firebase.firestore.ListenerRegistration)?.remove()
        holder.itemView.tag = null
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat) = oldItem.chatId == newItem.chatId
        override fun areContentsTheSame(oldItem: Chat, newItem: Chat) = oldItem == newItem
    }
}
