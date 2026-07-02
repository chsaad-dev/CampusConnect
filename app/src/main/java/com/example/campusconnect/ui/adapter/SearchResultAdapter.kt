package com.example.campusconnect.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.campusconnect.data.repository.SearchResult
import com.example.campusconnect.databinding.ItemEventBinding
import com.example.campusconnect.databinding.ItemJobBinding
import com.example.campusconnect.databinding.ItemNoteBinding

class SearchResultAdapter(
    private val onClick: (SearchResult) -> Unit
) : ListAdapter<SearchResult, SearchResultAdapter.SearchViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SearchResult.NoteResult -> 0
            is SearchResult.JobResult -> 1
            is SearchResult.EventResult -> 2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ViewBinding = when (viewType) {
            0 -> ItemNoteBinding.inflate(inflater, parent, false)
            1 -> ItemJobBinding.inflate(inflater, parent, false)
            2 -> ItemEventBinding.inflate(inflater, parent, false)
            else -> throw IllegalArgumentException("Invalid view type")
        }
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SearchViewHolder(private val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: SearchResult) {
            when (result) {
                is SearchResult.NoteResult -> {
                    val b = binding as ItemNoteBinding
                    b.tvNoteSubject.text = result.note.subject
                    b.tvNoteTeacher.text = result.note.teacherName
                }
                is SearchResult.JobResult -> {
                    val b = binding as ItemJobBinding
                    b.tvJobTitle.text = result.job.title
                    b.tvCompanyName.text = result.job.companyName
                }
                is SearchResult.EventResult -> {
                    val b = binding as ItemEventBinding
                    b.tvEventTitle.text = result.event.title
                    b.tvEventLocation.text = result.event.location
                }
            }
            binding.root.setOnClickListener { onClick(result) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem == newItem
        }
    }
}
