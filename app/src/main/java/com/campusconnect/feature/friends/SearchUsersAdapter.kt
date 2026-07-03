package com.campusconnect.feature.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.campusconnect.databinding.ItemSearchUserBinding
import com.campusconnect.domain.model.User

class SearchUsersAdapter(
    private val onSendRequestClick: (User) -> Unit,
    private val onFriendClick: (User) -> Unit
) : ListAdapter<User, SearchUsersAdapter.UserViewHolder>(UserDiffCallback()) {

    private var statuses: Map<String, String> = emptyMap()

    fun setStatuses(statuses: Map<String, String>) {
        this.statuses = statuses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemSearchUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(
        private val binding: ItemSearchUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvName.text = user.name
            binding.tvUsername.text = "@${user.uniqueUsername}"
            binding.tvDept.text = user.department

            Glide.with(binding.ivAvatar.context)
                .load(user.photoUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivAvatar)

            val status = statuses[user.uid] ?: "none"
            
            when (status) {
                "none" -> {
                    binding.btnAction.visibility = View.VISIBLE
                    binding.btnAction.isEnabled = true
                    binding.btnAction.text = "Connect"
                    binding.tvStatus.visibility = View.GONE
                }
                "pending_sent" -> {
                    binding.btnAction.visibility = View.GONE
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvStatus.text = "Requested"
                }
                "pending_received" -> {
                    binding.btnAction.visibility = View.GONE
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvStatus.text = "Pending Response"
                }
                "friends" -> {
                    binding.btnAction.visibility = View.VISIBLE
                    binding.btnAction.isEnabled = true
                    binding.btnAction.text = "Chat"
                    binding.tvStatus.visibility = View.GONE
                }
                else -> {
                    binding.btnAction.visibility = View.VISIBLE
                    binding.btnAction.isEnabled = true
                    binding.btnAction.text = "Connect"
                    binding.tvStatus.visibility = View.GONE
                }
            }

            binding.btnAction.setOnClickListener {
                if (status == "friends") {
                    onFriendClick(user)
                } else if (status == "none") {
                    onSendRequestClick(user)
                }
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.uid == newItem.uid
        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }
}
