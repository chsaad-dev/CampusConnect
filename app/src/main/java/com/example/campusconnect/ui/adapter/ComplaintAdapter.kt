package com.example.campusconnect.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.data.model.Complaint
import com.example.campusconnect.databinding.ItemComplaintBinding
import java.text.SimpleDateFormat
import java.util.*

class ComplaintAdapter(
    private val onComplaintClick: (Complaint) -> Unit
) : ListAdapter<Complaint, ComplaintAdapter.ComplaintViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val binding = ItemComplaintBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ComplaintViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ComplaintViewHolder(private val binding: ItemComplaintBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(complaint: Complaint) {
            binding.apply {
                tvComplaintTitle.text = complaint.category
                tvComplaintDesc.text = complaint.description
                tvComplaintStatus.text = complaint.status.uppercase()
                
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                tvComplaintDate.text = "Posted on ${sdf.format(Date(complaint.timestamp))}"

                root.setOnClickListener { onComplaintClick(complaint) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Complaint>() {
        override fun areItemsTheSame(oldItem: Complaint, newItem: Complaint): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Complaint, newItem: Complaint): Boolean {
            return oldItem == newItem
        }
    }
}
