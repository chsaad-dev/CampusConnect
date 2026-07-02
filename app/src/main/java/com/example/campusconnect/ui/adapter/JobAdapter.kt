package com.example.campusconnect.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.data.model.Job
import com.example.campusconnect.databinding.ItemJobBinding

class JobAdapter(
    private val onApplyClick: (Job) -> Unit
) : ListAdapter<Job, JobAdapter.JobViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class JobViewHolder(private val binding: ItemJobBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(job: Job) {
            binding.apply {
                tvJobTitle.text = job.title
                tvCompanyName.text = job.companyName
                tvJobLocation.text = job.location
                tvJobType.text = job.type

                btnApply.setOnClickListener { onApplyClick(job) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }
    }
}
