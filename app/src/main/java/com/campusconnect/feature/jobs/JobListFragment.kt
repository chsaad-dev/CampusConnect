package com.campusconnect.feature.jobs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.campusconnect.databinding.FragmentJobListBinding
import com.campusconnect.domain.model.Job
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JobListFragment : Fragment() {

    private var _binding: FragmentJobListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JobViewModel by viewModels()
    private lateinit var adapter: JobAdapter

    private var originalJobsList: List<Job> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadJobs()
        }

        binding.emptyState.setupEmptyState(
            iconRes = R.drawable.ic_briefcase,
            title = "No jobs posted yet",
            description = "There are no campus jobs or internships available at the moment. Please check back later."
        )

        setupRecyclerView()
        setupFilters()
        observeViewModel()

        viewModel.loadJobs()
    }

    private fun setupRecyclerView() {
        adapter = JobAdapter { job ->
            val bundle = Bundle().apply {
                putString("jobId", job.jobId)
            }
            findNavController().navigate(R.id.action_jobListFragment_to_jobDetailFragment, bundle)
        }
        binding.rvJobs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJobs.adapter = adapter
    }

    private fun setupFilters() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupSkills.setOnCheckedStateChangeListener { _, _ ->
            applyFilters()
        }
    }

    private fun applyFilters() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val checkedChipId = binding.chipGroupSkills.checkedChipId

        val selectedSkill = when (checkedChipId) {
            R.id.chip_kotlin -> "kotlin"
            R.id.chip_java -> "java"
            R.id.chip_python -> "python"
            R.id.chip_design -> "design"
            else -> ""
        }

        val filteredList = originalJobsList.filter { job ->
            val matchesQuery = job.title.lowercase().contains(query) ||
                    job.companyName.lowercase().contains(query)
            
            val matchesSkill = selectedSkill.isEmpty() ||
                    job.skillsRequired.any { it.lowercase() == selectedSkill }

            matchesQuery && matchesSkill
        }

        adapter.submitList(filteredList)
        if (filteredList.isEmpty()) {
            binding.emptyState.show()
        } else {
            binding.emptyState.hide()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.jobsList.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            if (!binding.swipeRefresh.isRefreshing) {
                                binding.progressBar.show()
                            }
                            binding.emptyState.hide()
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            binding.swipeRefresh.isRefreshing = false
                            originalJobsList = state.data
                            applyFilters()

                            if (originalJobsList.isEmpty()) {
                                binding.emptyState.show()
                                binding.rvJobs.hide()
                            } else {
                                binding.emptyState.hide()
                                binding.rvJobs.show()
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.hide()
                            binding.swipeRefresh.isRefreshing = false
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
