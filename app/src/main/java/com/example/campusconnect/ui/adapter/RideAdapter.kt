package com.example.campusconnect.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.data.model.Ride
import com.example.campusconnect.databinding.ItemRideBinding
import com.example.campusconnect.util.RideUtils

class RideAdapter(
    private val onJoinClick: (Ride) -> Unit
) : ListAdapter<Ride, RideAdapter.RideViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val binding = ItemRideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RideViewHolder(private val binding: ItemRideBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ride: Ride) {
            binding.apply {
                tvRideFrom.text = "From: ${ride.fromLocation}"
                tvRideTo.text = "To: ${ride.toLocation}"
                tvRideCost.text = "Rs. ${ride.costPerSeat}"
                tvRideTime.text = ride.departureTime
                tvSeatsAvailable.text = "${ride.availableSeats} Seats Left"
                tvEta.text = "ETA: ${RideUtils.calculateETA(ride.fromLocation, ride.toLocation)}"

                btnJoinRide.setOnClickListener { onJoinClick(ride) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Ride>() {
        override fun areItemsTheSame(oldItem: Ride, newItem: Ride): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Ride, newItem: Ride): Boolean {
            return oldItem == newItem
        }
    }
}
