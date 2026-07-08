package com.campusconnect.feature.complaints

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.campusconnect.databinding.ItemComplaintBinding
import com.campusconnect.domain.model.Complaint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ComplaintAdapter(
    private val onComplaintClick: (Complaint) -> Unit
) : ListAdapter<Complaint, ComplaintAdapter.ComplaintViewHolder>(ComplaintDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val binding = ItemComplaintBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ComplaintViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ComplaintViewHolder(
        private val binding: ItemComplaintBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(complaint: Complaint) {
            val categoryText = complaint.category.ifEmpty { "Other" }
            binding.tvCategory.text = categoryText.replaceFirstChar { it.uppercase() }

            val categoryChip = binding.tvCategory as? com.google.android.material.chip.Chip
            if (categoryChip != null) {
                val context = binding.root.context
                val (chipBg, chipText) = when (complaint.category.lowercase()) {
                    "wifi" -> Pair(com.campusconnect.R.color.chip_ride_bg, com.campusconnect.R.color.chip_ride_text)
                    "electrical" -> Pair(com.campusconnect.R.color.chip_lostfound_bg, com.campusconnect.R.color.chip_lostfound_text)
                    "plumbing" -> Pair(com.campusconnect.R.color.chip_blood_bg, com.campusconnect.R.color.chip_blood_text)
                    "furniture" -> Pair(com.campusconnect.R.color.chip_job_bg, com.campusconnect.R.color.chip_job_text)
                    "academic" -> Pair(com.campusconnect.R.color.chip_note_bg, com.campusconnect.R.color.chip_note_text)
                    else -> Pair(com.campusconnect.R.color.chip_event_bg, com.campusconnect.R.color.chip_event_text)
                }
                categoryChip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(context.getColor(chipBg))
                categoryChip.setTextColor(context.getColor(chipText))
                categoryChip.chipStrokeWidth = 0f
            }

            binding.tvDescription.text = complaint.description
            binding.tvPriority.text = "Priority: ${complaint.priority}"

            val statusText = when (complaint.status) {
                "submitted" -> "Submitted"
                "in_progress" -> "In Progress"
                "resolved" -> "Resolved"
                "duplicate" -> "Duplicate"
                else -> complaint.status.replaceFirstChar { it.uppercase() }
            }
            binding.chipStatus.text = statusText

            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDate.text = sdf.format(Date(complaint.createdAt))

            binding.root.setOnClickListener {
                onComplaintClick(complaint)
            }
        }
    }

    class ComplaintDiffCallback : DiffUtil.ItemCallback<Complaint>() {
        override fun areItemsTheSame(oldItem: Complaint, newItem: Complaint) =
            oldItem.complaintId == newItem.complaintId

        override fun areContentsTheSame(oldItem: Complaint, newItem: Complaint) =
            oldItem == newItem
    }
}
