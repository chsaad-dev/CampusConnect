package com.campusconnect.feature.post_detail

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
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.core.common.showErrorSnackbar
import com.campusconnect.core.common.showSnackbar
import com.campusconnect.databinding.FragmentRideDetailBinding
import com.campusconnect.domain.model.PostType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RideDetailFragment : Fragment() {

    private var _binding: FragmentRideDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRideDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val postId = arguments?.getString("postId")
        if (postId == null) {
            showErrorSnackbar("Invalid Post ID")
            findNavController().navigateUp()
            return
        }

        viewModel.loadDetails(postId, PostType.RIDE)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rideDetails.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                            binding.contentCard.hide()
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            binding.contentCard.show()
                            
                            val details = state.data
                            binding.tvFrom.text = details.from.takeIf { it.isNotEmpty() } ?: "Unknown"
                            binding.tvTo.text = details.to.takeIf { it.isNotEmpty() } ?: "Unknown"
                            binding.tvSeats.text = "${details.seatsLeft} / ${details.seatsTotal} Seats Left"
                            binding.tvCost.text = details.cost.takeIf { it.isNotEmpty() } ?: "Free"

                            binding.btnRequestSeat.setOnClickListener {
                                val bundle = Bundle().apply {
                                    putString("targetUid", details.driverId)
                                    putString("targetName", "Driver")
                                }
                                findNavController().navigate(R.id.action_rideDetailFragment_to_chatFragment, bundle)
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
