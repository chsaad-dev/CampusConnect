package com.campusconnect.feature.post_detail

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
import com.campusconnect.core.common.showSnackbar
import com.campusconnect.databinding.FragmentNoteDetailBinding
import com.campusconnect.domain.model.PostType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
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

        viewModel.loadDetails(postId, PostType.NOTE)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.noteDetails.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.show()
                            binding.contentCard.hide()
                        }
                        is Resource.Success -> {
                            binding.progressBar.hide()
                            binding.contentCard.show()
                            
                            val details = state.data
                            binding.tvSubject.text = details.subject.takeIf { it.isNotEmpty() } ?: "No Subject"
                            binding.tvTeacher.text = details.teacher.takeIf { it.isNotEmpty() } ?: "Unknown Teacher"
                            binding.tvRating.text = "${details.rating}/5.0"
                            binding.tvDownloads.text = "${details.downloads} Downloads"

                            binding.btnOpenDocument.setOnClickListener {
                                if (details.fileUrl.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.data = Uri.parse(details.fileUrl)
                                    startActivity(intent)
                                } else {
                                    showErrorSnackbar("No document attached.")
                                }
                            }

                            binding.btnBookmark.setOnClickListener {
                                showSnackbar("Note bookmarked!")
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
