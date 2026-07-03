package com.campusconnect.core.common

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

/**
 * Extension functions used across the app.
 */

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Fragment.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(requireView(), message, duration).show()
}

fun Fragment.showErrorSnackbar(message: String) {
    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
        .setBackgroundTint(requireContext().getColor(com.campusconnect.R.color.error))
        .setTextColor(requireContext().getColor(com.campusconnect.R.color.white))
        .show()
}

fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidUsername(): Boolean {
    // Instagram-style: 1-30 chars, lowercase letters, numbers, underscores, periods
    return matches(Regex("^[a-z0-9._]{1,30}$")) && !startsWith(".") && !endsWith(".")
}
