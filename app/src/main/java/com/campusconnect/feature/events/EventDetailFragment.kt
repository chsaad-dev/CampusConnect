package com.campusconnect.feature.events

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.core.common.hide
import com.campusconnect.core.common.show
import com.campusconnect.core.common.showErrorSnackbar
import com.campusconnect.core.common.showSnackbar
import com.campusconnect.databinding.FragmentEventDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventViewModel by viewModels()
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val eventId = arguments?.getString("eventId")
        if (eventId == null) {
            showErrorSnackbar("Invalid Event ID")
            findNavController().navigateUp()
            return
        }

        setupObservers(eventId)
        viewModel.loadEventDetails(eventId)
    }

    private fun setupObservers(eventId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe Event details
                launch {
                    viewModel.eventDetail.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.progressBar.show()
                                binding.layoutContent.hide()
                            }
                            is Resource.Success -> {
                                binding.progressBar.hide()
                                binding.layoutContent.show()

                                val event = state.data
                                binding.tvTitle.text = event.title
                                binding.tvHost.text = "Hosted by ${event.hostType}"
                                binding.tvDescription.text = event.description

                                Glide.with(requireContext())
                                    .load(event.bannerUrl)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_gallery)
                                    .into(binding.ivBanner)

                                val currentUid = viewModel.currentUid
                                if (event.registeredUsers.contains(currentUid)) {
                                    binding.btnRegister.hide()
                                    binding.cardTicket.show()
                                    binding.tvTicketId.text = "Ticket ID: ${event.qrCode}_${currentUid.take(5)}"
                                } else {
                                    binding.btnRegister.show()
                                    binding.cardTicket.hide()

                                    binding.btnRegister.setOnClickListener {
                                        viewModel.registerForEvent(eventId)
                                    }
                                }

                                setupCountdown(event.date)
                            }
                            is Resource.Error -> {
                                binding.progressBar.hide()
                                showErrorSnackbar(state.message)
                            }
                        }
                    }
                }

                // Observe Registration state
                launch {
                    viewModel.registerState.collectLatest { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.btnRegister.isEnabled = false
                            }
                            is Resource.Success -> {
                                binding.btnRegister.isEnabled = true
                                showSnackbar("Successfully registered!")
                                viewModel.resetRegisterState()
                            }
                            is Resource.Error -> {
                                binding.btnRegister.isEnabled = true
                                showErrorSnackbar(state.message)
                                viewModel.resetRegisterState()
                            }
                            null -> {}
                        }
                    }
                }
            }
        }
    }

    private fun setupCountdown(eventTime: Long) {
        countDownTimer?.cancel()

        val timeDiff = eventTime - System.currentTimeMillis()
        if (timeDiff <= 0) {
            binding.tvCountdown.text = "Event Started"
            return
        }

        countDownTimer = object : CountDownTimer(timeDiff, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                binding.tvCountdown.text = String.format(
                    "%02dd : %02dh : %02dm : %02ds",
                    days, hours, minutes, seconds
                )
            }

            override fun onFinish() {
                binding.tvCountdown.text = "Event Started"
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}
