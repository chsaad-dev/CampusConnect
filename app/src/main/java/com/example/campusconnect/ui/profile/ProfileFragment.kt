package com.example.campusconnect.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.campusconnect.databinding.FragmentProfileBinding
import com.example.campusconnect.ui.viewmodel.ProfileViewModel
import com.example.campusconnect.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(context, "Edit Profile feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            // viewModel.logout()
            Toast.makeText(context, "Logged out!", Toast.LENGTH_SHORT).show()
        }

        viewModel.fetchUserProfile("current_user_id") // TODO: Get actual user ID
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val user = resource.data
                    binding.apply {
                        tvName.text = user?.name
                        tvRollNo.text = "Roll No: ${user?.rollNumber}"
                        // Update other fields as well
                    }
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
