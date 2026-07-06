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

    private var lastAnimatedPosition = -1

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
        setEntranceAnimation(holder.itemView, position)
    }

    private fun setEntranceAnimation(view: View, position: Int) {
        if (position > lastAnimatedPosition) {
            val animation = android.view.animation.AnimationUtils.loadAnimation(
                view.context,
                android.R.anim.fade_in
            )
            animation.duration = 350
            view.startAnimation(animation)
            lastAnimatedPosition = position
        }
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
                viewAvatar.loadAvatar(post.authorPhotoUrl, post.authorName)

                // Setup post type chip color-coding
                chipPostType.text = post.type.name
                val chipColor = when (post.type) {
                    PostType.NOTE -> root.context.getColor(R.color.chip_note_text)
                    PostType.BLOOD -> root.context.getColor(R.color.chip_blood_text)
                    PostType.LOST_FOUND -> root.context.getColor(R.color.chip_lostfound_text)
                    PostType.RIDE -> root.context.getColor(R.color.chip_ride_text)
                    PostType.STATUS -> root.context.getColor(R.color.chip_note_text)
                }
                val chipBgColor = when (post.type) {
                    PostType.NOTE -> root.context.getColor(R.color.chip_note_bg)
                    PostType.BLOOD -> root.context.getColor(R.color.chip_blood_bg)
                    PostType.LOST_FOUND -> root.context.getColor(R.color.chip_lostfound_bg)
                    PostType.RIDE -> root.context.getColor(R.color.chip_ride_bg)
                    PostType.STATUS -> root.context.getColor(R.color.chip_note_bg)
                }
                chipPostType.setTextColor(chipColor)
                chipPostType.chipBackgroundColor = android.content.res.ColorStateList.valueOf(chipBgColor)
                chipPostType.chipStrokeWidth = 0f

                // Like status icon
                if (post.isLikedByCurrentUser) {
                    ivLike.setImageResource(R.drawable.ic_like_filled)
                    ivLike.imageTintList = null
                    tvLikeCount.setTextColor(root.context.getColor(R.color.like_active))
                } else {
                    ivLike.setImageResource(R.drawable.ic_like_outline)
                    ivLike.imageTintList = android.content.res.ColorStateList.valueOf(root.context.getColor(R.color.text_secondary))
                    tvLikeCount.setTextColor(root.context.getColor(R.color.text_secondary))
                }

                // Media layout setups
                setupMedia(post)

                // Actions wiring
                btnLike.setOnClickListener {
                    val newLiked = !post.isLikedByCurrentUser
                    onLikeClick(post)
                    animateLike(ivLike, newLiked)
                    
                    // Optimistic update to count color and value to feel instantaneous
                    tvLikeCount.text = (if (newLiked) post.likeCount + 1 else post.likeCount - 1).coerceAtLeast(0).toString()
                    if (newLiked) {
                        tvLikeCount.setTextColor(root.context.getColor(R.color.like_active))
                    } else {
                        tvLikeCount.setTextColor(root.context.getColor(R.color.text_secondary))
                    }
                }
                btnComment.setOnClickListener { onCommentClick(post) }
                btnShare.setOnClickListener { onShareClick(post) }
                root.setOnClickListener { onCardClick(post) }
            }
        }

        private fun animateLike(view: android.widget.ImageView, liked: Boolean) {
            view.setImageResource(if (liked) R.drawable.ic_like_filled else R.drawable.ic_like_outline)
            if (liked) {
                view.imageTintList = null
            } else {
                view.imageTintList = android.content.res.ColorStateList.valueOf(view.context.getColor(R.color.text_secondary))
            }
            view.animate()
                .scaleX(1.3f).scaleY(1.3f)
                .setDuration(120)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }.start()
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
