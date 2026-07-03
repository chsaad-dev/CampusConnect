package com.campusconnect.feature.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.campusconnect.databinding.ItemEventBinding
import com.campusconnect.domain.model.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(
    private val onEventClick: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(
        private val binding: ItemEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.tvTitle.text = event.title
            binding.tvHost.text = "Hosted by ${event.hostType}"
            binding.tvLocation.text = event.location

            val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
            binding.tvDate.text = sdf.format(Date(event.date))

            Glide.with(binding.ivBanner.context)
                .load(event.bannerUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivBanner)

            binding.root.setOnClickListener {
                onEventClick(event)
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event) =
            oldItem.eventId == newItem.eventId

        override fun areContentsTheSame(oldItem: Event, newItem: Event) =
            oldItem == newItem
    }
}
