package com.campusconnect.feature.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import android.text.format.DateUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.campusconnect.databinding.ItemCommentBinding
import com.campusconnect.domain.model.Comment

class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.apply {
                tvAuthorName.text = comment.authorName
                tvUsername.text = "@${comment.authorUsername}"
                tvCommentText.text = comment.text
                viewAvatar.loadAvatar(comment.authorPhotoUrl, comment.authorName)

                val elapsed = DateUtils.getRelativeTimeSpanString(
                    comment.createdAt,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
                tvTime.text = "• $elapsed"
            }
        }
    }
}

class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(old: Comment, new: Comment): Boolean = old.commentId == new.commentId
    override fun areContentsTheSame(old: Comment, new: Comment): Boolean = old == new
}
