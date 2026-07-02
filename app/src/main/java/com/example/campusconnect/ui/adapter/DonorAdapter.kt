package com.example.campusconnect.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.data.model.User
import com.example.campusconnect.databinding.ItemDonorBinding

class DonorAdapter(
    private val onContactClick: (User) -> Unit
) : ListAdapter<User, DonorAdapter.DonorViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorViewHolder {
        val binding = ItemDonorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DonorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DonorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DonorViewHolder(private val binding: ItemDonorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.apply {
                tvDonorName.text = user.name
                tvDonorDept.text = "${user.department} • Semester ${user.semester}"
                tvDonorBloodGroup.text = user.bloodGroup
                
                btnContactDonor.setOnClickListener { onContactClick(user) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
