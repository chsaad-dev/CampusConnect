package com.campusconnect.feature.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.campusconnect.R
import com.campusconnect.core.common.Resource
import com.campusconnect.core.theme.ThemeManager
import com.campusconnect.core.theme.ThemeMode
import com.campusconnect.databinding.FragmentSettingsBinding
import com.campusconnect.domain.model.UserRole
import com.campusconnect.domain.repository.UserRepository
import com.campusconnect.feature.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.widget.ArrayAdapter
import android.widget.AdapterView

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var firestore: com.google.firebase.firestore.FirebaseFirestore

    @Inject
    lateinit var preferenceManager: com.campusconnect.core.common.PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupThemeToggle()
        setupLogout()
        setupMenuClickListeners()
        setupLanguageSpinner()
        loadUserProfile()
    }

    private fun setupThemeToggle() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                themeManager.themeMode.collect { mode ->
                    binding.switchTheme.isChecked = mode == ThemeMode.DARK
                    binding.tvThemeLabel.text = when (mode) {
                        ThemeMode.LIGHT -> "Light Mode"
                        ThemeMode.DARK -> "Dark Mode"
                    }
                }
            }
        }

        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                themeManager.setThemeMode(
                    if (isChecked) ThemeMode.DARK else ThemeMode.LIGHT
                )
            }
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_settingsFragment_to_authNavGraph)
        }
    }

    private fun setupLanguageSpinner() {
        val languages = listOf("Urdu", "Arabic", "Spanish", "French", "English")
        val langCodes = listOf("ur", "ar", "es", "fr", "en")
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerTranslationLang.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                preferenceManager.targetTranslationLanguage.collectLatest { currentLangCode ->
                    val index = langCodes.indexOf(currentLangCode).coerceAtLeast(0)
                    binding.spinnerTranslationLang.setSelection(index)
                }
            }
        }

        binding.spinnerTranslationLang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCode = langCodes[position]
                viewLifecycleOwner.lifecycleScope.launch {
                    preferenceManager.setTargetTranslationLanguage(selectedCode)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupMenuClickListeners() {
        binding.cardAssistant.setOnClickListener {
            findNavController().navigate(R.id.assistantFragment)
        }
        binding.cardFriends.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_friendsFragment)
        }
        binding.cardComplaints.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_complaintListFragment)
        }
        binding.cardEvents.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_eventListFragment)
        }
        binding.cardJobs.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_jobListFragment)
        }
        binding.cardAdmin.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_adminDashboardFragment)
        }
    }

    private fun loadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userRepository.getCurrentUserProfile().collectLatest { resource ->
                    if (resource is Resource.Success) {
                        val user = resource.data
                        binding.tvProfileName.text = user.name.takeIf { it.isNotEmpty() } ?: "User"
                        binding.tvProfileDept.text = user.department.takeIf { it.isNotEmpty() } ?: "General Department"
                        binding.tvProfileFriends.text = user.friendsCount.toString()
                        binding.tvProfileReputation.text = user.reputationPoints.toString()
                        
                        binding.viewProfileAvatar.loadAvatar(user.photoUrl, user.name)

                        // Calculate posts count dynamically
                        launch {
                            try {
                                val postsSnapshot = firestore.collection(com.campusconnect.core.common.Constants.COLLECTION_POSTS)
                                    .whereEqualTo("authorId", user.uid)
                                    .get()
                                    .await()
                                binding.tvProfilePosts.text = postsSnapshot.size().toString()
                            } catch (e: Exception) {
                                binding.tvProfilePosts.text = "0"
                            }
                        }

                        // Bind reputation progression and level
                        bindReputationLevel(user.reputationPoints)

                        // Render subject analytics
                        renderSubjectAnalytics(user.viewedSubjects)

                        if (user.role == UserRole.ADMIN) {
                            binding.cardAdmin.visibility = View.VISIBLE
                        } else {
                            binding.cardAdmin.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun bindReputationLevel(reputationPoints: Int) {
        val levelName: String
        val progressPercent: Int
        val progressText: String

        if (reputationPoints < 50) {
            levelName = "Novice Scholar"
            progressPercent = ((reputationPoints / 50f) * 100).toInt()
            progressText = "$reputationPoints / 50 XP"
        } else if (reputationPoints in 50..199) {
            levelName = "Active Contributor"
            progressPercent = (((reputationPoints - 50) / 150f) * 100).toInt()
            progressText = "${reputationPoints - 50} / 150 XP"
        } else if (reputationPoints in 200..499) {
            levelName = "Campus Mentor"
            progressPercent = (((reputationPoints - 200) / 300f) * 100).toInt()
            progressText = "${reputationPoints - 200} / 300 XP"
        } else {
            levelName = "Campus Legend"
            progressPercent = 100
            progressText = "$reputationPoints XP"
        }

        binding.tvProfileLevel.text = levelName
        binding.tvLevelProgressLabel.text = progressText
        binding.pbLevelProgress.progress = progressPercent
    }

    private fun renderSubjectAnalytics(subjects: List<String>) {
        binding.layoutAnalyticsContainer.removeAllViews()
        if (subjects.isEmpty()) {
            binding.tvNoAnalytics.visibility = View.VISIBLE
            return
        }
        binding.tvNoAnalytics.visibility = View.GONE

        // Group subjects to count occurrences
        val counts = subjects.groupingBy { it }.eachCount()
        val total = counts.values.sum().toFloat()
        
        // Sort descending and take top 3
        val sortedTop = counts.entries.sortedByDescending { it.value }.take(3)

        for (entry in sortedTop) {
            val percentage = ((entry.value / total) * 100).toInt()
            
            val rowLayout = android.widget.LinearLayout(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(12))
                }
                orientation = android.widget.LinearLayout.VERTICAL
            }

            val headerLayout = android.widget.LinearLayout(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val titleText = android.widget.TextView(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = entry.key
                textSize = 14f
                setTextColor(getColorFromAttr(android.R.attr.textColorPrimary))
            }

            val percentText = android.widget.TextView(requireContext()).apply {
                text = "$percentage%"
                textSize = 12f
                setTextColor(getColorFromAttr(android.R.attr.textColorSecondary))
            }

            headerLayout.addView(titleText)
            headerLayout.addView(percentText)

            val progress = com.google.android.material.progressindicator.LinearProgressIndicator(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dpToPx(4), 0, 0)
                }
                setProgress(percentage)
                trackThickness = dpToPx(4)
                trackCornerRadius = dpToPx(2)
            }

            rowLayout.addView(headerLayout)
            rowLayout.addView(progress)

            binding.layoutAnalyticsContainer.addView(rowLayout)
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun getColorFromAttr(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        requireContext().theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
