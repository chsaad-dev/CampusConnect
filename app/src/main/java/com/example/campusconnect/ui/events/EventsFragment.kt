package com.example.campusconnect.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.campusconnect.databinding.FragmentEventsBinding
import com.example.campusconnect.ui.adapter.EventAdapter
import com.example.campusconnect.ui.viewmodel.EventViewModel
import com.example.campusconnect.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventsFragment : Fragment() {
    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventViewModel by viewModels()
    private lateinit var adapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        binding.fabCreateEvent.setOnClickListener {
            Toast.makeText(context, "Post event feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        viewModel.fetchEvents()
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter(
            onEventClick = { event ->
                Toast.makeText(context, "Clicked: ${event.title}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvEvents.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.events.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { }
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
