package com.campusconnect.feature.admin

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.core.common.showErrorSnackbar
import com.campusconnect.core.common.showSnackbar
import com.campusconnect.databinding.FragmentAdminDashboardBinding
import com.campusconnect.domain.model.Complaint
import com.campusconnect.feature.complaints.ComplaintAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by viewModels()
    private lateinit var complaintsAdapter: ComplaintAdapter

    private var selectedEventBannerUri: Uri? = null
    private var eventTime: Long = 0L
    private var jobDeadline: Long = 0L

    // Event Banner Image Picker Launcher
    private val bannerPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedEventBannerUri = uri
            binding.ivBannerPreview.setImageURI(uri)
            binding.cardBannerPreview.show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.inflateMenu(com.campusconnect.R.menu.admin_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == com.campusconnect.R.id.action_scan_ticket) {
                findNavController().navigate(com.campusconnect.R.id.ticketScannerFragment)
                true
            } else {
                false
            }
        }

        setupTabLayout()
        setupComplaintsTab()
        setupEventsTab()
        setupJobsTab()
        observeViewModel()

        // Load initially active tab data (Complaints)
        viewModel.loadAllComplaints()
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // Complaints
                        binding.containerComplaints.show()
                        binding.containerCreateEvent.hide()
                        binding.containerCreateJob.hide()
                        viewModel.loadAllComplaints()
                    }
                    1 -> {
                        // Post Event
                        binding.containerComplaints.hide()
                        binding.containerCreateEvent.show()
                        binding.containerCreateJob.hide()
                    }
                    2 -> {
                        // Post Job
                        binding.containerComplaints.hide()
                        binding.containerCreateEvent.hide()
                        binding.containerCreateJob.show()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupComplaintsTab() {
        complaintsAdapter = ComplaintAdapter { complaint ->
            showComplaintActionDialog(complaint)
        }
        binding.rvComplaints.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComplaints.adapter = complaintsAdapter
    }

    private fun showComplaintActionDialog(complaint: Complaint) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(
            com.campusconnect.R.layout.dialog_admin_complaint_status, null
        )

        val actvStatus = dialogView.findViewById<AutoCompleteTextView>(com.campusconnect.R.id.actv_dialog_status)
        val etDuplicateOfId = dialogView.findViewById<EditText>(com.campusconnect.R.id.et_dialog_duplicate_id)

        val statuses = arrayOf("submitted", "in_progress", "resolved", "duplicate")
        actvStatus.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses)
        )

        // Prepopulate values
        actvStatus.setText(complaint.status, false)
        etDuplicateOfId.setText(complaint.duplicateOfId)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Manage Complaint Status")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()

        dialog.show()

        // Handle custom click listener to prevent auto-close if validations fail
        dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener {
            val status = actvStatus.text.toString().trim()
            val duplicateId = etDuplicateOfId.text.toString().trim()

            if (status == "duplicate" && duplicateId.isEmpty()) {
                etDuplicateOfId.error = "Duplicate Original ID is required"
                return@setOnClickListener
            }

            viewModel.updateComplaintStatus(complaint.complaintId, status, duplicateId)
            dialog.dismiss()
        }
    }

    private fun setupEventsTab() {
        binding.btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                
                TimePickerDialog(requireContext(), { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    
                    eventTime = calendar.timeInMillis
                    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                    binding.tvPickedDate.text = "Picked Date: ${sdf.format(calendar.time)}"
                }, 10, 0, false).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnAttachBanner.setOnClickListener {
            bannerPickerLauncher.launch("image/*")
        }

        binding.btnSubmitEvent.setOnClickListener {
            val title = binding.etEventTitle.text.toString().trim()
            val host = binding.etEventHost.text.toString().trim()
            val location = binding.etEventLocation.text.toString().trim()
            val description = binding.etEventDescription.text.toString().trim()

            if (title.isEmpty() || host.isEmpty() || location.isEmpty() || description.isEmpty() || eventTime == 0L) {
                showErrorSnackbar("Please fill out all event fields & pick a date.")
                return@setOnClickListener
            }

            viewModel.createEvent(
                title = title,
                description = description,
                hostType = host,
                date = eventTime,
                location = location,
                bannerUri = selectedEventBannerUri
            )
        }
    }

    private fun setupJobsTab() {
        val jobTypes = arrayOf("job", "internship")
        binding.actvJobType.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, jobTypes)
        )

        binding.btnPickDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)

                jobDeadline = calendar.timeInMillis
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.tvPickedDeadline.text = "Deadline: ${sdf.format(calendar.time)}"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnSubmitJob.setOnClickListener {
            val company = binding.etJobCompany.text.toString().trim()
            val title = binding.etJobTitle.text.toString().trim()
            val type = binding.actvJobType.text.toString().trim()
            val skillsText = binding.etJobSkills.text.toString().trim()
            val link = binding.etJobLink.text.toString().trim()
            val description = binding.etJobDescription.text.toString().trim()

            if (company.isEmpty() || title.isEmpty() || type.isEmpty() || skillsText.isEmpty() || link.isEmpty() || description.isEmpty() || jobDeadline == 0L) {
                showErrorSnackbar("Please fill out all placement fields.")
                return@setOnClickListener
            }

            val skillsList = skillsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            viewModel.createJob(
                companyName = company,
                title = title,
                type = type,
                skills = skillsList,
                applyLink = link,
                description = description,
                deadline = jobDeadline
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. Observe Admin complaints list
                launch {
                    viewModel.complaintsList.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.progressBar.show()
                                binding.emptyStateComplaints.hide()
                            }
                            is Resource.Success -> {
                                binding.progressBar.hide()
                                val list = state.data
                                val sortedList = list.sortedWith { o1, o2 ->
                                    val p1 = when (o1.priority.lowercase()) {
                                        "high" -> 3
                                        "medium" -> 2
                                        "low" -> 1
                                        else -> 2
                                    }
                                    val p2 = when (o2.priority.lowercase()) {
                                        "high" -> 3
                                        "medium" -> 2
                                        "low" -> 1
                                        else -> 2
                                    }
                                    p2.compareTo(p1)
                                }
                                complaintsAdapter.submitList(sortedList)

                                if (list.isEmpty()) {
                                    binding.emptyStateComplaints.show()
                                    binding.rvComplaints.hide()
                                } else {
                                    binding.emptyStateComplaints.hide()
                                    binding.rvComplaints.show()
                                }
                            }
                            is Resource.Error -> {
                                binding.progressBar.hide()
                                binding.emptyStateComplaints.show()
                            }
                        }
                    }
                }

                // 2. Observe Event creation flow
                launch {
                    viewModel.createEventState.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.progressBar.show()
                                binding.btnSubmitEvent.isEnabled = false
                            }
                            is Resource.Success -> {
                                binding.progressBar.hide()
                                binding.btnSubmitEvent.isEnabled = true
                                showSnackbar("Official Event published successfully!")
                                resetEventForm()
                                viewModel.resetStates()
                            }
                            is Resource.Error -> {
                                binding.progressBar.hide()
                                binding.btnSubmitEvent.isEnabled = true
                                showErrorSnackbar(state.message)
                                viewModel.resetStates()
                            }
                            null -> {}
                        }
                    }
                }

                // 3. Observe Job creation flow
                launch {
                    viewModel.createJobState.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.progressBar.show()
                                binding.btnSubmitJob.isEnabled = false
                            }
                            is Resource.Success -> {
                                binding.progressBar.hide()
                                binding.btnSubmitJob.isEnabled = true
                                showSnackbar("Official Placement posted successfully!")
                                resetJobForm()
                                viewModel.resetStates()
                            }
                            is Resource.Error -> {
                                binding.progressBar.hide()
                                binding.btnSubmitJob.isEnabled = true
                                showErrorSnackbar(state.message)
                                viewModel.resetStates()
                            }
                            null -> {}
                        }
                    }
                }

                // 4. Observe Complaint status update
                launch {
                    viewModel.updateComplaintState.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> binding.progressBar.show()
                            is Resource.Success -> {
                                binding.progressBar.hide()
                                showSnackbar("Complaint updated successfully!")
                                viewModel.resetStates()
                            }
                            is Resource.Error -> {
                                binding.progressBar.hide()
                                showErrorSnackbar(state.message)
                                viewModel.resetStates()
                            }
                            null -> {}
                        }
                    }
                }
            }
        }
    }

    private fun resetEventForm() {
        binding.etEventTitle.text?.clear()
        binding.etEventHost.text?.clear()
        binding.etEventLocation.text?.clear()
        binding.etEventDescription.text?.clear()
        binding.tvPickedDate.text = ""
        binding.cardBannerPreview.hide()
        selectedEventBannerUri = null
        eventTime = 0L
    }

    private fun resetJobForm() {
        binding.etJobCompany.text?.clear()
        binding.etJobTitle.text?.clear()
        binding.actvJobType.text = null
        binding.etJobSkills.text?.clear()
        binding.etJobLink.text?.clear()
        binding.etJobDescription.text?.clear()
        binding.tvPickedDeadline.text = ""
        jobDeadline = 0L
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
