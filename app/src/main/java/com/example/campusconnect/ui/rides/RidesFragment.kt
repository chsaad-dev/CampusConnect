package com.example.campusconnect.ui.rides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.campusconnect.databinding.FragmentRidesBinding
import com.example.campusconnect.ui.adapter.RideAdapter
import com.example.campusconnect.ui.viewmodel.RideViewModel
import com.example.campusconnect.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RidesFragment : Fragment() {
    private var _binding: FragmentRidesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RideViewModel by viewModels()
    private lateinit var adapter: RideAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRidesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        binding.fabOfferRide.setOnClickListener {
            // TODO: Navigate to Create Ride
            Toast.makeText(context, "Offer Ride feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        viewModel.fetchRides()
    }

    private fun setupRecyclerView() {
        adapter = RideAdapter(
            onJoinClick = { ride ->
                viewModel.joinRide(ride.id, "current_user_id") // TODO: Get actual user ID
            }
        )
        binding.rvRides.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.rides.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show loader
                }
                is Resource.Success -> {
                    adapter.submitList(resource.data)
                }
                is Resource.Error -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.actionStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(context, resource.data, Toast.LENGTH_SHORT).show()
                    viewModel.fetchRides() // Refresh
                }
                is Resource.Error -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
