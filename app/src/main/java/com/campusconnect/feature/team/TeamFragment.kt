package com.campusconnect.feature.team

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.campusconnect.databinding.FragmentTeamBinding
import com.campusconnect.databinding.ItemTeamMemberBinding
import com.campusconnect.domain.model.TeamMember

class TeamFragment : Fragment() {

    private var _binding: FragmentTeamBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val teamMembers = listOf(
            TeamMember(
                name = "Muhammad Saad",
                role = "Developer",
                avatarRes = com.campusconnect.R.drawable.ic_saad,
                githubUrl = "https://github.com/chsaad-dev",
                linkedinUrl = "https://www.linkedin.com/in/muhammad-saad075/"
            ),
            TeamMember(
                name = "Ghulam Fareed",
                role = "Developer",
                avatarRes = com.campusconnect.R.drawable.ic_farid,
                githubUrl = "https://github.com/fareediftikhar70-jpg",
                linkedinUrl = "https://www.linkedin.com/in/ghulam-farid-06635541a/"
            )
        )

        binding.rvTeam.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TeamAdapter(teamMembers)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class TeamAdapter(private val members: List<TeamMember>) :
    RecyclerView.Adapter<TeamAdapter.VH>() {

    inner class VH(val binding: ItemTeamMemberBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTeamMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val member = members[position]
        holder.binding.apply {
            tvName.text = member.name
            tvRole.text = member.role
            viewAvatar.loadAvatar(member.avatarRes, member.name)

            ivGithub.isVisible = member.githubUrl != null
            ivGithub.setOnClickListener {
                member.githubUrl?.let { url ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        root.context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            ivLinkedin.isVisible = member.linkedinUrl != null
            ivLinkedin.setOnClickListener {
                member.linkedinUrl?.let { url ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        root.context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun getItemCount() = members.size
}
