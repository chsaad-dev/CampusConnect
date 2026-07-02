package com.example.campusconnect.ui.lostfound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.campusconnect.databinding.FragmentLostFoundBinding
import com.example.campusconnect.ui.adapter.LostFoundAdapter
import com.example.campusconnect.ui.viewmodel.LostAndFoundViewModel
import com.example.campusconnect.util.Resource
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LostFoundFragment : Fragment() {
    private var _binding: FragmentLostFoundBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LostAndFoundViewModel by viewModels()
    private lateinit var adapter: LostFoundAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLostFoundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupTabs()
        observeViewModel()

        binding.fabReport.setOnClickListener {
            Toast.makeText(context, "Report feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        viewModel.fetchItems()
    }

    private fun setupRecyclerView() {
        adapter = LostFoundAdapter(
            onItemClick = { item ->
                Toast.makeText(context, "Detail for ${item.title}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvItems.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.fetchItems()
                    1 -> viewModel.searchItems("Lost")
                    2 -> viewModel.searchItems("Found")
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.items.observe(viewLifecycleOwner) { resource ->
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
