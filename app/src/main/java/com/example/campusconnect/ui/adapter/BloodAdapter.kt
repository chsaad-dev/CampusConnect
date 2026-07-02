package com.example.campusconnect.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.data.model.BloodRequest
import com.example.campusconnect.databinding.ItemBloodRequestBinding

class BloodAdapter(
    private val onDonateClick: (BloodRequest) -> Unit,
    private val onContactClick: (BloodRequest) -> Unit
) : ListAdapter<BloodRequest, BloodAdapter.BloodViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloodViewHolder {
        val binding = ItemBloodRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BloodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BloodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BloodViewHolder(private val binding: ItemBloodRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(request: BloodRequest) {
            binding.apply {
                tvBloodGroup.text = request.bloodGroup
                tvUrgency.text = request.urgency
                tvRequesterName.text = "Requested by: ${request.requesterName}"
                tvHospital.text = "Hospital: ${request.hospitalName}"
                tvReason.text = "Reason: ${request.reason}"

                btnDonate.setOnClickListener { onDonateClick(request) }
                btnContact.setOnClickListener { onContactClick(request) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<BloodRequest>() {
        override fun areItemsTheSame(oldItem: BloodRequest, newItem: BloodRequest): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BloodRequest, newItem: BloodRequest): Boolean {
            return oldItem == newItem
        }
    }
}
