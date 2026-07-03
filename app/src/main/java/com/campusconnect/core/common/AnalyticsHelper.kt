package com.campusconnect.core.common

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsHelper {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        }
    }

    fun logEvent(name: String, params: Bundle = Bundle()) {
        firebaseAnalytics?.logEvent(name, params)
    }

    fun logLogin(uid: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, "firebase_auth")
            putString("user_id", uid)
        }
        logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    fun logRegistration(uid: String, role: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, "firebase_auth")
            putString("user_id", uid)
            putString("user_role", role)
        }
        logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
    }

    fun logNoteDownload(postId: String, subject: String) {
        val bundle = Bundle().apply {
            putString("post_id", postId)
            putString("note_subject", subject)
        }
        logEvent("note_download", bundle)
    }

    fun logJobApply(jobId: String, title: String) {
        val bundle = Bundle().apply {
            putString("job_id", jobId)
            putString("job_title", title)
        }
        logEvent("job_apply", bundle)
    }

    fun logEventRegistration(eventId: String, title: String) {
        val bundle = Bundle().apply {
            putString("event_id", eventId)
            putString("event_title", title)
        }
        logEvent("event_registration", bundle)
    }

    fun logComplaintSubmitted(category: String) {
        val bundle = Bundle().apply {
            putString("complaint_category", category)
        }
        logEvent("complaint_submitted", bundle)
    }

    fun logError(e: Throwable, message: String) {
        Log.e("AnalyticsHelper", message, e)
    }
}
