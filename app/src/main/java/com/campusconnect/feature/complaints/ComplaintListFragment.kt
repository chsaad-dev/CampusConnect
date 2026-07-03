package com.campusconnect.feature.complaints

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
import com.campusconnect.databinding.FragmentComplaintListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ComplaintListFragment : Fragment() {

    private var _binding: FragmentComplaintListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ComplaintViewModel by viewModels()
    private lateinit var adapter: ComplaintAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComplaintListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadStudentComplaints()
    }

    private fun setupRecyclerView() {
        adapter = ComplaintAdapter { complaint ->
            val bundle = Bundle().apply {
                putString("complaintId", complaint.complaintId)
            }
            findNavController().navigate(R.id.action_complaintListFragment_to_complaintDetailFragment, bundle)
        }
        binding.rvComplaints.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComplaints.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAddComplaint.setOnClickListener {
            findNavController().navigate(R.id.action_complaintListFragment_to_submitComplaintFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.complaintsList.collectLatest { state ->
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
                                binding.rvComplaints.hide()
                            } else {
                                binding.emptyState.hide()
                                binding.rvComplaints.show()
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
