package com.campusconnect.feature.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.databinding.FragmentEventListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventListFragment : Fragment() {

    private var _binding: FragmentEventListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventViewModel by viewModels()
    private lateinit var adapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        observeViewModel()

        viewModel.loadEvents()
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter { event ->
            val bundle = Bundle().apply {
                putString("eventId", event.eventId)
            }
            findNavController().navigate(R.id.action_eventListFragment_to_eventDetailFragment, bundle)
        }
        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEvents.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsList.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                            binding.emptyState.hide()
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            val list = state.data
                            adapter.submitList(list)

                            if (list.isEmpty()) {
                                binding.emptyState.show()
                                binding.rvEvents.hide()
                            } else {
                                binding.emptyState.hide()
                                binding.rvEvents.show()
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.hide()
                            binding.emptyState.show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
