package com.example.campusconnect.ui.blood

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.campusconnect.databinding.FragmentBloodDonationBinding
import com.example.campusconnect.ui.adapter.BloodAdapter
import com.example.campusconnect.ui.adapter.DonorAdapter
import com.example.campusconnect.ui.viewmodel.BloodViewModel
import com.example.campusconnect.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BloodDonationFragment : Fragment() {
    private var _binding: FragmentBloodDonationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BloodViewModel by viewModels()
    private lateinit var bloodAdapter: BloodAdapter
    private lateinit var donorAdapter: DonorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBloodDonationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupBloodGroupSpinner()
        observeViewModel()
        
        binding.fabRequest.setOnClickListener {
            Toast.makeText(context, "Request Blood feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        viewModel.fetchRequests()
    }

    private fun setupRecyclerViews() {
        bloodAdapter = BloodAdapter(
            onDonateClick = { _ ->
                Toast.makeText(context, "Thank you for volunteering!", Toast.LENGTH_SHORT).show()
            },
            onContactClick = { request ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${request.contactNumber}")
                }
                startActivity(intent)
            }
        )
        
        donorAdapter = DonorAdapter { donor ->
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${donor.phoneNumber}")
            }
            startActivity(intent)
        }
        
        binding.rvDonors.adapter = bloodAdapter
    }

    private fun setupBloodGroupSpinner() {
        val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, bloodGroups)
        (binding.bloodGroupLayout.editText as? android.widget.AutoCompleteTextView)?.apply {
            setAdapter(spinnerAdapter)
            setOnItemClickListener { _, _, position, _ ->
                binding.rvDonors.adapter = donorAdapter
                viewModel.searchDonors(bloodGroups[position])
            }
        }
    }

    private fun observeViewModel() {
        viewModel.requests.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                bloodAdapter.submitList(resource.data)
            }
        }

        viewModel.donors.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                donorAdapter.submitList(resource.data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
