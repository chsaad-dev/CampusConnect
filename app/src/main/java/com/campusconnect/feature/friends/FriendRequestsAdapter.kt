package com.campusconnect.feature.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.campusconnect.databinding.ItemFriendRequestBinding
import com.campusconnect.domain.model.FriendRequest

class FriendRequestsAdapter(
    private val onAcceptClick: (FriendRequest) -> Unit,
    private val onRejectClick: (FriendRequest) -> Unit
) : ListAdapter<FriendRequest, FriendRequestsAdapter.RequestViewHolder>(RequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemFriendRequestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RequestViewHolder(
        private val binding: ItemFriendRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(request: FriendRequest) {
            binding.tvName.text = request.fromName
            binding.tvUsername.text = "@${request.fromUsername}"

            Glide.with(binding.ivAvatar.context)
                .load(request.fromPhotoUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivAvatar)

            binding.btnAccept.setOnClickListener {
                onAcceptClick(request)
            }

            binding.btnReject.setOnClickListener {
                onRejectClick(request)
            }
        }
    }

    class RequestDiffCallback : DiffUtil.ItemCallback<FriendRequest>() {
        override fun areItemsTheSame(oldItem: FriendRequest, newItem: FriendRequest) =
            oldItem.requestId == newItem.requestId

        override fun areContentsTheSame(oldItem: FriendRequest, newItem: FriendRequest) =
            oldItem == newItem
    }
}
