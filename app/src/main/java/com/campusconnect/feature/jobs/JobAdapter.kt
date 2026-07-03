package com.campusconnect.feature.jobs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.campusconnect.databinding.ItemJobBinding
import com.campusconnect.domain.model.Job
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JobAdapter(
    private val onJobClick: (Job) -> Unit
) : ListAdapter<Job, JobAdapter.JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemJobBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class JobViewHolder(
        private val binding: ItemJobBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Job) {
            binding.tvTitle.text = job.title
            binding.tvCompany.text = job.companyName
            binding.tvType.text = job.type.replaceFirstChar { it.uppercase() }

            binding.tvSkills.text = "Skills: ${job.skillsRequired.joinToString(", ")}"

            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDeadline.text = "Apply by: ${sdf.format(Date(job.deadline))}"

            binding.root.setOnClickListener {
                onJobClick(job)
            }
        }
    }

    class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job) =
            oldItem.jobId == newItem.jobId

        override fun areContentsTheSame(oldItem: Job, newItem: Job) =
            oldItem == newItem
    }
}
