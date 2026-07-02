package com.example.campusconnect.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.data.model.Note
import com.example.campusconnect.databinding.ItemNoteBinding

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onBookmarkClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.apply {
                tvNoteSubject.text = note.subject
                tvNoteTeacher.text = "By ${note.teacherName}"
                tvDownloadCount.text = "${note.downloadsCount} Downloads"
                tvFileSize.text = note.fileSize

                root.setOnClickListener { onNoteClick(note) }
                ivBookmark.setOnClickListener { onBookmarkClick(note) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
