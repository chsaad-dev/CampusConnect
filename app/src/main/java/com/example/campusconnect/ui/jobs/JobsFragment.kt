package com.example.campusconnect.ui.jobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.campusconnect.databinding.FragmentJobsBinding
import com.example.campusconnect.ui.adapter.JobAdapter
import com.example.campusconnect.ui.viewmodel.JobViewModel
import com.example.campusconnect.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JobsFragment : Fragment() {
    private var _binding: FragmentJobsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JobViewModel by viewModels()
    private lateinit var adapter: JobAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        viewModel.fetchJobs()
    }

    private fun setupRecyclerView() {
        adapter = JobAdapter(
            onApplyClick = { job ->
                Toast.makeText(context, "Applying for ${job.title}...", Toast.LENGTH_SHORT).show()
                // Navigate to Apply Fragment or open link
            }
        )
        binding.rvJobs.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.jobs.observe(viewLifecycleOwner) { resource ->
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
