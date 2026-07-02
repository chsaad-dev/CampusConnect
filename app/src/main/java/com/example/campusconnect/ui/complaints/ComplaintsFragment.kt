package com.example.campusconnect.ui.complaints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.campusconnect.databinding.FragmentComplaintsBinding
import com.example.campusconnect.ui.adapter.ComplaintAdapter
import com.example.campusconnect.ui.viewmodel.ComplaintViewModel
import com.example.campusconnect.util.Resource
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComplaintsFragment : Fragment() {
    private var _binding: FragmentComplaintsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ComplaintViewModel by viewModels()
    private lateinit var adapter: ComplaintAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComplaintsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupTabs()
        observeViewModel()

        binding.fabNewComplaint.setOnClickListener {
            Toast.makeText(context, "New complaint feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            viewModel.fetchMyComplaints(currentUserId)
        }
    }

    private fun setupRecyclerView() {
        adapter = ComplaintAdapter(
            onComplaintClick = { complaint ->
                Toast.makeText(context, "Status: ${complaint.status}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvComplaints.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabComplaints.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Filter logic
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.complaints.observe(viewLifecycleOwner) { resource ->
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
