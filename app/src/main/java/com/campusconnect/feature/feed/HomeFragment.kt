package com.campusconnect.feature.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.campusconnect.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Phase 1 stub — will be replaced with full feed implementation in Phase 2.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvWelcome.text = "Welcome to CampusConnect!"
        binding.tvSubtitle.text = "Feed coming in Phase 2"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
