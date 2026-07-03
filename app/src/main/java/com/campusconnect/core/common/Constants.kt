package com.campusconnect.core.common

/**
 * App-wide constants for Firestore collection names and keys.
 */
object Constants {

    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_USERNAMES = "usernames"
    const val COLLECTION_POSTS = "posts"
    const val COLLECTION_NOTES = "notes"
    const val COLLECTION_LOST_FOUND = "lostFoundItems"
    const val COLLECTION_BLOOD_REQUESTS = "bloodRequests"
    const val COLLECTION_RIDES = "rides"
    const val COLLECTION_FRIEND_REQUESTS = "friendRequests"
    const val COLLECTION_CHATS = "chats"
    const val COLLECTION_MESSAGES = "messages"
    const val COLLECTION_COMPLAINTS = "complaints"
    const val COLLECTION_EVENTS = "events"
    const val COLLECTION_JOBS = "jobs"
    const val COLLECTION_NOTIFICATIONS = "notifications"

    // Sub-collections
    const val SUBCOLLECTION_LIKES = "likes"
    const val SUBCOLLECTION_COMMENTS = "comments"
    const val SUBCOLLECTION_JOINED_USERS = "joinedUsers"
    const val SUBCOLLECTION_ITEMS = "items"

    // DataStore
    const val DATASTORE_NAME = "campus_connect_prefs"
    const val PREF_THEME_MODE = "theme_mode"

    // Firestore field names
    const val FIELD_UID = "uid"
    const val FIELD_CREATED_AT = "createdAt"
    const val FIELD_USERNAME = "uniqueUsername"

    // Pagination
    const val PAGE_SIZE = 20
}
