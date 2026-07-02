package com.example.campusconnect.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.data.model.LostItem
import com.example.campusconnect.databinding.ItemLostFoundBinding

class LostFoundAdapter(
    private val onItemClick: (LostItem) -> Unit
) : ListAdapter<LostItem, LostFoundAdapter.LostFoundViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LostFoundViewHolder {
        val binding = ItemLostFoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LostFoundViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LostFoundViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LostFoundViewHolder(private val binding: ItemLostFoundBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LostItem) {
            binding.apply {
                tvItemTitle.text = item.title
                tvItemLocation.text = item.location
                tvItemStatus.text = item.status.uppercase()
                tvItemDate.text = item.date
                
                // Glide.with(root).load(item.imageUrl).into(ivItemImage)
                
                root.setOnClickListener { onItemClick(item) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<LostItem>() {
        override fun areItemsTheSame(oldItem: LostItem, newItem: LostItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LostItem, newItem: LostItem): Boolean {
            return oldItem == newItem
        }
    }
}
