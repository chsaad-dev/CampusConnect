package com.example.campusconnect.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.data.model.Event
import com.example.campusconnect.databinding.ItemEventBinding

class EventAdapter(
    private val onEventClick: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.apply {
                tvEventTitle.text = event.title
                tvEventDate.text = "${event.date} • ${event.time}"
                tvEventLocation.text = event.location
                
                // Glide.with(root).load(event.imageUrl).into(ivEventImage)
                
                root.setOnClickListener { onEventClick(event) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}
