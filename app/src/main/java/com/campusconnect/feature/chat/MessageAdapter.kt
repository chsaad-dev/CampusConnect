package com.campusconnect.feature.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.campusconnect.databinding.ItemMessageReceivedBinding
import com.campusconnect.databinding.ItemMessageSentBinding
import com.campusconnect.domain.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val currentUid: String
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == currentUid) TYPE_SENT else TYPE_RECEIVED
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is SentViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedViewHolder) {
            holder.bind(message)
        }
    }

    inner class SentViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            if (message.text.isNotEmpty()) {
                binding.tvMessage.text = message.text
                binding.tvMessage.visibility = View.VISIBLE
            } else {
                binding.tvMessage.visibility = View.GONE
            }

            if (message.mediaUrl.isNotEmpty() && message.mediaType == "image") {
                binding.ivImage.visibility = View.VISIBLE
                Glide.with(binding.ivImage.context)
                    .load(message.mediaUrl)
                    .into(binding.ivImage)
            } else {
                binding.ivImage.visibility = View.GONE
            }

            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            binding.tvTime.text = sdf.format(Date(message.createdAt))

            // Check if seen
            val isSeen = message.seenBy.size > 1 // seen by current user + recipient
            binding.tvSeenStatus.text = if (isSeen) "Seen" else "Sent"
        }
    }

    inner class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            if (message.text.isNotEmpty()) {
                binding.tvMessage.text = message.text
                binding.tvMessage.visibility = View.VISIBLE
            } else {
                binding.tvMessage.visibility = View.GONE
            }

            if (message.mediaUrl.isNotEmpty() && message.mediaType == "image") {
                binding.ivImage.visibility = View.VISIBLE
                Glide.with(binding.ivImage.context)
                    .load(message.mediaUrl)
                    .into(binding.ivImage)
            } else {
                binding.ivImage.visibility = View.GONE
            }

            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            binding.tvTime.text = sdf.format(Date(message.createdAt))
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message) =
            oldItem.messageId == newItem.messageId

        override fun areContentsTheSame(oldItem: Message, newItem: Message) =
            oldItem == newItem
    }
}
