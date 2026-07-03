package com.campusconnect.feature.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.campusconnect.databinding.ItemRecommendedNoteBinding
import com.campusconnect.domain.model.NoteDetails

class RecommendedNoteAdapter(
    private val onNoteClick: (NoteDetails) -> Unit
) : ListAdapter<NoteDetails, RecommendedNoteAdapter.RecommendedViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendedViewHolder {
        val binding = ItemRecommendedNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecommendedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecommendedViewHolder(
        private val binding: ItemRecommendedNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: NoteDetails) {
            binding.tvSubject.text = note.subject
            binding.tvTeacher.text = note.teacher
            binding.tvRating.text = "${note.rating}"

            binding.root.setOnClickListener {
                onNoteClick(note)
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<NoteDetails>() {
        override fun areItemsTheSame(oldItem: NoteDetails, newItem: NoteDetails) =
            oldItem.postId == newItem.postId

        override fun areContentsTheSame(oldItem: NoteDetails, newItem: NoteDetails) =
            oldItem == newItem
    }
}
