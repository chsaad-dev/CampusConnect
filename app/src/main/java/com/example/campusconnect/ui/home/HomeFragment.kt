package com.example.campusconnect.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.campusconnect.databinding.FragmentHomeBinding
import com.example.campusconnect.ui.adapter.EventAdapter
import com.example.campusconnect.ui.viewmodel.EventViewModel
import com.example.campusconnect.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val eventViewModel: EventViewModel by viewModels()
    private val notesViewModel: com.example.campusconnect.ui.viewmodel.NotesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupEventsRecyclerView()
        setupRecommendationsRecyclerView()
        observeViewModel()
        
        // Fetch dashboard data (Mocked for now)
        binding.tvWelcome.text = "Good Morning, Fareedi!"
        
        eventViewModel.fetchEvents()
        notesViewModel.fetchNotes("Computer Science", "4")
    }

    private fun setupEventsRecyclerView() {
        val adapter = EventAdapter { event ->
            // Navigate to event detail
        }
        binding.rvEvents.adapter = adapter
    }

    private fun setupRecommendationsRecyclerView() {
        val adapter = com.example.campusconnect.ui.adapter.NoteAdapter(
            onNoteClick = { },
            onBookmarkClick = { }
        )
        binding.rvRecommendations.adapter = adapter
    }

    private fun observeViewModel() {
        eventViewModel.events.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                (binding.rvEvents.adapter as? EventAdapter)?.submitList(resource.data)
            }
        }

        notesViewModel.notes.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                (binding.rvRecommendations.adapter as? com.example.campusconnect.ui.adapter.NoteAdapter)?.submitList(resource.data?.take(3))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
