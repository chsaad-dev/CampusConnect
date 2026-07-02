package com.example.campusconnect.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.campusconnect.databinding.FragmentNotesBinding
import com.example.campusconnect.ui.adapter.NoteAdapter
import com.example.campusconnect.ui.viewmodel.NotesViewModel
import com.example.campusconnect.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotesFragment : Fragment() {
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotesViewModel by viewModels()
    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observeViewModel()

        binding.fabUpload.setOnClickListener {
            Toast.makeText(context, "Upload feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        viewModel.fetchNotes("Computer Science", "4") // Hardcoded for demo
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onNoteClick = { note ->
                viewModel.downloadNote(note)
                Toast.makeText(context, "Downloading ${note.title}...", Toast.LENGTH_SHORT).show()
            },
            onBookmarkClick = { note ->
                Toast.makeText(context, "Bookmarked ${note.title}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvNotes.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchLayout.editText?.addTextChangedListener { text ->
            val query = text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchNotes(query)
            } else {
                viewModel.fetchNotes("Computer Science", "4")
            }
        }
    }

    private fun observeViewModel() {
        viewModel.notes.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show progress
                }
                is Resource.Success -> {
                    adapter.submitList(resource.data)
                }
                is Resource.Error -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
