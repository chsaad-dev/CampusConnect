package com.campusconnect.feature.jobs

import android.content.Intent
import android.net.Uri
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
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.core.common.showErrorSnackbar
import com.campusconnect.databinding.FragmentJobDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class JobDetailFragment : Fragment() {

    private var _binding: FragmentJobDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JobViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val jobId = arguments?.getString("jobId")
        if (jobId == null) {
            showErrorSnackbar("Invalid Job ID")
            findNavController().navigateUp()
            return
        }

        setupObserver()
        viewModel.loadJobDetail(jobId)
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.jobDetail.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                            binding.contentCard.hide()
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            binding.contentCard.show()

                            val job = state.data
                            binding.tvTitle.text = job.title
                            binding.tvCompany.text = job.companyName
                            binding.tvType.text = "Type: ${job.type.replaceFirstChar { it.uppercase() }}"
                            binding.tvSkills.text = job.skillsRequired.joinToString(", ")
                            binding.tvDescription.text = job.description

                            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            binding.tvDeadline.text = "Application Deadline: ${sdf.format(Date(job.deadline))}"

                            binding.btnApply.setOnClickListener {
                                if (job.applyLink.isNotEmpty()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job.applyLink))
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        showErrorSnackbar("Invalid application link.")
                                    }
                                } else {
                                    showErrorSnackbar("Application link not available.")
                                }
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.hide()
                            showErrorSnackbar(state.message)
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
