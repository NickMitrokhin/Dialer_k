package com.nickmitrokhin.dialer.domain.models

data class ContactsPrefs(
    val searchQuery: String,
    val scrollPosition: Int,
    val searchEnabled: Boolean
)