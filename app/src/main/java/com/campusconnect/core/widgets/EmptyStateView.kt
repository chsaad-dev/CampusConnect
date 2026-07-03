package com.campusconnect.core.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.campusconnect.R
import com.campusconnect.databinding.ViewEmptyStateBinding

class EmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewEmptyStateBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        orientation = VERTICAL
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.EmptyStateView)
            
            val iconRes = typedArray.getResourceId(R.styleable.EmptyStateView_emptyIcon, -1)
            if (iconRes != -1) {
                binding.ivEmptyIcon.setImageResource(iconRes)
            }

            val title = typedArray.getString(R.styleable.EmptyStateView_emptyTitle)
            binding.tvEmptyTitle.text = title

            val description = typedArray.getString(R.styleable.EmptyStateView_emptyDescription)
            binding.tvEmptyDescription.text = description

            val actionText = typedArray.getString(R.styleable.EmptyStateView_emptyActionText)
            if (!actionText.isNullOrEmpty()) {
                binding.btnEmptyAction.text = actionText
                binding.btnEmptyAction.visibility = View.VISIBLE
            } else {
                binding.btnEmptyAction.visibility = View.GONE
            }

            typedArray.recycle()
        }
    }

    fun setActionClickListener(listener: OnClickListener) {
        binding.btnEmptyAction.setOnClickListener(listener)
    }

    fun setupEmptyState(iconRes: Int, title: String, description: String, actionText: String? = null, actionListener: OnClickListener? = null) {
        binding.ivEmptyIcon.setImageResource(iconRes)
        binding.tvEmptyTitle.text = title
        binding.tvEmptyDescription.text = description
        if (!actionText.isNullOrEmpty() && actionListener != null) {
            binding.btnEmptyAction.text = actionText
            binding.btnEmptyAction.visibility = View.VISIBLE
            binding.btnEmptyAction.setOnClickListener(actionListener)
        } else {
            binding.btnEmptyAction.visibility = View.GONE
        }
    }
}
