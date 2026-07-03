package com.campusconnect.feature.feed

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.campusconnect.R
import com.campusconnect.databinding.ItemPostCardBinding
import com.campusconnect.domain.model.MediaType
import com.campusconnect.domain.model.Post
import com.campusconnect.domain.model.PostType

class PostAdapter(
    private val onLikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit,
    private val onShareClick: (Post) -> Unit,
    private val onCardClick: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(
        private val binding: ItemPostCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                tvAuthorName.text = post.authorName
                
                // Formatted elapsed time
                val timeAgo = DateUtils.getRelativeTimeSpanString(
                    post.createdAt,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
                tvUsernameTime.text = "@${post.authorUsername} • $timeAgo"
                
                tvCaption.text = post.caption
                tvLikeCount.text = post.likeCount.toString()
                tvCommentCount.text = post.commentCount.toString()

                // Load avatar
                viewAvatar.loadAvatar(post.authorPhotoUrl)

                // Setup post type chip color-coding
                chipPostType.text = post.type.name
                val chipColor = when (post.type) {
                    PostType.NOTE -> root.context.getColor(R.color.notes_green)
                    PostType.BLOOD -> root.context.getColor(R.color.blood_red)
                    PostType.LOST_FOUND -> root.context.getColor(R.color.lost_found_orange)
                    PostType.RIDE -> root.context.getColor(R.color.ride_blue)
                }
                chipPostType.setTextColor(chipColor)

                // Like status color
                if (post.isLikedByCurrentUser) {
                    ivLike.setImageResource(android.R.drawable.btn_star_big_on)
                } else {
                    ivLike.setImageResource(android.R.drawable.btn_star_big_off)
                }

                // Media layout setups
                setupMedia(post)

                // Actions wiring
                btnLike.setOnClickListener { onLikeClick(post) }
                btnComment.setOnClickListener { onCommentClick(post) }
                btnShare.setOnClickListener { onShareClick(post) }
                root.setOnClickListener { onCardClick(post) }
            }
        }

        private fun ItemPostCardBinding.setupMedia(post: Post) {
            if (post.mediaType == MediaType.NONE || post.mediaUrls.isEmpty()) {
                mediaContainer.visibility = View.GONE
                return
            }

            mediaContainer.visibility = View.VISIBLE
            ivPostImage.visibility = View.GONE
            cardDocument.visibility = View.GONE
            videoPlaceholder.visibility = View.GONE

            val firstUrl = post.mediaUrls.firstOrNull() ?: return

            when (post.mediaType) {
                MediaType.IMAGE -> {
                    ivPostImage.visibility = View.VISIBLE
                    Glide.with(root.context)
                        .load(firstUrl)
                        .placeholder(R.color.surface_variant)
                        .into(ivPostImage)
                }
                MediaType.PDF, MediaType.DOCX, MediaType.PPT -> {
                    cardDocument.visibility = View.VISIBLE
                    tvDocTitle.text = firstUrl.substringAfterLast("/")
                        .substringBefore("?")
                        .ifBlank { "Attached Document" }
                    tvDocType.text = post.mediaType.name
                    ivDocIcon.setImageResource(
                        if (post.mediaType == MediaType.PDF) android.R.drawable.ic_menu_save
                        else android.R.drawable.ic_menu_agenda
                    )
                }
                MediaType.VIDEO -> {
                    videoPlaceholder.visibility = View.VISIBLE
                }
                else -> {
                    mediaContainer.visibility = View.GONE
                }
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(old: Post, new: Post): Boolean = old.postId == new.postId
    override fun areContentsTheSame(old: Post, new: Post): Boolean = old == new
}
