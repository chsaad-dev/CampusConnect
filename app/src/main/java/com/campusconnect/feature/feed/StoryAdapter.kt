package com.campusconnect.feature.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.campusconnect.databinding.ItemStoryBinding
import com.campusconnect.domain.model.User

class StoryAdapter(
    private val onYourStoryClick: () -> Unit,
    private val onAuthorStoryClick: (User) -> Unit
) : ListAdapter<User, StoryAdapter.StoryViewHolder>(StoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(getItem(position), position == 0)
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, isCurrentUserSlot: Boolean) {
            binding.apply {
                if (isCurrentUserSlot) {
                    ivAddBadge.visibility = View.VISIBLE
                    viewRing.visibility = View.GONE
                    viewMask.visibility = View.GONE
                    tvStoryName.text = "You"
                    viewAvatar.loadAvatar(user.photoUrl, user.name)
                    root.setOnClickListener { onYourStoryClick() }
                } else {
                    ivAddBadge.visibility = View.GONE
                    viewRing.visibility = View.VISIBLE
                    viewMask.visibility = View.VISIBLE
                    
                    // Limit name to 4 letters
                    val firstName = user.name.split("\\s+".toRegex()).firstOrNull() ?: user.name
                    tvStoryName.text = if (firstName.length > 4) firstName.take(4) else firstName

                    viewAvatar.loadAvatar(user.photoUrl, user.name)
                    root.setOnClickListener { onAuthorStoryClick(user) }
                }
            }
        }
    }
}

class StoryDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.uid == newItem.uid
    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
}
