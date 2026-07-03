package com.campusconnect.core.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.campusconnect.R
import com.campusconnect.databinding.ViewUserAvatarBinding

class UserAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewUserAvatarBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.UserAvatarView)
            val size = typedArray.getDimensionPixelSize(
                R.styleable.UserAvatarView_avatarSize,
                resources.getDimensionPixelSize(R.dimen.default_avatar_size)
            )
            binding.ivAvatar.layoutParams = binding.ivAvatar.layoutParams.apply {
                width = size
                height = size
            }
            typedArray.recycle()
        }
    }

    fun loadAvatar(url: String?) {
        Glide.with(this)
            .load(url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(binding.ivAvatar)
    }
}
