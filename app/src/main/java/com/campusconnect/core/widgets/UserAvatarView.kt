package com.campusconnect.core.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.campusconnect.R
import com.campusconnect.databinding.ViewUserAvatarBinding
import java.util.Locale

class UserAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewUserAvatarBinding.inflate(
        LayoutInflater.from(context), this
    )

    private var avatarSize: Int = resources.getDimensionPixelSize(R.dimen.default_avatar_size)

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.UserAvatarView)
            avatarSize = typedArray.getDimensionPixelSize(
                R.styleable.UserAvatarView_avatarSize,
                resources.getDimensionPixelSize(R.dimen.default_avatar_size)
            )
            binding.ivAvatar.layoutParams = binding.ivAvatar.layoutParams.apply {
                width = avatarSize
                height = avatarSize
            }
            binding.tvInitials.layoutParams = binding.tvInitials.layoutParams.apply {
                width = avatarSize
                height = avatarSize
            }
            // Set text size proportional to the avatar size
            binding.tvInitials.textSize = (avatarSize / 3f) / resources.displayMetrics.density
            typedArray.recycle()
        }
    }

    fun loadAvatar(url: String?, name: String? = null) {
        if (!url.isNullOrEmpty()) {
            binding.tvInitials.visibility = GONE
            binding.ivAvatar.visibility = VISIBLE
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivAvatar)
        } else {
            binding.ivAvatar.visibility = GONE
            binding.tvInitials.visibility = VISIBLE

            // Get initials
            val initials = getInitials(name)
            binding.tvInitials.text = initials

            // Get deterministic background color
            val colorRes = getAvatarColor(name)
            binding.tvInitials.setBackgroundResource(R.drawable.bg_avatar_circle)
            binding.tvInitials.backgroundTintList = android.content.res.ColorStateList.valueOf(
                context.getColor(colorRes)
            )
        }
    }

    private fun getInitials(name: String?): String {
        if (name.isNullOrBlank()) return "?"
        val cleanName = name.trim()
        val parts = cleanName.split("\\s+".toRegex())
        return if (parts.size >= 2) {
            val first = parts[0].take(1).uppercase(Locale.getDefault())
            val last = parts[parts.size - 1].take(1).uppercase(Locale.getDefault())
            first + last
        } else {
            cleanName.take(1).uppercase(Locale.getDefault())
        }
    }

    private fun getAvatarColor(name: String?): Int {
        val hash = name?.hashCode() ?: 0
        val index = Math.abs(hash) % 8
        return when (index) {
            0 -> R.color.avatar_palette_1
            1 -> R.color.avatar_palette_2
            2 -> R.color.avatar_palette_3
            3 -> R.color.avatar_palette_4
            4 -> R.color.avatar_palette_5
            5 -> R.color.avatar_palette_6
            6 -> R.color.avatar_palette_7
            else -> R.color.avatar_palette_8
        }
    }
}
